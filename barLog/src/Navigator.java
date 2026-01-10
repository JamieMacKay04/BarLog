package barLog.src;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navigator {

  public static void go(ActionEvent e, String fxml) {
    try {
      Parent root = FXMLLoader.load(Navigator.class.getResource(fxml));
      Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
      stage.setScene(new Scene(root));
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}