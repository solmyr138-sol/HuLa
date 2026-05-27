import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;

public class LoginTest {
    public static void main(String[] args) throws Exception {
        URL url = new URL("http://127.0.0.1:18773/anyTenant/login");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        conn.setDoOutput(true);
        String body = "{\"account\":\"13333333333\",\"password\":\"123456a.\",\"systemType\":2,\"deviceType\":\"MOBILE\",\"grantType\":\"PASSWORD\",\"clientId\":\"test\"}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes("UTF-8"));
        }
        int code = conn.getResponseCode();
        System.out.println("Status: " + code);
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        String result = sb.toString();
        System.out.println("Body: " + result.substring(0, Math.min(2000, result.length())));
    }
}