package barLog.src;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path CACHE = Paths.get("barLog_cache.csv"); // saved beside where you run the app
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

  String url = "https://api.upcitemdb.com/prod/trial/lookup?upc=" + code;

  HTTP.sendAsync(
      HttpRequest.newBuilder(URI.create(url)).GET().build(),
      HttpResponse.BodyHandlers.ofString()
  )
  .thenApply(HttpResponse::body)
  .thenApply(this::parseTitle)
  .exceptionally(e -> null)
  .thenAccept(name -> Platform.runLater(() ->
      lstItems.getItems().add(name == null ? code : name)
  ));
}

private String parseTitle(String json) {
  try {
    var root = MAPPER.readTree(json);
    var items = root.get("items");
    if (items == null || items.isEmpty()) return null;
    var title = items.get(0).get("title");
    return title.asText().split(" - ")[0];
  } catch (Exception e) {
    return null;
  }
}


}

