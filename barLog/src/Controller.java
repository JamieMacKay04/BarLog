package barLog.src;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class Controller {

    @FXML
    private TextField tfname;

    private Stage mainWindow; //Define mainWindow as a variable of type Stage

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow; //Setter method to set the mainWindow variable
    }

    @FXML
    void onBtnClick(ActionEvent event) {

        String title = tfname.getText();
        mainWindow.setTitle(title);

    }

}

