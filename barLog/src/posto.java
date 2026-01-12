package barLog.src;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class posto {

    @FXML
    private Button btnApply;

    @FXML
    private Button btnBarLog;

    @FXML
    private Button btnGoMarquee;

    @FXML
    private Button btnGoMorton;

    @FXML
    private Button btnGoTransfers;

    @FXML
    private Button btnHotelBar;

    @FXML
    private TableColumn<?, ?> colItem;

    @FXML
    private TableColumn<?, ?> colQty;

    @FXML
    private DatePicker dateFrom;

    @FXML
    private DatePicker dateTo;

    @FXML
    private TableView<?> tvIntake;

@FXML
private void goPosto(ActionEvent e) {
  Navigator.go(e, "posto.fxml");
}

@FXML
private void goMarquee(ActionEvent e) {
  Navigator.go(e, "marquee.fxml");
}

@FXML
private void goMorton(ActionEvent e) {
  Navigator.go(e, "morton.fxml");
}

@FXML
private void goBarLog(ActionEvent e) {
  Navigator.go(e, "mainScene.fxml");
}

@FXML
private void goTransfers(ActionEvent e) {
  Navigator.go(e, "transfers.fxml");
}

@FXML
private void goHotelBar(ActionEvent e) {
  Navigator.go(e, "hotelBar.fxml");
}

}