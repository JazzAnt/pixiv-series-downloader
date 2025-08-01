package org.jazzant.pixivseriesdownloader;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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
    private ArrayList<Series> seriesList;
    private ArrayList<String> groupList;

    @FXML protected TreeView<SeriesTreeItem> treeView;
    @FXML protected TableView<Series> tableView;
    @FXML protected Button toggleButton;

    @FXML protected VBox buttonsView;
    @FXML protected GridPane detailsView;
    @FXML protected Text titleTxt;
    @FXML protected Text artistTxt;
    @FXML protected Text statusTxt;
    @FXML protected Text linkTxt;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isLeaf()) {
                Platform.runLater(() -> treeView.getSelectionModel().clearSelection());
            }
        });
    }

    public void fetchSeriesFromDatabase(){
        seriesList = broker.selectAll();
        groupList = broker.selectAllGroups();
    }

    @FXML
    public void toggleView(){
        tableView.setVisible(!tableView.isVisible());
        tableView.setManaged(!tableView.isManaged());
        treeView.setVisible(!treeView.isVisible());
        treeView.setManaged(!treeView.isManaged());
        if(tableView.isManaged()) toggleButton.setText("Table View");
        else toggleButton.setText("Tree View");
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
    public void handleDeleteSeries(){
        if(selectedSeries == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Are you sure you want to delete「" + selectedSeries.getTitle() + "」from the database?\n" +
                "(this won't delete the files in the library folder)");
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> {
                    deleteSeries(selectedSeries.getSeriesID());
                });
    }

    private void deleteSeries(int seriesId){
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return broker.deleteRecord(seriesId);
            }
        };
        task.setOnSucceeded(event-> {
            if(task.getValue()){
                seriesList.remove(selectedSeries);
                deselectSeries();
                populateDBViewers();
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Something went wrong with deleting the series");
                alert.show();
            }
        });
        Thread thread = new Thread(task);
        thread.start();
    }

    @FXML
    public void handleUpdateStatus(){
        if(selectedSeries == null) return;
        SeriesStatus[] status = {SeriesStatus.ONGOING, SeriesStatus.COMPLETED, SeriesStatus.HIATUS, SeriesStatus.PAUSED, SeriesStatus.DELETED};
        ChoiceDialog<SeriesStatus> dialog = new ChoiceDialog<>(selectedSeries.getStatus(), status);
        dialog.setContentText("Change the status of the series to:");
        dialog.setHeaderText("Change Series Status");
        dialog.showAndWait()
                .ifPresent(response -> {
                    updateStatus(selectedSeries, response);
                });
    }

    private void updateStatus(Series series, SeriesStatus status){
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return broker.updateRecordStatus(series.getSeriesID(), status);
            }
        };
        task.setOnSucceeded(event-> {
            if(task.getValue()){
                int index = seriesList.indexOf(series);
                series.setStatus(status);
                seriesList.set(index, series);
                selectSeries(series);
                populateDBViewers();
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Something went wrong with updating the series");
                alert.show();
            }
        });
        Thread thread = new Thread(task);
        thread.start();
    }

    public void selectTableItem(){
        if(tableView.getSelectionModel().isEmpty()) return;
        Series series = tableView.getSelectionModel().getSelectedItem();
        selectSeries(series);
    }

    public void selectTreeItem(){
        TreeItem<SeriesTreeItem> item = treeView.getSelectionModel().getSelectedItem();
        if(item == null || !item.getValue().isSeries()) return;
        Series series = item.getValue().getSeries();
        selectSeries(series);
    }

    public void selectSeries(Series series){
        detailsView.setVisible(true);
        buttonsView.setVisible(true);

        titleTxt.setText(series.getTitle());
        artistTxt.setText(series.getArtist());
        statusTxt.setText(series.getStatus().toString());
        linkTxt.setText(series.getSeriesURL());
        selectedSeries = series;
    }

    public void deselectSeries(){
        detailsView.setVisible(false);
        buttonsView.setVisible(false);

        selectedSeries = null;
    }

    public void setLibraryName(String libraryName){
        this.libraryName = libraryName;
    }

    public void setBroker(SeriesBroker broker){
        this.broker = broker;
    }

    public void populateDBViewers(){
        populateTree();
        populateTable();
    }

    public void populateTree(){
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
        root.setExpanded(true);
        treeView.setRoot(root);
    }

    public void populateTable(){
        ObservableList<Series> list = FXCollections.observableArrayList(seriesList);
        tableView.setItems(list);
        tableView.refresh();
    }

    public void setupTableColumns(){
        TableColumn<Series, String> groupColumn = new TableColumn<>("Group");
        groupColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Series, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Series, String> series) {
                String group = series.getValue().getDirectoryGroup();
                if(group.isBlank()) group = "{no group}";
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

        tableView.getColumns().setAll(groupColumn, titleColumn, artistColumn, statusColumn);
    }
}
