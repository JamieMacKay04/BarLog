package barLog.src;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        URL mainScene = Objects.requireNonNull(
            getClass().getResource("mainScene.fxml"),
            "Missing application resource: mainScene.fxml"
        );
        FXMLLoader loader = new FXMLLoader(mainScene);
        Parent root = loader.load();
        Controller controller = loader.getController();

        var iconStream = Main.class.getResourceAsStream("img/mhLogo.jpeg");
        if (iconStream != null) {
        primaryStage.getIcons().add(new Image(iconStream));
        }

        controller.setMainWindow(primaryStage);
        primaryStage.setTitle("BarLog");
        primaryStage.setScene(new Scene(root, 800, 800));
        primaryStage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }
}
