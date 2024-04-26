package io.github.alejomc.resourcepack;

import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void parseFolderToFolder(File sourceFolder, File outputFolder) {
        File[] sourceFolderFiles = sourceFolder.listFiles();
        if (sourceFolderFiles == null) {
            return;
        }
        ResourcePackBuilder resourcePackBuilder = new ResourcePackBuilder(outputFolder);
        for (File sourceFolderFile : sourceFolderFiles) {
            if (sourceFolderFile.isDirectory() || !getFileExtension(sourceFolderFile).equals(".bbmodel")) {
                continue;
            }
            Model model = parse(sourceFolderFile);
            resourcePackBuilder.addModel(model);
        }
        resourcePackBuilder.build();
    }

    public String getFileExtension(File file) {
        String fileName = file.getName();
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf);
    }

    public Model parse(File file) {
        try (FileReader fileReader = new FileReader(file)) {
            return parse(gson.fromJson(fileReader, JsonObject.class), file.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Model parse(JsonObject jsonToBeParsed, String modelFileName) {
        JsonObject parsedJson = new JsonObject();
        parsedJson.addProperty("credit", "");

        String fileNameWithoutExtension = getNameWithoutExtension(modelFileName);
        addTexturesPathToJson(parsedJson, fileNameWithoutExtension);

        boolean modelIsValid = jsonToBeParsed.has("elements") && jsonToBeParsed.has("outliner") && jsonToBeParsed.has("textures");
        if (!modelIsValid) {
            return null;
        }
        parsedJson.add("elements", parseElements(jsonToBeParsed.getAsJsonArray("elements")));
        parsedJson.add("groups", parseGroups(jsonToBeParsed.getAsJsonArray("outliner")));
        return new Model(gson.toJson(parsedJson), extractBase64Textures(jsonToBeParsed.getAsJsonArray("textures")), fileNameWithoutExtension);
    }

    private JsonArray parseElements(JsonArray elementsArray) {
        JsonArray parsedElements = new JsonArray();
        for (JsonElement element : elementsArray) {
            JsonObject currentElement = element.getAsJsonObject();
            JsonObject container = new JsonObject();
            container.add("from", currentElement.getAsJsonArray("from"));
            container.add("to", currentElement.getAsJsonArray("to"));

            boolean elementHasBeenRotated = currentElement.has("rotation") && currentElement.has("origin");
            if (elementHasBeenRotated) {
                addRotationToElement(currentElement.getAsJsonArray("origin"), currentElement.getAsJsonArray("rotation"), container);
            }

            addFacesToElement(currentElement.getAsJsonObject("faces"), container);
            parsedElements.add(container);
        }
        return parsedElements;
    }

    private void addRotationToElement(JsonArray origin, JsonArray rotation, JsonObject container) {
        double[] rotationArray = gson.fromJson(rotation, double[].class);
        String axis = getAxisFromRotation(rotationArray);

        JsonObject rotationJson = new JsonObject();
        rotationJson.addProperty("angle", rotationArray[getAxisNumber(axis)]);
        rotationJson.addProperty("axis", axis);
        rotationJson.add("origin", origin);
        container.add("rotation", rotationJson);
    }

    private void addFacesToElement(JsonObject faces, JsonObject container) {
        for (String key : faces.keySet()) {
            JsonObject currentFace = faces.getAsJsonObject(key);
            currentFace.addProperty("texture", "#0");
        }
        container.add("faces", faces);
    }

    private JsonArray parseGroups(JsonArray groups) {
        JsonArray parsedGroups = new JsonArray();
        int index = 0;
        for (JsonElement element : groups) {
            JsonObject currentGroup = element.getAsJsonObject();
            JsonObject container = new JsonObject();
            container.addProperty("name", currentGroup.get("name").getAsString());
            container.add("origin", currentGroup.getAsJsonArray("origin"));
            container.addProperty("color", currentGroup.get("color").getAsInt());
            JsonArray childrenArray = currentGroup.getAsJsonArray("children");
            container.add("children", parseGroupsChildren(childrenArray, index));

            parsedGroups.add(container);
            index += childrenArray.size() - 1;
        }
        return parsedGroups;
    }

    private JsonArray parseGroupsChildren(JsonArray jsonArray, int index) {
        JsonArray parsedChildrenArray = new JsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            parsedChildrenArray.add(i + index);
        }
        return parsedChildrenArray;
    }

    private String extractBase64Textures(JsonArray texturesArray) {
        JsonObject jsonObject = texturesArray.get(0).getAsJsonObject();
        String source = jsonObject.get("source").getAsString();
        return source.split(",")[1];
    }

    private void addTexturesPathToJson(JsonObject parsedJson, String modelName) {
        JsonObject texturePathJson = new JsonObject();
        texturePathJson.addProperty("0", "item/" + modelName);
        parsedJson.add("textures", texturePathJson);
    }

    private String getAxisFromRotation(double[] rotation) {
        for (int i = 0; i < rotation.length; i++) {
            if (rotation[i] == 0) {
                continue;
            }
            return getAxisFromNumber(i);
        }
        return "";
    }

    private String getAxisFromNumber(int axis) {
        return switch (axis) {
            case 0 -> "x";
            case 1 -> "y";
            case 2 -> "z";
            default -> throw new IllegalStateException("Unexpected value: " + axis);
        };
    }

    private int getAxisNumber(String axis) {
        return switch (axis.toLowerCase()) {
            case "x" -> 0;
            case "y" -> 1;
            case "z" -> 2;
            default -> throw new IllegalStateException("Unexpected value: " + axis.toLowerCase());
        };
    }

    private String getNameWithoutExtension(String name) {
        if (name.indexOf(".") > 0) {
            return name.substring(0, name.lastIndexOf("."));
        } else {
            return name;
        }
    }
}
