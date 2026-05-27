import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;

public class TestFullLogin {
    public static void main(String[] args) throws Exception {
        // Login with 13333333333
        URL url = new URL("http://localhost:18760/api/oauth/anyTenant/login");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        String auth = java.util.Base64.getEncoder().encodeToString("luohuo_web_pro:luohuo_web_pro_secret".getBytes());
        conn.setRequestProperty("Authorization", auth);
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        String body = "{\"account\":\"13333333333\",\"password\":\"123456a\",\"systemType\":2,\"deviceType\":\"MOBILE\",\"grantType\":\"PASSWORD\",\"clientId\":\"test\"}";
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
        System.out.println("LOGIN HTTP " + code + ": " + loginResp);

        // Try token from a known valid format
        if (loginResp.contains("\"token\":\"")) {
            String token = loginResp.split("\"token\":\"")[1].split("\"")[0];
            System.out.println("Got token: " + token.substring(0, Math.min(30, token.length())) + "...");

            // Call IM msg list
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
            if (imIs != null) {
                BufferedReader imBr = new BufferedReader(new InputStreamReader(imIs, "UTF-8"));
                StringBuilder imSb = new StringBuilder();
                while((line = imBr.readLine()) != null) imSb.append(line);
                String r = imSb.toString();
                System.out.println("IM HTTP " + imCode + ": " + (r.length() > 500 ? r.substring(0, 500) + "..." : r));
            } else {
                System.out.println("IM HTTP " + imCode + ": null stream");
            }
        } else {
            System.out.println("Login failed, no token. Testing error response encoding...");
            // Check if error response is valid JSON by verifying UTF-8 bytes
            byte[] rawBytes = loginResp.getBytes("UTF-8");
            System.out.println("Response length: " + rawBytes.length + " bytes");
            System.out.println("First 200 chars: " + loginResp.substring(0, Math.min(200, loginResp.length())));
        }
    }
}