package weight_watcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        primaryStage.setScene(new Scene(root, screenBounds.getWidth() * 0.7, screenBounds.getHeight() * 0.7));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
