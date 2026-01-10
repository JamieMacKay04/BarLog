package barLog.src;

public class Utils {

  public static String unescape(String s) {
    s = s.trim();
    if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
      s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
    }
    return s;
  }
}
