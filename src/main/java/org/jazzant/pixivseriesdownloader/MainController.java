package org.jazzant.pixivseriesdownloader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private final SeriesBroker broker = new SeriesBroker();
    @FXML
    protected Button databaseButton;
    @FXML
    protected Button addSeriesButton;
    @FXML
    protected Button loginButton;
    @FXML
    protected Text loginDisplay;

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

    @FXML
    private void openLoginWindow(){
        loginButton.setDisable(true);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);

            stage.setTitle("Login View");
            stage.setOnCloseRequest(windowEvent -> {
                if(Parser.isLoggedIn()){
                    loginButton.setText("Logged In To Pixiv");
                    loginButton.setVisible(false);
                    loginDisplay.setVisible(true);
                } else {
                    loginButton.setDisable(false);
                }
            });
            stage.setScene(scene);
            stage.show();
        } catch (IOException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Something went wrong: " + e.getMessage());
            alert.show();
            loginButton.setDisable(false);
        }
    }

    @FXML
    public void openDatabaseViewerWindow(ActionEvent actionEvent) {
        databaseButton.setDisable(true);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("database-view.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);

            DatabaseViewerController controller = fxmlLoader.getController();
            controller.populateTree(broker.selectAllGroups(), broker.selectAll());

            stage.setTitle("Database View");
            stage.setOnCloseRequest(windowEvent -> {
                databaseButton.setDisable(false);
            });
            stage.setScene(scene);
            stage.show();
        } catch (IOException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Something went wrong: " + e.getMessage());
            alert.show();
            databaseButton.setDisable(false);
        }
    }
}
