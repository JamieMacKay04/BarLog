package barLog.src;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class transfers {

    public record Transfer(
    String id,
    String dateTime,
    String initials,
    String location,
    String items
) {}

    @FXML
    private Button btnGoHotelBar;

    @FXML
    private Button btnGoMarquee;

    @FXML
    private Button btnGoMorton;

    @FXML
    private Button btnGoPosto;

    @FXML
    private ListView<Transfer> transfersListView;

    private static final Path TRANSFERS =
    Paths.get("data", "transfers.csv");

private String aggregateItems(String raw) {
    String[] items = raw.split(" \\| ");
    Map<String, Integer> counts = new HashMap<>();

    for (String i : items)
        counts.merge(i, 1, Integer::sum);

    StringBuilder sb = new StringBuilder();
    counts.forEach((k, v) ->
        sb.append(k).append(" ×").append(v).append("\n")
    );

    return sb.toString().trim();
}

    @FXML
private void initialize() {
    transfersListView.setCellFactory(lv -> new ListCell<>() {
        @Override
        protected void updateItem(Transfer t, boolean empty) {
            super.updateItem(t, empty);
            if (empty || t == null) {
                setGraphic(null);
                return;
            }

            VBox card = new VBox(
                new Label(t.dateTime()),
                new Label(t.location()),
                new Label(t.initials()),
                new Label(aggregateItems(t.items()))

            );

            card.getStyleClass().add("transfer-card");
            setGraphic(card);
        }
    });

    if (!Files.exists(TRANSFERS)) return;

try {
    for (String line : Files.readAllLines(TRANSFERS)) {
        if (line.isBlank()) continue;

        String[] parts = line.split(",", 5);
        transfersListView.getItems().add(
            new Transfer(
                parts[0],
                parts[1],
                parts[2],
                parts[3],
                parts[4]
            )
        );
    }
} catch (IOException e) {
    e.printStackTrace();
}

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
private void goHotelBar(ActionEvent e) {
  Navigator.go(e, "hotelBar.fxml");
}

}
