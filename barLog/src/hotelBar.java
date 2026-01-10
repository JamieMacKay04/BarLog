package barLog.src;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;



public class hotelBar {

    @FXML
    private Button btnApply;

    @FXML
    private DatePicker dateFrom;

    @FXML
    private DatePicker dateTo;

    @FXML
    private TableView<ItemCount> tvIntake;
    @FXML
    private TableColumn<ItemCount, String> colItem;

    @FXML
    private TableColumn<ItemCount, Integer> colQty;

  @FXML private Button btnPosto;
  @FXML private Button btnMarquee;
  @FXML private Button btnMorton;
  @FXML private Button btnBarLog;
  @FXML private Button btnTransfers;


public static class ItemCount {

  private final SimpleStringProperty item = new SimpleStringProperty();
  private final SimpleIntegerProperty qty = new SimpleIntegerProperty();

  public ItemCount(String item, int qty) {
    this.item.set(item);
    this.qty.set(qty);
  }

  public String getItem() {
    return item.get();
  }

  public int getQty() {
    return qty.get();
  }
}

@FXML
private void initialize() {
  colItem.setCellValueFactory(new PropertyValueFactory<>("item"));
  colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));

  btnApply.setOnAction(e -> applyFilter());
}


private void applyFilter() {
  LocalDate from = dateFrom.getValue();
  LocalDate to = dateTo.getValue();

  List<String> items = loadItemsForHotelBar(from, to);

  Map<String, Integer> counts = new HashMap<>();
  for (String it : items) counts.merge(it, 1, Integer::sum);

  List<ItemCount> rows = counts.entrySet().stream()
    .sorted((a,b) -> b.getValue().compareTo(a.getValue()))
    .map(e -> new ItemCount(e.getKey(), e.getValue()))
    .toList();

  tvIntake.getItems().setAll(rows);
}

private List<String> loadItemsForHotelBar(LocalDate from, LocalDate to) {
  Path file = Paths.get("data", "transfers.csv");
  if (!Files.exists(file)) return List.of();

  List<String> out = new ArrayList<>();

  try {
    for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
      if (line.isBlank()) continue;

      String[] parts = line.split(",", 5);
      if (parts.length < 5) continue;

      String dateTimeStr = parts[1];
      String location = parts[3];
      String itemsJoined = Utils.unescape(parts[4]);


      if (!"Hotel Bar".equals(location)) continue;

      LocalDate d = LocalDateTime.parse(dateTimeStr).toLocalDate();

      if (from != null && d.isBefore(from)) continue;
      if (to != null && d.isAfter(to)) continue;

      for (String item : itemsJoined.split("\\s\\|\\s")) {
        String trimmed = item.trim();
        if (!trimmed.isEmpty()) out.add(trimmed);
      }
    }
  } catch (IOException ignored) {}

  return out;
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

}



