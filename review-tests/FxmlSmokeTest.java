package barLog.review;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import barLog.src.Main;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;

public final class FxmlSmokeTest {
  private static final List<String> SCREENS = List.of(
      "mainScene.fxml",
      "stock.fxml",
      "hotelBar.fxml",
      "marquee.fxml",
      "morton.fxml",
      "posto.fxml",
      "transfers.fxml"
  );

  private FxmlSmokeTest() {
  }

  public static void main(String[] args) throws Exception {
    CountDownLatch finished = new CountDownLatch(1);
    AtomicReference<Throwable> failure = new AtomicReference<>();

    Platform.startup(() -> { });
    Platform.runLater(() -> {
      try {
        for (String screen : SCREENS) {
          URL resource = Main.class.getResource(screen);
          if (resource == null) {
            throw new IllegalStateException("Missing FXML resource: " + screen);
          }
          new FXMLLoader(resource).load();
          System.out.println("Loaded " + screen);
        }
      } catch (Throwable error) {
        failure.set(error);
      } finally {
        finished.countDown();
      }
    });

    finished.await();
    Platform.exit();
    if (failure.get() != null) {
      throw new RuntimeException("FXML smoke test failed", failure.get());
    }
  }
}
