import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;

public class TestGWLogin3 {
    public static void main(String[] args) throws Exception {
        URL url = new URL("http://localhost:18760/api/oauth/anyTenant/login");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        String auth = java.util.Base64.getEncoder().encodeToString("luohuo_web_pro:luohuo_web_pro_secret".getBytes());
        conn.setRequestProperty("Authorization", auth);
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        String body = "{\"mobile\":\"13333333333\",\"password\":\"123456a\",\"systemType\":2,\"deviceType\":\"MOBILE\",\"grantType\":\"PASSWORD\",\"clientId\":\"test\"}";
        try(OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes("UTF-8"));
        }

        int code = conn.getResponseCode();
        InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) sb.append(line);
        String loginResp = sb.toString();
        System.out.println("=== LOGIN (HTTP " + code + ") ===");
        System.out.println(loginResp);

        if (loginResp.contains("\"token\":\"")) {
            String token = loginResp.split("\"token\":\"")[1].split("\"")[0];
            System.out.println("\nToken obtained. Testing IM endpoint...");

            URL imUrl = new URL("http://localhost:18760/api/im/chat/msg/list");
            HttpURLConnection imConn = (HttpURLConnection) imUrl.openConnection();
            imConn.setRequestMethod("POST");
            imConn.setRequestProperty("Content-Type", "application/json");
            imConn.setRequestProperty("satoken", token);
            imConn.setDoOutput(true);
            imConn.setConnectTimeout(10000);
            imConn.setReadTimeout(10000);
            try(OutputStream os2 = imConn.getOutputStream()) {
                os2.write("{}".getBytes("UTF-8"));
            }
            int imCode = imConn.getResponseCode();
            InputStream imIs = imCode >= 400 ? imConn.getErrorStream() : imConn.getInputStream();
            if (imIs == null) { System.out.println("IM null stream, HTTP " + imCode); return; }
            BufferedReader imBr = new BufferedReader(new InputStreamReader(imIs, "UTF-8"));
            StringBuilder imSb = new StringBuilder();
            while((line = imBr.readLine()) != null) imSb.append(line);
            System.out.println("=== IM (HTTP " + imCode + ") ===");
            String r = imSb.toString();
            System.out.println(r.length() > 800 ? r.substring(0, 800) + "..." : r);
        }
    }
}