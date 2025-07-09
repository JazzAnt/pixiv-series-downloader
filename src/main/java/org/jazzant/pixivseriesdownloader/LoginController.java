package org.jazzant.pixivseriesdownloader;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
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
        toggleLoginButtonsDisability(true);
        Parser.setPixivUsername(usernameField.getText());
        Parser.setPixivPassword(passwordField.getText());
        boolean successfulLogin = false;
        try{
            successfulLogin = Parser.loginPixiv();
        } catch (ParserReCaptchaException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Login Failed due to appearance of ReCaptcha\n" +
                    "Retry after a while or try using 'Login Manually'");
            alert.show();
            toggleLoginButtonsDisability(false);
        }
        if(successfulLogin){
            closeWindow();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Login Unsuccessful");
            alert.show();
            toggleLoginButtonsDisability(false);
        }
    }

    @FXML
    protected void handleLoginManuallyButton(){
        toggleLoginButtonsDisability(true);
        Parser.setPixivUsername(usernameField.getText());
        Parser.setPixivPassword(passwordField.getText());
        boolean successfulLogin = false;
        successfulLogin = Parser.loginPixivManually();
        if(successfulLogin){
            closeWindow();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Login Unsuccessful");
            alert.show();
            toggleLoginButtonsDisability(false);
        }
    }

    private void toggleLoginButtonsDisability(boolean isDisabled){
        if(isDisabled){
            loginButton.disableProperty().unbind();
            loginButton.setDisable(true);
            loginManuallyButton.setDisable(true);
        } else {
            loginButton.setDisable(false);
            loginManuallyButton.setDisable(false);
            loginButton.disableProperty().bind(credentialsFilledProperty.not());
        }
    }

    private void closeWindow(){
        if(Parser.isLoggedIn() && saveCredentialCheckBox.isSelected()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("TODO: replace this with saving credentials");
            alert.show();
        }
        Stage stage = (Stage) scenePane.getScene().getWindow();
        stage.close();
    }

    private boolean isCredentialsInputted(){
        if(usernameField.getText().isBlank() || passwordField.getText().isBlank()) return false;
        return true;
    }
}
