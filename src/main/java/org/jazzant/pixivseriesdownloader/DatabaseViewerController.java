package org.jazzant.pixivseriesdownloader;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DatabaseViewerController implements Initializable {
    @FXML
    protected TreeView<String> treeView;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    private void populateTree(TreeItem<String> treeView, ArrayList<String> groupList, ArrayList<Series> seriesList){
        String root = Downloader.getLibraryDir();


    }

    private TreeItem<String> createTreeItem(String directory){
        return new TreeItem<>(directory);
    }
}
