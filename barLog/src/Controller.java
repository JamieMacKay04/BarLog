package barLog.src;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class Controller {

    @FXML
    private TextField tfname;

    private Stage mainWindow; //Define mainWindow as a variable of type Stage

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow; //Setter method to set the mainWindow variable
    }

    @FXML private TextField txtInput;
    @FXML private ListView<String> lstItems;

    @FXML private void handleInput() {
        String s = txtInput.getText().trim();
            if (!s.isEmpty()) lstItems.getItems().add(s);
            txtInput.clear();
    }
    
    @FXML
    private Label title;

    @FXML
    void onBtnClick(ActionEvent event) {

        String title = tfname.getText();
        mainWindow.setTitle(title);

    }

}

