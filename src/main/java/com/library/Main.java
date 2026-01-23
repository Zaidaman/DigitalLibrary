package com.library;
import com.library.models.UserPreferences;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/login-view.fxml"));
        Scene scene = new Scene(loader.load(), 400, 350);
        stage.setTitle("Login - Digital Library");

        UserPreferences prefs = new UserPreferences();
        String cssFile = UserPreferences.THEME_DARK.equals(prefs.getTheme()) 
            ? "/css/login-dark.css" 
            : "/css/login.css";
        
        scene.getStylesheets().add(
            getClass().getResource(cssFile).toExternalForm()
        );

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
