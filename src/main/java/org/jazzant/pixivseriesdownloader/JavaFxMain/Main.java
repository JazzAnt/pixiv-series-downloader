package org.jazzant.pixivseriesdownloader.JavaFxMain;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.jazzant.pixivseriesdownloader.Database.SeriesBroker;
import org.jazzant.pixivseriesdownloader.Downloader.Downloader;
import org.jazzant.pixivseriesdownloader.Downloader.SaveAs;
import org.jazzant.pixivseriesdownloader.FilePath;
import org.jazzant.pixivseriesdownloader.JavaFxConfig.ConfigController;
import org.jazzant.pixivseriesdownloader.JavaFxConfig.ConfigManager;
import org.jazzant.pixivseriesdownloader.Parser.Parser;

import java.io.File;
import java.io.IOException;

public class Main extends Application {
    private SeriesBroker broker;
    private final Parser parser = new Parser();
    private final Downloader downloader = new Downloader();
    private final ConfigManager configManager = new ConfigManager();
    public static void main(String[] args) {launch(args);}

    @Override
    public void start(Stage stage) throws IOException {
        createDataFolder();
        if(!configManager.configIsValid()) createConfig();
        broker = new SeriesBroker();

        String libraryDir = configManager.getProperty(configManager.KEY_LIBRARY);
        SaveAs saveAs = SaveAs.valueOf(configManager.getProperty(configManager.KEY_SAVEAS));
        String filenameFormat = configManager.getProperty(configManager.KEY_FILENAME_FORMAT);
        downloader.setLibraryDir(libraryDir);
        downloader.setFileFormat(saveAs);
        downloader.setFilenameFormat(filenameFormat);

        stage.setOnCloseRequest(windowEvent -> {
            parser.quit();
            Platform.exit();
        });

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FilePath.RESOURCE_FOLDER + "main-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);

        MainController controller = fxmlLoader.getController();
        controller.setBroker(broker);
        controller.setParser(parser);
        controller.setDownloader(downloader);
        controller.setConfigManager(configManager);
        controller.updateLoginButton();

        if(parser.isLoginCookieExpired()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Your login session has expired, please login again.");
            alert.showAndWait();
        }

        stage.setTitle("Pixiv Series Downloader");
        stage.setScene(scene);
        stage.show();
    }

    private void createDataFolder() throws IOException {
        File folder = new File(FilePath.DATA_FOLDER.getPath());
        folder.mkdir();
    }

    private void createConfig() throws IOException {
        configManager.createConfigFile();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(FilePath.RESOURCE_FOLDER + "config-view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        ConfigController controller = loader.getController();
        controller.setConfigManager(configManager);
        controller.setLibrary(System.getProperty("user.home") + "/Library");
        controller.setInfoText("Welcome to Pixiv Series Manager! " +
                "Please enter the following settings for the program to work. " +
                "(these settings can be changed later but they won't change " +
                "any existing files you've already downloaded)");
        controller.setFilenameFormatToDefault();

        Stage stage = new Stage();
        stage.setOnCloseRequest(windowEvent -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Program Cannot Run Without the Config Being Set!");
            alert.showAndWait();
            parser.quit();
            Platform.exit();
        });
        stage.setTitle("First Time User Configuration");
        stage.setScene(scene);
        stage.showAndWait();
    }
}
