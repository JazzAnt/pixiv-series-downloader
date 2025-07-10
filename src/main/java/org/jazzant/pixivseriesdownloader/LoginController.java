package org.jazzant.pixivseriesdownloader;

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

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    private BooleanBinding credentialsFilledProperty;
    @FXML
    protected VBox scenePane;
    @FXML
    protected TextField usernameField;
    @FXML
    protected PasswordField passwordField;
    @FXML
    protected CheckBox saveCredentialCheckBox;
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
        if(Parser.isLoggedIn()){
            if(saveCredentialCheckBox.isSelected()) saveCredentials();
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
                Parser.loginPixiv(username, password);
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
                Parser.loginPixivManually();
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
        loginButton.setDisable(isDisabled);
        loginManuallyButton.setDisable(isDisabled);
    }

    private void saveCredentials(){
        if(isCredentialsInputted()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("TODO: replace this with saving credentials");
            alert.show();
        }
    }

    private void closeWindow(){
        Stage stage = (Stage) scenePane.getScene().getWindow();
        stage.close();
    }

    private boolean isCredentialsInputted(){
        if(usernameField.getText().isBlank() || passwordField.getText().isBlank()) return false;
        return true;
    }
}
