package org.jazzant.pixivseriesdownloader.JavaFxMain;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.jazzant.pixivseriesdownloader.Database.SeriesBroker;
import org.jazzant.pixivseriesdownloader.Downloader.Downloader;
import org.jazzant.pixivseriesdownloader.Downloader.SaveAs;
import org.jazzant.pixivseriesdownloader.FilePath;
import org.jazzant.pixivseriesdownloader.JavaFxAddSeries.AddSeriesController;
import org.jazzant.pixivseriesdownloader.JavaFxConfig.ConfigController;
import org.jazzant.pixivseriesdownloader.JavaFxConfig.ConfigManager;
import org.jazzant.pixivseriesdownloader.JavaFxDbViewer.DatabaseViewerController;
import org.jazzant.pixivseriesdownloader.JavaFxDownload.DownloadController;
import org.jazzant.pixivseriesdownloader.JavaFxLogin.LoginController;
import org.jazzant.pixivseriesdownloader.Parser.Parser;

import java.io.IOException;

public class MainController {
    private SeriesBroker broker;
    private Parser parser;
    private Downloader downloader;
    private ConfigManager configManager;
    @FXML
    protected Button downloadButton;
    @FXML
    protected Button databaseButton;
    @FXML
    protected Button addSeriesButton;
    @FXML
    protected Button loginButton;
    @FXML
    protected Text loginDisplay;
    @FXML
    protected Button configButton;

    public void setBroker(SeriesBroker broker){
        this.broker = broker;
    }
    public void setParser(Parser parser){
        this.parser = parser;
    }
    public void setDownloader(Downloader downloader){
        this.downloader = downloader;
    }
    public void setConfigManager(ConfigManager configManager) {this.configManager = configManager;}

    @FXML
    private void openDownloadWindow(){
        downloadButton.setDisable(true);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FilePath.RESOURCE + "download-view.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);

            DownloadController controller = fxmlLoader.getController();
            controller.setParser(parser);
            controller.setDownloader(downloader);
            controller.setBroker(broker);

            stage.setTitle("Download View");
            stage.setOnCloseRequest(windowEvent -> {
                downloadButton.setDisable(false);
            });
            stage.setScene(scene);
            stage.show();
        } catch (IOException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Something went wrong: " + e.getMessage());
            alert.show();
            downloadButton.setDisable(false);
        }
    }

    @FXML
    private void openAddSeriesWindow(){
        addSeriesButton.setDisable(true);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FilePath.RESOURCE + "add-series-view.fxml"));
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FilePath.RESOURCE + "login-view.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);

            LoginController controller = fxmlLoader.getController();
            controller.setParser(parser);
            controller.setConfigManager(configManager);
            controller.getSavedCredentials();

            stage.setTitle("Login View");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Something went wrong: " + e.getMessage());
            alert.show();
            loginButton.setDisable(false);
        }
        updateLoginButton();
    }

    public void updateLoginButton(){
        boolean isLoggedIn = parser.isLoggedIn();
        loginButton.setDisable(isLoggedIn);
        loginButton.setVisible(!isLoggedIn);
        loginButton.setManaged(!isLoggedIn);

        loginDisplay.setVisible(isLoggedIn);
        loginDisplay.setManaged(isLoggedIn);
    }

    @FXML
    public void openDatabaseViewerWindow(ActionEvent actionEvent) {
        databaseButton.setDisable(true);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FilePath.RESOURCE + "database-view.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);

            DatabaseViewerController controller = fxmlLoader.getController();
            controller.setLibraryName(downloader.getLibraryDir());
            controller.setBroker(broker);
            controller.fetchSeriesFromDatabase();
            controller.setupTableColumns();
            controller.populateTable();

            stage.setTitle("Database View");
            stage.setHeight(450);
            stage.setWidth(950);
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

    @FXML
    protected void openConfigWindow(){
        configButton.setDisable(true);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FilePath.RESOURCE + "config-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            ConfigController controller = loader.getController();
            controller.setConfigManager(configManager);
            controller.setLibrary(downloader.getLibraryDir());
            controller.setFilenameFormat(downloader.getFilenameFormatter());
            controller.setComboBoxSelection(downloader.getFileFormat());
            controller.setParser(parser);
            controller.updateLoginCookieButton();
            controller.setInfoText("Note: Changing the config files won't modify existing files that have been downloaded. " +
                    "If you want to change the library directory or file format, you'll need to either manually move the files " +
                    "or redownload everything.");

            controller.setOnSaveMessage("Reminder: you'll need to either manually move the existing files or redownload everything");

            Stage stage = new Stage();

            stage.setTitle("First Time User Configuration");
            stage.setScene(scene);
            stage.showAndWait();

            String libraryDir = configManager.getProperty(configManager.KEY_LIBRARY);
            SaveAs saveAs = SaveAs.valueOf(configManager.getProperty(configManager.KEY_SAVEAS));
            String filenameFormat = configManager.getProperty(configManager.KEY_FILENAME_FORMAT);
            downloader.setLibraryDir(libraryDir);
            downloader.setFileFormat(saveAs);
            downloader.setFilenameFormat(filenameFormat);
        }
        catch (IOException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Something went wrong: " + e.getMessage());
            alert.show();
            configButton.setDisable(false);
        }
        configButton.setDisable(false);
        updateLoginButton();
    }
}
