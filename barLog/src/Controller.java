package barLog.src;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;





public class Controller {

    @FXML
    private TextField tfname;

    private Stage mainWindow; 

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }


    @FXML private TextField txtInput;
    @FXML private ListView<String> lstItems;
    @FXML private ComboBox<String> locationChoiceBox;
    @FXML private Button btnSubmit;
  
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path CACHE = Paths.get("data", "barLog_cache.csv"); // saved beside where you run the app
    private final Map<String, String> cache = new HashMap<>();
    

@FXML
private void initialize() {
  loadCache();
  Platform.runLater(() -> txtInput.requestFocus());
  locationChoiceBox.getItems().addAll("Hotel Bar", "Marquee", "Morton", "Il Posto");
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

private Path getLocationFile(String location) {
  return switch (location) {
    case "Il Posto" -> Paths.get("data", "il_posto.csv");
    case "Marquee" -> Paths.get("data", "marquee.csv");
    case "Morton" -> Paths.get("data", "morton.csv");
    case "Hotel Bar" -> Paths.get("data", "hotel_bar.csv");
    case "Transfers" -> Paths.get("data", "transfers.csv");
    default -> throw new IllegalStateException("Unknown location: " + location);
  };
}

Map<String, String> venueFiles = Map.of(
    "Marquee", "data/marquee.csv",
    "Morton", "data/morton.csv",
    "Posto", "data/posto.csv"
);



@FXML
private void sendSubmit() {

  if (locationChoiceBox.getValue() == null || locationChoiceBox.getValue().isEmpty()) {
    System.out.println("Please select a location before submitting.");
    return;
  }

  TextInputDialog dialog = new TextInputDialog();
  dialog.setTitle("Submit");
  dialog.setHeaderText("Enter your initials");
  dialog.setContentText("Initials (2 letters):");

  Optional<String> result = dialog.showAndWait();
  if (result.isEmpty()) return;

  String initials = result.get().trim().toUpperCase();
  if (!initials.matches("[A-Z]{2}")) {
    System.out.println("Initials must be exactly 2 letters.");
    return;
  }

  String transferId = UUID.randomUUID().toString();
  String dateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
  String location = locationChoiceBox.getValue();
  String itemsJoined = String.join(" | ", lstItems.getItems());

  Path file = Paths.get("data", "transfers.csv");

  try {
    Files.createDirectories(file.getParent());

    String row =
      transferId + "," +
      dateTime + "," +
      initials + "," +
      location + "," +
      escape(itemsJoined) +
      System.lineSeparator();

    Files.writeString(file, row, StandardCharsets.UTF_8,
      StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      
      Path destinationFile = getLocationFile(location);

for (String item : lstItems.getItems()) {
    String venueRow =
        dateTime + "," +
        escape(item) +
        System.lineSeparator();

    Files.writeString(
        destinationFile,
        venueRow,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND
    );
}

  } catch (IOException e) {
    e.printStackTrace();
  }

  lstItems.getItems().clear();
}




private void loadCache() {
  if (!Files.exists(CACHE)) return;
  try {
    for (String line : Files.readAllLines(CACHE, StandardCharsets.UTF_8)) {
      if (line.isBlank()) continue;
      String[] parts = line.split(",", 2);
      if (parts.length == 2) cache.put(parts[0], unescape(parts[1]));
    }
  } catch (IOException ignored) {}
}

private void appendCache(String code, String name) {
  try {
    Files.createDirectories(CACHE.getParent());
    System.out.println("Saving to: " + CACHE.toAbsolutePath());

    String row = code + "," + escape(name) + System.lineSeparator();
    Files.writeString(CACHE, row, StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.APPEND);

  } catch (IOException e) {
    e.printStackTrace();
  }
}


private String parseTitle(String json) {
  try {
    return MAPPER.readTree(json)
      .path("items")
      .path(0)
      .path("title")
      .asText(null);
  } catch (Exception e) {
    return null;
  }
}

private String escape(String s) {
  return "\"" + s.replace("\"", "\"\"") + "\"";
}

private String unescape(String s) {
  s = s.trim();
  if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
    s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
  }
  return s;
}

@FXML
private void editBarcode(ActionEvent e) {
  Dialog<ButtonType> dialog = new Dialog<>();
  dialog.setTitle("Add Barcode Override");

  ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
  dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

  TextField codeField = new TextField();
  codeField.setPromptText("Barcode / UPC");

  TextField nameField = new TextField();
  nameField.setPromptText("Product name");

  GridPane grid = new GridPane();
  grid.setHgap(10);
  grid.setVgap(10);
  grid.add(new Label("Code:"), 0, 0);
  grid.add(codeField, 1, 0);
  grid.add(new Label("Name:"), 0, 1);
  grid.add(nameField, 1, 1);

  dialog.getDialogPane().setContent(grid);

  Node saveBtn = dialog.getDialogPane().lookupButton(save);
  saveBtn.setDisable(true);

  ChangeListener<String> validator = (obs, o, n) -> {
    saveBtn.setDisable(codeField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty());
  };
  codeField.textProperty().addListener(validator);
  nameField.textProperty().addListener(validator);

  Platform.runLater(codeField::requestFocus);

  dialog.showAndWait().ifPresent(btn -> {
    if (btn != save) return;

    String code = codeField.getText().trim();
    String name = nameField.getText().trim();

    if (cache.containsKey(code)) {
      Alert a = new Alert(Alert.AlertType.ERROR);
      a.setTitle("Duplicate barcode");
      a.setHeaderText("That barcode already exists");
      a.setContentText("Code: " + code + "\nCurrent name: " + cache.get(code));
      a.showAndWait();
      return;
    }

    cache.put(code, name);
    appendCache(code, name); // writes to barLog_cache.csv
  });
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


