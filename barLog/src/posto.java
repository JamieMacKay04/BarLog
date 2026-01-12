package barLog.src;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import barLog.src.hotelBar.ItemCount;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

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
    private TableColumn<ItemCount, String> colItem;

    @FXML
    private TableColumn<ItemCount, Integer> colQty;

    @FXML
    private DatePicker dateFrom;

    @FXML
    private DatePicker dateTo;

    @FXML
    private TableView<ItemCount> tvIntake;


        @FXML
private void initialize() {
    colItem.setCellValueFactory(new PropertyValueFactory<>("item"));
    colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));

    dateTo.setValue(java.time.LocalDate.now());
    dateFrom.setValue(java.time.LocalDate.now().minusDays(7));

    applyFilter();
}

private static final Path FILE = Paths.get("data", "posto.csv");

@FXML
private void applyFilter() {
    if (dateFrom.getValue() == null || dateTo.getValue() == null) return;

    var from = dateFrom.getValue().atStartOfDay();
    var to = dateTo.getValue().plusDays(1).atStartOfDay(); // inclusive end date

    Map<String, Integer> counts = new HashMap<>();

    if (!Files.exists(FILE)) {
        tvIntake.getItems().clear();
        return;
    }

    try {
        for (String line : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;

            String[] parts = line.split(",", 2);
            if (parts.length < 2) continue;

            LocalDateTime ts = LocalDateTime.parse(parts[0]);
            if (ts.isBefore(from) || !ts.isBefore(to)) continue;

            String product = unescape(parts[1]);
            counts.merge(product, 1, Integer::sum);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    tvIntake.getItems().clear();
    counts.forEach((k, v) -> tvIntake.getItems().add(new ItemCount(k, v)));
}

private String unescape(String s) {
    s = s.trim();
    if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
        s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
    }
    return s;
}


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