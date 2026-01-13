package barLog.src;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class stock {

  @FXML
  private Button btnGoMarquee;

  @FXML
  private Button btnGoMorton;

  @FXML
  private Button btnGoPosto;

  @FXML
  private Button btnGoTransfers;

  @FXML
  private Button btnSubmit;

  @FXML
  private Button goBarLog;

  @FXML
  private ListView<String> lstItems;

  @FXML
  private TableView<ItemCount> tvCurrent;

  @FXML
  private TableColumn<ItemCount, String> colItem;

  @FXML
  private TableColumn<ItemCount, Integer> colQty;

  @FXML
  private TextField txtInput;

  private static final HttpClient HTTP = HttpClient.newHttpClient();
  private static final Path CACHE = Paths.get("data", "barLog_cache.csv");
  private static final Path STOCK = Paths.get("data", "stock.csv");
  private final Map<String, String> cache = new HashMap<>();

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
    loadCache();
    colItem.setCellValueFactory(new PropertyValueFactory<>("item"));
    colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));

    refreshStockTable();
    Platform.runLater(() -> txtInput.requestFocus());

    lstItems.setOnMouseClicked(e -> {
      if (e.getClickCount() == 1) {
        String selected = lstItems.getSelectionModel().getSelectedItem();
        if (selected != null) {
          lstItems.getItems().remove(selected);
        }
      }
    });
  }

  @FXML
  private void handleInput() {
    String code = txtInput.getText().trim();
    txtInput.clear();
    Platform.runLater(() -> txtInput.requestFocus());
    if (code.isEmpty()) return;

    String cachedName = cache.get(code);
    if (cachedName != null) {
      lstItems.getItems().add(cachedName);
      return;
    }

    String url = "https://api.upcitemdb.com/prod/trial/lookup?upc=" + code;

    HTTP.sendAsync(
        HttpRequest.newBuilder(URI.create(url)).GET().build(),
        HttpResponse.BodyHandlers.ofString()
    )
    .thenApply(HttpResponse::body)
    .thenApply(this::parseTitle)
    .exceptionally(e -> null)
    .thenAccept(name -> Platform.runLater(() -> {
      String finalName = (name == null || name.isBlank()) ? code : name.trim();
      lstItems.getItems().add(finalName);
      cache.put(code, finalName);
      appendCache(code, finalName);
    }));
  }

  @FXML
  private void sendSubmit() {
    if (lstItems.getItems().isEmpty()) return;

    Map<String, Integer> toAdd = countItems(lstItems);
    Map<String, Integer> stock = Utils.readStock(STOCK);
    toAdd.forEach((item, qty) -> stock.merge(item, qty, Integer::sum));

    if (!Utils.writeStock(STOCK, stock)) {
      showError("Failed to update stock.csv");
      return;
    }

    lstItems.getItems().clear();
    refreshStockTable();
  }

  private Map<String, Integer> countItems(ListView<String> list) {
    Map<String, Integer> counts = new HashMap<>();
    for (String item : list.getItems()) {
      if (item == null || item.isBlank()) continue;
      counts.merge(item, 1, Integer::sum);
    }
    return counts;
  }

  private void refreshStockTable() {
    Map<String, Integer> stock = Utils.readStock(STOCK);
    tvCurrent.getItems().clear();
    stock.forEach((item, qty) -> tvCurrent.getItems().add(new ItemCount(item, qty)));
  }

  private void loadCache() {
    if (!Files.exists(CACHE)) return;
    try {
      for (String line : Files.readAllLines(CACHE, StandardCharsets.UTF_8)) {
        if (line.isBlank()) continue;
        String[] parts = line.split(",", 2);
        if (parts.length == 2) cache.put(parts[0], Utils.unescape(parts[1]));
      }
    } catch (Exception ignored) {
    }
  }

  private void appendCache(String code, String name) {
    try {
      Files.createDirectories(CACHE.getParent());
      String row = code + "," + Utils.escape(name) + System.lineSeparator();
      Files.writeString(CACHE, row, StandardCharsets.UTF_8,
          StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String parseTitle(String json) {
    if (json == null) return null;

    int i = json.indexOf("\"title\"");
    if (i < 0) return null;

    int colon = json.indexOf(':', i);
    if (colon < 0) return null;

    int firstQuote = json.indexOf('"', colon + 1);
    if (firstQuote < 0) return null;

    int secondQuote = json.indexOf('"', firstQuote + 1);
    if (secondQuote < 0) return null;

    return json.substring(firstQuote + 1, secondQuote);
  }

  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Stock Error");
    alert.setHeaderText(message);
    alert.showAndWait();
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
