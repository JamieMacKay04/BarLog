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
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;
import javafx.fxml.FXML;
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
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path CACHE = Paths.get("data", "barLog_cache.csv"); // saved beside where you run the app
    private final Map<String, String> cache = new HashMap<>();

@FXML
private void initialize() {
  loadCache();
  Platform.runLater(() -> txtInput.requestFocus());
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



}

