package org.jazzant.pixivseriesdownloader;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private final SeriesBroker broker = new SeriesBroker();
    @FXML
    protected Button addSeriesButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    private void openAddSeriesWindow(){
        addSeriesButton.setDisable(true);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("add-series-view.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);

            AddSeriesController addSeriesController = fxmlLoader.getController();
            addSeriesController.setBroker(broker);

            stage.setTitle("Add Series View");
            stage.setOnCloseRequest(windowEvent -> {addSeriesButton.setDisable(false);});
            stage.setScene(scene);
            stage.show();
        } catch (IOException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Something went wrong: " + e.getMessage());
            alert.show();
            addSeriesButton.setDisable(false);
        }
    }
}
