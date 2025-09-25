package org.jazzant.pixivseriesdownloader.JavaFxLogin;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jazzant.pixivseriesdownloader.JavaFxConfig.ConfigManager;
import org.jazzant.pixivseriesdownloader.Parser.Parser;
import org.jazzant.pixivseriesdownloader.Parser.ParserReCaptchaException;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    private Parser parser;
    private BooleanBinding credentialsFilledProperty;
    @FXML
    protected VBox scenePane;
    @FXML
    protected TextField usernameField;
    @FXML
    protected PasswordField passwordField;
    @FXML
    protected Label stayLoggedInLabel;
    @FXML
    protected CheckBox stayLoggedInCheckBox;
    @FXML
    protected Button loginButton;
    @FXML
    protected Button loginManuallyButton;
    @FXML
    protected Label loginManuallyLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Tooltip tooltip = new Tooltip("Opens the Browser and Attempts to Login Manually. \n" +
                "Note that due to limitations, the browser must be kept open for the rest of the session");
        Tooltip.install(loginManuallyLabel, tooltip);

        credentialsFilledProperty = Bindings.createBooleanBinding(this::isCredentialsInputted,
                usernameField.textProperty(),
                passwordField.textProperty()
        );
        loginButton.disableProperty().bind(credentialsFilledProperty.not());
    }

    public void setParser(Parser parser){
        this.parser = parser;
    }

    @FXML
    protected void handleLoginButton(){
        login(false);
    }

    @FXML
    protected void handleLoginManuallyButton(){
        if(parser.isHeadless()){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("This will bring out the firefox browser that the app uses to the front for you to log-in manually. " +
                    "Due to technical limitations the browser will need to be kept open for the remainder of this session (it can be minimized but not closed). " +
                    "Open the browser?");
            alert.showAndWait();
            if (alert.getResult() == ButtonType.OK) login(true);
        }
        else login(true);
    }

    private void login(boolean loginManually){
        loginButton.disableProperty().unbind();
        toggleElementsDisability(true);
        if(loginManually){
            loginManually(this::handleLoginAttempt);
        } else {
            final String username = usernameField.getText().trim();
            final String password = passwordField.getText().trim();
            loginAutomatically(username, password, this::handleLoginAttempt);
        }
    }

    private void handleLoginAttempt(){
        if(parser.isLoggedIn()){
            if(stayLoggedInCheckBox.isSelected()) {
                parser.getLoginCookieFromBrowser();
                parser.saveLoginCookieToFile();
            }
            closeWindow();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Login Unsuccessful");
            alert.show();
            toggleElementsDisability(false);
            loginButton.disableProperty().bind(credentialsFilledProperty.not());
        }
    }

    private void loginAutomatically(String username, String password, Runnable onLoginAttemptFinished){
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                parser.loginPixiv(username, password);
                return null;
            }
        };
        task.setOnSucceeded(event->{
            onLoginAttemptFinished.run();
        });
        task.setOnFailed(event->{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            if(task.getException() instanceof ParserReCaptchaException){
                alert.setContentText("Login failed due to reCAPTCHA. \nTry again after a while or use manual login.");
            } else {
                alert.setContentText(task.getException().getMessage());
            }
            alert.show();
            onLoginAttemptFinished.run();
        });
        Thread thread = new Thread(task);
        thread.start();
    }

    private void loginManually(Runnable onLoginAttemptFinished){
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                parser.loginPixivManually();
                return null;
            }
        };
        task.setOnSucceeded(event->{
            onLoginAttemptFinished.run();
        });
        Thread thread = new Thread(task);
        thread.start();
    }

    private void toggleElementsDisability(boolean isDisabled){
        usernameField.setDisable(isDisabled);
        passwordField.setDisable(isDisabled);
        stayLoggedInCheckBox.setDisable(isDisabled);
        loginButton.setDisable(isDisabled);
        loginManuallyButton.setDisable(isDisabled);
    }

    private void closeWindow(){
        Stage stage = (Stage) scenePane.getScene().getWindow();
        stage.close();
    }

    private boolean isCredentialsInputted(){
        return !usernameField.getText().isBlank() && !passwordField.getText().isBlank();
    }
}
