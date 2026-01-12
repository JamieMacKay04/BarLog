package barLog.src;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainScene.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();

        var iconStream = Main.class.getResourceAsStream("img/mhLogo.jpeg");
        if (iconStream != null) {
        primaryStage.getIcons().add(new Image(iconStream));
        }

        var u = getClass().getResource("mhLogo.jpeg");
        if (u != null) primaryStage.getIcons().add(new Image(u.toExternalForm()));


        controller.setMainWindow(primaryStage);
        primaryStage.setScene(new Scene(root, 800, 800));
        primaryStage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }
}