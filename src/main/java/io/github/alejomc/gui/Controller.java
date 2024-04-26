package io.github.alejomc.gui;

import io.github.alejomc.resourcepack.Parser;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class Controller {

    private final Parser parser;
    private File outputFolder;
    private File sourceFolder;
    public Controller(Parser parser) {
        this.parser = parser;
    }

    public void selectSourceFolder() {
        this.sourceFolder = openFileChooser();
    }

    public void selectOutputFolder() {
        this.outputFolder = openFileChooser();
    }

    private File openFileChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        return directoryChooser.showDialog(null);
    }

    public void startParse() {
        if (this.sourceFolder == null || outputFolder == null) {
            return;
        }
        parser.parseFolderToFolder(this.sourceFolder, this.outputFolder);
    }
}
