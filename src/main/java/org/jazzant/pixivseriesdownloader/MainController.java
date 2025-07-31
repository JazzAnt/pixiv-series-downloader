package org.jazzant.pixivseriesdownloader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    private SeriesBroker broker;
    private Parser parser;
    private Downloader downloader;
    @FXML
    protected Button databaseButton;
    @FXML
    protected Button addSeriesButton;
    @FXML
    protected Button loginButton;
    @FXML
    protected Text loginDisplay;

    public void setBroker(SeriesBroker broker){
        this.broker = broker;
    }
    public void setParser(Parser parser){
        this.parser = parser;
    }
    public void setDownloader(Downloader downloader){
        this.downloader = downloader;
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
            addSeriesController.setParser(parser);
            addSeriesController.setLibraryDirectoryText(downloader.getLibraryDir());

            stage.setTitle("Add Series View");
            stage.setOnCloseRequest(windowEvent -> {
                addSeriesButton.setDisable(false);
            });
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

            LoginController controller = fxmlLoader.getController();
            controller.setParser(parser);

            stage.setTitle("Login View");
            stage.setOnCloseRequest(windowEvent -> {
                if(parser.isLoggedIn()){
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
            controller.setLibraryName(downloader.getLibraryDir());
            controller.setBroker(broker);
            controller.populateTree();

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
