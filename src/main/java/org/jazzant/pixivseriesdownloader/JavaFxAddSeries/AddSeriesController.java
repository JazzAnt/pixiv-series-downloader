package org.jazzant.pixivseriesdownloader.JavaFxAddSeries;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.jazzant.pixivseriesdownloader.Database.SeriesBroker;
import org.jazzant.pixivseriesdownloader.Downloader.ImageURLUtils;
import org.jazzant.pixivseriesdownloader.Parser.Parser;
import org.jazzant.pixivseriesdownloader.Parser.Series;
import org.jazzant.pixivseriesdownloader.Database.SeriesStatus;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AddSeriesController implements Initializable {
    private final String NO_GROUP_DIRECTORY = "{no group directory}";
    private final SeriesModel seriesModel = new SeriesModel();
    private boolean parsed = false;
    private Image missingThumbnailImage;

    private SeriesBroker broker;
    private Parser parser;

    @FXML protected Text parseButtonReport;
    @FXML protected TextField seriesUrlField;
    @FXML protected Button parseButton;

    @FXML protected GridPane parsedDetails;

    @FXML protected TextField dirGroupField;
    @FXML protected ComboBox<String> dirGroupComboBox;
    @FXML protected CheckBox dirGroupCheckBox;
    @FXML protected CheckBox dirGroupUseArtistCheckBox;

    @FXML protected TextField dirTitleField;
    @FXML protected CheckBox dirTitleCheckBox;
    @FXML protected CheckBox dirTitleUseArtistCheckBox;

    @FXML protected Text artistText;

    @FXML protected Text titleText;

    @FXML protected ImageView thumbnailImageView;

    @FXML protected Text dirLibraryText;
    @FXML protected HBox dirGroupHBox;
    @FXML protected Text dirGroupText;
    @FXML protected Text dirTitleText;
    @FXML protected Text dirTitleTextPadding;

    @FXML protected Button saveButton;

    public void setBroker(SeriesBroker broker){
        this.broker = broker;
        ObservableList<String> groupNames = FXCollections.observableArrayList(broker.selectAllGroups());
        groupNames.add(NO_GROUP_DIRECTORY);
        dirGroupComboBox.setItems(groupNames);
        dirGroupComboBox.getSelectionModel().selectLast();
    }

    public void setParser(Parser parser){
        this.parser = parser;
    }

    public void setLibraryDirectoryText(String libraryDirectory){
        dirLibraryText.setText(libraryDirectory);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        seriesModel.setPropertiesFromSeries(new Series());
        Bindings.bindBidirectional(dirGroupCheckBox.selectedProperty(), dirGroupUseArtistCheckBox.disableProperty());
        Bindings.bindBidirectional(dirGroupUseArtistCheckBox.selectedProperty(), dirGroupCheckBox.disableProperty());

        Bindings.bindBidirectional(dirTitleCheckBox.selectedProperty(), dirTitleUseArtistCheckBox.disableProperty());
        Bindings.bindBidirectional(dirTitleUseArtistCheckBox.selectedProperty(), dirTitleCheckBox.disableProperty());

        Bindings.bindBidirectional(dirGroupField.textProperty(), seriesModel.getDirectoryGroupProperty());
        Bindings.bindBidirectional(dirTitleField.textProperty(), seriesModel.getDirectoryTitleProperty());

        Bindings.bindBidirectional(titleText.textProperty(), seriesModel.getTitleProperty());
        Bindings.bindBidirectional(artistText.textProperty(), seriesModel.getArtistProperty());

        Bindings.bindBidirectional(dirGroupText.textProperty(), seriesModel.getDirectoryGroupProperty());
        Bindings.bindBidirectional(dirTitleText.textProperty(), seriesModel.getDirectoryTitleProperty());

        seriesModel.getOkToSaveProperty().bind(Bindings.createBooleanBinding(this::isDataValid,
                seriesModel.getDirectoryGroupProperty(),
                seriesModel.getDirectoryTitleProperty(),
                seriesModel.getTitleProperty(),
                seriesModel.getArtistProperty(),
                seriesModel.getStatusProperty(),
                seriesModel.getArtistIdProperty(),
                seriesModel.getSeriesIdProperty()));
        saveButton.disableProperty().bind(seriesModel.getOkToSaveProperty().not());

        dirLibraryText.setText("Library");
        toggleDisabilityOfDetails(true);
        toggleVisibilityOfDetails(false);

        dirGroupComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldvalue, newvalue)->{
            if(!dirGroupCheckBox.isSelected()){
                dirGroupText.setText(newvalue);
                seriesModel.setDirectoryGroup(newvalue);
                hideGroupDirDisplayIfNoGroupDirIsSelected();
            }
        });

        try{
            missingThumbnailImage = new Image(Objects.requireNonNull(
                    getClass().getResource("thumbnail_not_found.jpg")).openStream());
        } catch (IOException e) {
            missingThumbnailImage = null;
        }
    }

    @FXML
    protected void handleParseOrResetButton(ActionEvent actionEvent) {
        parseButton.setDisable(true);
        if(parsed) resetSeries();
        else handleParseButton();
        parseButton.setDisable(false);
    }

    @FXML
    protected void handleGroupCheckBox(ActionEvent actionEvent){
        if(dirGroupCheckBox.isSelected()){
            toggleGroupComboBox(false);
        } else {
            toggleGroupComboBox(true);
        }
    }

    private void toggleGroupComboBox(boolean isActive){
        dirGroupComboBox.setDisable(!isActive);
        dirGroupComboBox.setVisible(isActive);

        dirGroupField.setDisable(isActive);
        dirGroupField.setVisible(!isActive);

        if(isActive){
            dirGroupComboBox.getSelectionModel().selectLast();
            seriesModel.setDirectoryGroup(dirGroupComboBox.getValue());
        }
        hideGroupDirDisplayIfNoGroupDirIsSelected();
    }

    @FXML
    protected void handleGroupUseArtistCheckBox(ActionEvent actionEvent) {
        if(dirGroupUseArtistCheckBox.isSelected()){
            toggleGroupComboBox(false);
            dirGroupField.setDisable(true);
            seriesModel.setDirectoryGroup(seriesModel.getArtist());
        } else {
            toggleGroupComboBox(true);
        }
    }
    @FXML
    protected void handleTitleCheckBox(ActionEvent actionEvent){
        if(dirTitleCheckBox.isSelected()){
            dirTitleField.setDisable(false);
        } else {
            seriesModel.setDirectoryTitle(seriesModel.getTitle());
            dirTitleField.setDisable(true);
        }
    }
    @FXML
    protected void handleTitleUseArtistCheckBox(ActionEvent actionEvent) {
        if(dirTitleUseArtistCheckBox.isSelected()){
            seriesModel.setDirectoryTitle("["+seriesModel.getArtist()+"]"+seriesModel.getTitle());
            dirTitleField.setDisable(true);
        } else {
            seriesModel.setDirectoryTitle(seriesModel.getTitle());
            dirTitleField.setDisable(true);
        }
    }
    @FXML
    protected void handleSaveButton(ActionEvent actionEvent) {
        saveButton.disableProperty().unbind();
        saveButton.setDisable(true);
        String saveButtonText = saveButton.getText();
        saveButton.setText("Saving...");
        final Series series = seriesModel.getSeries();
        if(series.getDirectoryGroup().equals(NO_GROUP_DIRECTORY))
            series.setDirectoryGroup("");
        saveSeries(series, ()->{
            saveButton.setText(saveButtonText);
            saveButton.disableProperty().bind(seriesModel.getOkToSaveProperty().not());
        }, ()->{
            appendGroupNames(series.getDirectoryGroup());
        });
    }

    private void saveSeries(Series series, Runnable onSaveFinished, Runnable onSaveSuccess){
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return broker.createRecord(series);
            }
        };
        task.setOnSucceeded(event->{
            if(task.getValue()){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Success: Series Saved into the Database");
                alert.show();
                onSaveSuccess.run();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Fail: Something went wrong with saving the series to the database");
                alert.show();
            }
            onSaveFinished.run();
        });
        task.setOnFailed(event->{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(task.getException().getMessage());
            alert.show();
            onSaveFinished.run();
        });
        Thread thread = new Thread(task);
        thread.start();
    }

    private void hideGroupDirDisplayIfNoGroupDirIsSelected(){
        if(dirGroupComboBox.getValue().equals(NO_GROUP_DIRECTORY)
                && !dirGroupCheckBox.isSelected()
                && !dirGroupUseArtistCheckBox.isSelected()){
            dirGroupHBox.setVisible(false);
            dirGroupHBox.setManaged(false);
            dirTitleTextPadding.setVisible(false);
            dirTitleTextPadding.setManaged(false);
        }
        else {
            dirGroupHBox.setManaged(true);
            dirGroupHBox.setVisible(true);
            dirTitleTextPadding.setManaged(true);
            dirTitleTextPadding.setVisible(true);
        }
    }

    private void handleParseButton(){
        parseButton.setDisable(true);
        parseButton.setText("Parsing...");
        seriesUrlField.setDisable(true);
        removeUrlQueryString();
        final String seriesURL = seriesUrlField.getText();
        parseSeries(seriesURL, seriesModel,
                ()->{
            parseThumbnail();
            dirGroupCheckBox.setSelected(false);
            dirGroupUseArtistCheckBox.setSelected(false);
            dirTitleCheckBox.setSelected(false);
            dirTitleUseArtistCheckBox.setSelected(false);
            dirGroupComboBox.getSelectionModel().selectLast();
            toggleDisabilityOfDetails(false);
            toggleVisibilityOfDetails(true);
            hideGroupDirDisplayIfNoGroupDirIsSelected();
            dirTitleField.setDisable(true);
            parsed = true;
            parseButtonReport.setText("");
            parseButton.setText("Reset");
            parseButton.setDisable(false);
                },
                ()->{
            seriesUrlField.setDisable(false);
            parseButton.setText("Parse");
            parseButton.setDisable(false);
                });
    }

    private void removeUrlQueryString(){
        String url = seriesUrlField.getText();
        if(url.contains("?")){
            url = url.split("\\?")[0];
            seriesUrlField.setText(url);
        }
    }

    private void parseSeries(String seriesURL, SeriesModel seriesModel, Runnable onSucceed, Runnable onFailure) {
        Task<Series> task = new Task<Series>() {
            @Override
            protected Series call() throws Exception {
                return parser.parseSeries(seriesURL);
            }
        };
        task.setOnSucceeded(event -> {
            seriesModel.setPropertiesFromSeries(task.getValue());
            seriesModel.setDirectoryGroup(NO_GROUP_DIRECTORY);
            onSucceed.run();
        });
        task.setOnFailed(event -> {
            parseButtonReport.setText(task.getException().getMessage());
            onFailure.run();
        });
        Thread thread = new Thread(task);
        thread.start();
    }

    private void parseThumbnail(){
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String seriesThumbnailUrl = parser.parseSeriesThumbnail();
                InputStream inputStream = ImageURLUtils.getInputStreamFromImageURL(seriesThumbnailUrl);
                Image image = new Image(inputStream);
                thumbnailImageView.setImage(image);
                inputStream.close();
                return null;
            }
        };
        task.setOnFailed(event->{
            thumbnailImageView.setImage(missingThumbnailImage);
        });
        Thread thread = new Thread(task);
        thread.start();
    }

    private void resetSeries(){
        parseButton.setText("Parse");
        seriesUrlField.setDisable(false);
        toggleDisabilityOfDetails(true);
        toggleVisibilityOfDetails(false);
        parseButtonReport.setText("");
        parsed = false;
    }

    private void toggleDisabilityOfDetails(boolean isDisabled){
        dirGroupCheckBox.setDisable(isDisabled);
        dirGroupUseArtistCheckBox.setDisable(isDisabled);
        dirTitleField.setDisable(isDisabled);
        dirTitleCheckBox.setDisable(isDisabled);
        dirTitleUseArtistCheckBox.setDisable(isDisabled);
        if(dirGroupCheckBox.isSelected()){
            dirGroupField.setDisable(isDisabled);
        } else {
            dirGroupComboBox.setDisable(isDisabled);
        }
    }

    private void toggleVisibilityOfDetails(boolean isVisible){
        parsedDetails.setVisible(isVisible);
        parsedDetails.setManaged(isVisible);
        if(dirGroupCheckBox.isSelected()){
            dirGroupField.setVisible(isVisible);
        } else {
            dirGroupComboBox.setVisible(isVisible);
        }
    }
    private boolean isDataValid(){
        if(seriesModel.getDirectoryGroup().isBlank() ||
                seriesModel.getDirectoryTitle().isBlank() ||
                seriesModel.getTitle().isBlank() ||
                seriesModel.getArtist().isBlank() ||
                seriesModel.getStatus() < 0 || seriesModel.getStatus() >= SeriesStatus.values().length ||
                seriesModel.getArtistId() < 0 ||
                seriesModel.getSeriesId() < 0
        ) return false;
        return true;
    }

    private void appendGroupNames(String groupName){
        if(dirGroupComboBox.getItems().contains(groupName)) return;
        dirGroupComboBox.getItems().add(groupName);
    }
}
