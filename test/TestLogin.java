import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;

public class TestLogin {
    public static void main(String[] args) throws Exception {
        URL url = new URL("http://localhost:18773/anyTenant/login");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        String auth = java.util.Base64.getEncoder().encodeToString("luohuo_web_pro:luohuo_web_pro_secret".getBytes());
        conn.setRequestProperty("Authorization", auth);
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        
        String body = "{\"mobile\":\"13333333333\",\"password\":\"123456a\",\"systemType\":\"MEMBER\",\"deviceType\":\"ANDROID\",\"grantType\":\"PASSWORD\",\"key\":\"\"}";
        try(OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes("UTF-8"));
        }
        
        int code = conn.getResponseCode();
        InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) sb.append(line);
        System.out.println("HTTP " + code);
        System.out.println(sb.toString());
    }
}