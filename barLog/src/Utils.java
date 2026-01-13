package barLog.src;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Utils {

  public static String unescape(String s) {
    s = s.trim();
    if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
      s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
    }
    return s;
  }

  public static String escape(String s) {
    return "\"" + s.replace("\"", "\"\"") + "\"";
  }

  public static Map<String, Integer> readStock(Path file) {
    Map<String, Integer> counts = new TreeMap<>(String::compareToIgnoreCase);
    if (!Files.exists(file)) return counts;

    try {
      for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
        if (line.isBlank()) continue;
        String[] parts = line.split(",", 2);
        if (parts.length < 2) continue;

        String item = unescape(parts[0]);
        String qtyStr = parts[1].trim();
        int qty;
        try {
          qty = Integer.parseInt(qtyStr);
        } catch (NumberFormatException ignored) {
          continue;
        }

        if (item.isBlank() || qty == 0) continue;
        counts.put(item, qty);
      }
    } catch (IOException ignored) {
    }

    return counts;
  }

  public static boolean writeStock(Path file, Map<String, Integer> counts) {
    try {
      Files.createDirectories(file.getParent());

      List<String> lines = new ArrayList<>();
      counts.forEach((item, qty) -> {
        if (qty > 0) lines.add(escape(item) + "," + qty);
      });

      Files.write(file, lines, StandardCharsets.UTF_8,
          StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }
}
