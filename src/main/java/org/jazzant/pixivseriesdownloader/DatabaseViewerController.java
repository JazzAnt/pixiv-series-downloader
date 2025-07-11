package org.jazzant.pixivseriesdownloader;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DatabaseViewerController implements Initializable {
    @FXML protected Label testingLabel;
    @FXML
    protected TreeView<String> treeView;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isLeaf()) {
                Platform.runLater(() -> treeView.getSelectionModel().clearSelection());
            }
        });
    }

    public void selectItem(){
        TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
        if(item != null) {
            testingLabel.setText(item.getValue());
        }
    }

    public void populateTree(String libraryName, ArrayList<String> groupList, ArrayList<Series> seriesList){
        TreeItem<String> root = new TreeItem<>(libraryName);

        for(String group : groupList){
            root.getChildren().add(new TreeItem<>(group));
        }

        for(Series series : seriesList){
            if(series.getDirectoryGroup().isBlank()){
                root.getChildren().add(new TreeItem<>(series.getDirectoryTitle()));
                continue;
            }
            for(TreeItem<String> group : root.getChildren()){
                if(group.getValue().equals(series.getDirectoryGroup())){
                    group.getChildren().add(new TreeItem<>(series.getDirectoryTitle()));
                    break;
                }
            }
        }
        treeView.setRoot(root);
    }
}
