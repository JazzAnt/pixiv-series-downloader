package org.jazzant.pixivseriesdownloader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ConfigController implements Initializable {
    private ConfigManager configManager;
    private String onSaveMessage;
    private boolean onSaveAlert = false;

    @FXML
    protected VBox scenePane;
    @FXML
    protected Text infoText;
    @FXML
    protected TextField libraryField;
    @FXML
    protected ComboBox<SaveAs> saveComboBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<SaveAs> saveAsList = FXCollections.observableArrayList(
                Arrays.asList(SaveAs.values())
        );
        saveComboBox.setItems(saveAsList);
        saveComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    protected void directoryPicker(){
        DirectoryChooser chooser = new DirectoryChooser();
        Stage stage = new Stage();
        File directory = chooser.showDialog(stage);
        if(directory == null){
            libraryField.setText("");
        }
        else {
            libraryField.setText(directory.getAbsolutePath());
        }
    }

    @FXML
    protected void saveConfig(){
        String libraryDirectory = libraryField.getText().trim();

        Path path = Paths.get(libraryDirectory);

        if(!Files.exists(path) || !Files.isDirectory(path)){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Invalid Directory!");
            alert.show();
            return;
        }

        configManager.setProperty(configManager.KEY_LIBRARY, libraryDirectory);
        configManager.setProperty(configManager.KEY_SAVEAS, saveComboBox.getValue().toString());

        if(onSaveAlert){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText(onSaveMessage);
            alert.show();
            onSaveAlert = false;
        }

        Stage stage = (Stage) scenePane.getScene().getWindow();
        stage.close();
    }

    public void setOnSaveMessage(String onSaveMessage){
        this.onSaveMessage = onSaveMessage;
        onSaveAlert = true;
    }

    public void setConfigManager(ConfigManager manager){
        configManager = manager;
    }

    public void setInfoText(String information){infoText.setText(information);}

    public void setLibrary(String libraryDirectory){
        libraryField.setText(libraryDirectory);
    }

    public void setComboBoxSelection(SaveAs saveAs){
        List<SaveAs> saveAsArrayList = Arrays.asList(SaveAs.values());
        int index = saveAsArrayList.indexOf(saveAs);
        saveComboBox.getSelectionModel().select(index);

    }
}
