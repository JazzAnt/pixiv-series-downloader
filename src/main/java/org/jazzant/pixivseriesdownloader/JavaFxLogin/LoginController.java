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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Tooltip tooltip = new Tooltip("Saves the login cookie as a file called 'loginCookie.ser'. \n" +
                "Don't share the file with anyone!");
        Tooltip.install(stayLoggedInCheckBox, tooltip);
        Tooltip.install(stayLoggedInLabel, tooltip);

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
        login(true);
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
                alert.setContentText("Login failed due to reCAPTCHA. \nTry logging in using the browser.");
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
