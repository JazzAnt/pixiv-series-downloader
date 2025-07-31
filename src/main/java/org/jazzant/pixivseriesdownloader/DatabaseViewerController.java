package org.jazzant.pixivseriesdownloader;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DatabaseViewerController implements Initializable {
    private String libraryName;
    private SeriesBroker broker;
    private Series selectedSeries;

    @FXML protected GridPane detailsView;
    @FXML protected Text titleTxt;
    @FXML protected Text artistTxt;
    @FXML protected Text statusTxt;
    @FXML protected Text linkTxt;
    @FXML
    protected TreeView<SeriesTreeItem> treeView;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isLeaf()) {
                Platform.runLater(() -> treeView.getSelectionModel().clearSelection());
            }
        });
    }

    public void selectItem(){
        TreeItem<SeriesTreeItem> item = treeView.getSelectionModel().getSelectedItem();
        if(item != null && item.getValue().isSeries()) {
            Series series = item.getValue().getSeries();
            detailsView.setVisible(true);
            titleTxt.setText(series.getTitle());
            artistTxt.setText(series.getArtist());
            statusTxt.setText(series.getStatus().toString());
            linkTxt.setText(series.getSeriesURL());
            selectedSeries = series;
        }
        else {
            detailsView.setVisible(false);
            selectedSeries = null;
        }
    }

    public void setLibraryName(String libraryName){
        this.libraryName = libraryName;
    }

    public void setBroker(SeriesBroker broker){
        this.broker = broker;
    }

    public void populateTree(){
        ArrayList<String> groupList = broker.selectAllGroups();
        ArrayList<Series> seriesList = broker.selectAll();

        TreeItem<SeriesTreeItem> root = new TreeItem<>(new SeriesTreeItem(libraryName));

        for(String group : groupList){
            root.getChildren().add(new TreeItem<>(new SeriesTreeItem(group)));
        }

        for(Series series : seriesList){
            if(series.getDirectoryGroup().isBlank()){
                root.getChildren().add(new TreeItem<>(new SeriesTreeItem(series)));
                continue;
            }
            for(TreeItem<SeriesTreeItem> group : root.getChildren()){
                if(group.getValue().toString().equals(series.getDirectoryGroup())){
                    group.getChildren().add(new TreeItem<>(new SeriesTreeItem(series)));
                    break;
                }
            }
        }
        treeView.setRoot(root);
    }
}
