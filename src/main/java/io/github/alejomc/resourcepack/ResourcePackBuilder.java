package io.github.alejomc.resourcepack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.alejomc.config.ParserConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResourcePackBuilder {

    private final List<String> modelNames = new ArrayList<>();
    private final File texturesItem;
    private final File modelsItem;
    public ResourcePackBuilder(File outputFolder) {
        File resourcePackFolder = createFolder(outputFolder, "resource pack");
        File minecraft = createFolder(resourcePackFolder, "assets", "minecraft");
        this.modelsItem = createFolder(minecraft, "models", "item");
        this.texturesItem = createFolder(minecraft, "textures", "item");
        File packMcMeta = new File(resourcePackFolder, "pack.mcmeta");
        writeToFile(packMcMeta, packMcMetaContent());
    }

    private String packMcMetaContent() {
        JsonObject packMcMeta = new JsonObject();
        JsonObject pack = new JsonObject();
        pack.addProperty("pack_format", ParserConfig.PACK_FORMAT);
        pack.addProperty("description", ParserConfig.CREDIT);
        packMcMeta.add("pack", pack);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(packMcMeta);
    }

    private File createFolder(File parent, String... children) {
        String folderPath = String.join(File.separator, children);
        File file = new File(parent, folderPath);
        if (!file.exists() && !file.mkdirs()) {
            System.out.println("Issue while creating a folder");
        }
        return file;
    }

    public void addModel(Model model) {
        File modelFile = new File(modelsItem, model.modelName() + ".json");
        writeToFile(modelFile, model.outputJson());
        File texturesFile = new File(texturesItem, model.modelName() + ".png");
        writeToFile(texturesFile, model.base64Bytes());
        modelNames.add(model.modelName());
    }

    private void writeToFile(File file, String content) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFile(File file, byte[] base64Texture) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)){
            fileOutputStream.write(base64Texture);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void build() {
        JsonObject diamondSwordJson = new JsonObject();
        diamondSwordJson.addProperty("parent", "minecraft:item/generated");

        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "minecraft:item/diamond_sword");
        diamondSwordJson.add("textures", textures);

        JsonArray overrides = new JsonArray();
        for (String name : modelNames) {
            JsonObject container = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", name.hashCode());
            container.add("predicate", predicate);
            container.addProperty("model", "item/" + name);

            overrides.add(container);
        }
        diamondSwordJson.add("overrides", overrides);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File diamondSwordFile = new File(modelsItem, "diamond_sword.json");
        writeToFile(diamondSwordFile, gson.toJson(diamondSwordJson));
    }
}
