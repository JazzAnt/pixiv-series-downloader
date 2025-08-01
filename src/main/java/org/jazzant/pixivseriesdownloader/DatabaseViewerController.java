package org.jazzant.pixivseriesdownloader;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.beans.EventHandler;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DatabaseViewerController implements Initializable {
    private String libraryName;
    private SeriesBroker broker;
    private Series selectedSeries;

    @FXML protected VBox buttonsView;
    @FXML protected GridPane detailsView;
    @FXML protected Text titleTxt;
    @FXML protected Text artistTxt;
    @FXML protected Text statusTxt;
    @FXML protected Text linkTxt;
    @FXML
    protected TreeView<SeriesTreeItem> treeView;
    @FXML protected TableView<Series> tableView;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isLeaf()) {
                Platform.runLater(() -> treeView.getSelectionModel().clearSelection());
            }
        });
    }

    @FXML
    public void copyLinkToClipboard(){
        if(selectedSeries == null) return;
        StringSelection selection = new StringSelection(selectedSeries.getSeriesURL());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }

    @FXML
    public void openLinkInBrowser(){
        if(selectedSeries == null) return;
        if(!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Your computer doesn't support opening links to browsers");
            alert.show();
        }
        try{
            Desktop.getDesktop().browse(new URI(selectedSeries.getSeriesURL()));
        } catch (IOException | URISyntaxException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Something went wrong with trying to open link with browser:\n" + e.getMessage());
            alert.show();
        }
    }

    @FXML
    public void deleteSeries(){
        if(selectedSeries == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Are you sure you want to delete「" + selectedSeries.getTitle() + "」from the database?\n" +
                "(this won't delete the files in the library folder)");
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> {
                    if(broker.deleteRecord(selectedSeries.getSeriesID())){
                        populateTree();
                    }
                    else {
                        Alert alert1 = new Alert(Alert.AlertType.ERROR);
                        alert1.setContentText("Something went wrong with deleting the series");
                        alert1.show();
                    }
                });
    }

    @FXML
    public void changeStatus(){
        if(selectedSeries == null) return;
        SeriesStatus[] status = {SeriesStatus.ONGOING, SeriesStatus.COMPLETED, SeriesStatus.HIATUS, SeriesStatus.PAUSED, SeriesStatus.DELETED};
        ChoiceDialog<SeriesStatus> dialog = new ChoiceDialog<>(selectedSeries.getStatus(), status);
        dialog.setContentText("Change the status of the series to:");
        dialog.setHeaderText("Change Series Status");
        dialog.showAndWait()
                .ifPresent(response -> {
                    if(broker.updateRecordStatus(selectedSeries.getSeriesID(), response)){
                        populateTree();
                    }
                    else {
                        Alert alert1 = new Alert(Alert.AlertType.ERROR);
                        alert1.setContentText("Something went wrong with updating the series");
                        alert1.show();
                    }
                });
    }

    public void selectItem(){
        TreeItem<SeriesTreeItem> item = treeView.getSelectionModel().getSelectedItem();
        if(item != null && item.getValue().isSeries()) {
            Series series = item.getValue().getSeries();
            detailsView.setVisible(true);
            buttonsView.setVisible(true);

            titleTxt.setText(series.getTitle());
            artistTxt.setText(series.getArtist());
            statusTxt.setText(series.getStatus().toString());
            linkTxt.setText(series.getSeriesURL());
            selectedSeries = series;
        }
        else {
            detailsView.setVisible(false);
            buttonsView.setVisible(false);
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

    public void populateTable(){
        ObservableList<Series> list = FXCollections.observableArrayList(broker.selectAll());
        tableView.setItems(list);
    }

    public void setupTableColumns(){
        TableColumn<Series, String> groupColumn = new TableColumn<>("Group");
        groupColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Series, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Series, String> series) {
                String group = series.getValue().getDirectoryGroup();
                if(group.isBlank()) group = "{no group directory}";
                return new ReadOnlyObjectWrapper<>(group);
            }
        });

        TableColumn<Series, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Series, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Series, String> series) {
                return new ReadOnlyObjectWrapper<>(series.getValue().getTitle());
            }
        });

        TableColumn<Series, String> artistColumn = new TableColumn<>("Artist");
        artistColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Series, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Series, String> series) {
                return new ReadOnlyObjectWrapper<>(series.getValue().getArtist());
            }
        });

        TableColumn<Series, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Series, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Series, String> series) {
                return new ReadOnlyObjectWrapper<>(series.getValue().getStatus().toString());
            }
        });

        TableColumn<Series, String> linkColumn = new TableColumn<>("Pixiv Link");
        linkColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Series, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Series, String> series) {
                return new ReadOnlyObjectWrapper<>(series.getValue().getSeriesURL());
            }
        });

        tableView.getColumns().setAll(groupColumn, titleColumn, artistColumn, statusColumn, linkColumn);
    }
}
