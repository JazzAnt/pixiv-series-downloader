package org.jazzant.pixivseriesdownloader;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.jazzant.pixivseriesdownloader.Exceptions.SeriesAlreadyInDatabaseException;

import java.net.URL;
import java.util.ResourceBundle;

public class AddSeriesController implements Initializable {
    private final String NO_GROUP_DIRECTORY = "{no group directory}";
    private SeriesBroker broker;
    private SeriesModel seriesModel;
    private boolean parsed = false;

    @FXML protected Text parseButtonErrorText;
    @FXML protected TextField seriesUrlField;
    @FXML protected Button parseButton;

    @FXML protected Label dirGroupLabel;
    @FXML protected TextField dirGroupField;
    @FXML protected ComboBox<String> dirGroupComboBox;
    @FXML protected CheckBox createNewGroupCheckBox;
    @FXML protected Label dirGroupCheckLabel;

    @FXML protected Label dirTitleLabel;
    @FXML protected TextField dirTitleField;
    @FXML protected CheckBox useDefaultTitleCheckBox;
    @FXML protected Label dirTitleCheckLabel;

    @FXML protected Label artistLabel;
    @FXML protected Text artistText;

    @FXML protected Label titleLabel;
    @FXML protected Text titleText;

    @FXML protected Label directoryDisplayLabel;
    @FXML protected VBox directoryDisplay;
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
        dirGroupComboBox.getSelectionModel().selectFirst();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        seriesModel = new SeriesModel();
        Bindings.bindBidirectional(dirGroupField.textProperty(), seriesModel.getDirectoryGroupProperty());
        Bindings.bindBidirectional(dirTitleField.textProperty(), seriesModel.getDirectoryTitleProperty());

        Bindings.bindBidirectional(titleText.textProperty(), seriesModel.getTitleProperty());
        Bindings.bindBidirectional(artistText.textProperty(), seriesModel.getArtistProperty());

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

        dirLibraryText.setText(Downloader.getLibraryDir());
        toggleDisabilityOfDetails(true);
        toggleVisibilityOfDetails(false);

        dirGroupComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldvalue, newvalue)->{
            if(!createNewGroupCheckBox.isSelected()){
                dirGroupText.setText(newvalue);
                toggleGroupDirectoryDisplayVisibilityBasedOnComboBox();
            }
        });
    }

    @FXML
    protected void handleParseButton(ActionEvent actionEvent) {
        if(parsed) resetSeries();
        else parseSeries();
    }

    @FXML
    protected void handleGroupCheckBox(ActionEvent actionEvent){
        if(createNewGroupCheckBox.isSelected()){
            dirGroupComboBox.setDisable(true);
            dirGroupComboBox.setVisible(false);

            dirGroupField.setDisable(false);
            dirGroupField.setVisible(true);
            Bindings.bindBidirectional(dirGroupText.textProperty(), seriesModel.getDirectoryGroupProperty());
            toggleGroupDirectoryDisplayVisibilityBasedOnComboBox();

        } else {
            dirGroupComboBox.setDisable(false);
            dirGroupComboBox.setVisible(true);

            dirGroupField.setDisable(true);
            dirGroupField.setVisible(false);
            dirGroupHBox.setVisible(true);
            Bindings.unbindBidirectional(dirGroupText.textProperty(), seriesModel.getDirectoryGroupProperty());
            dirGroupText.setText(dirGroupComboBox.getValue());
            toggleGroupDirectoryDisplayVisibilityBasedOnComboBox();
        }
    }
    @FXML
    protected void handleTitleCheckBox(ActionEvent actionEvent){
        if(useDefaultTitleCheckBox.isSelected()){
            dirTitleField.setText(seriesModel.getTitle());
            dirTitleField.setDisable(true);
        } else {
            dirTitleField.setDisable(false);
        }
    }
    @FXML
    protected void handleSaveButton(ActionEvent actionEvent) {
        if(!createNewGroupCheckBox.isSelected()){
            String groupDirectory = dirGroupComboBox.getValue();
            if(groupDirectory.equals(NO_GROUP_DIRECTORY)){
                groupDirectory = "";
            }
            seriesModel.setDirectoryGroup(groupDirectory);
        }
        try {
            broker.createRecord(seriesModel.getSeries());
        }
        catch (SeriesAlreadyInDatabaseException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Fail: This Series is already in the database!");
            alert.show();
        }
    }

    private void toggleGroupDirectoryDisplayVisibilityBasedOnComboBox(){
        if(dirGroupComboBox.getValue().equals(NO_GROUP_DIRECTORY) && !createNewGroupCheckBox.isSelected()){
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

    private void parseSeries(){
        try {
            String seriesURL = seriesUrlField.getText();
            Series series = Parser.parseSeries(seriesURL);
            seriesModel.setPropertiesFromSeries(series);
            parsed = true;
            parseButtonErrorText.setText("");
            parseButton.setText("Reset");
            seriesUrlField.setDisable(true);
            toggleDisabilityOfDetails(false);
            toggleVisibilityOfDetails(true);
        } catch (ParserException e) {
            parseButtonErrorText.setText(e.getMessage());
        }
    }

    private void resetSeries(){
        parsed = false;
        parseButton.setText("Parse");
        seriesUrlField.setDisable(false);
        toggleDisabilityOfDetails(true);
        toggleVisibilityOfDetails(false);
    }

    private void toggleDisabilityOfDetails(boolean isDisabled){
        createNewGroupCheckBox.setDisable(isDisabled);
        dirTitleField.setDisable(isDisabled);
        useDefaultTitleCheckBox.setDisable(isDisabled);
        if(createNewGroupCheckBox.isSelected()){
            dirGroupField.setDisable(isDisabled);
        } else {
            dirGroupComboBox.setDisable(isDisabled);
        }
    }

    private void toggleVisibilityOfDetails(boolean isVisible){
        dirGroupLabel.setVisible(isVisible);
        dirGroupField.setVisible(isVisible);
        if(createNewGroupCheckBox.isSelected()){
            dirGroupField.setVisible(isVisible);
        } else {
            dirGroupComboBox.setVisible(isVisible);
        }
        createNewGroupCheckBox.setVisible(isVisible);
        dirGroupCheckLabel.setVisible(isVisible);

        dirTitleLabel.setVisible(isVisible);
        dirTitleField.setVisible(isVisible);
        useDefaultTitleCheckBox.setVisible(isVisible);
        dirTitleCheckLabel.setVisible(isVisible);

        titleLabel.setVisible(isVisible);
        titleText.setVisible(isVisible);
        artistLabel.setVisible(isVisible);
        artistText.setVisible(isVisible);

        directoryDisplayLabel.setVisible(isVisible);
        directoryDisplay.setVisible(isVisible);
        saveButton.setVisible(isVisible);
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
}
