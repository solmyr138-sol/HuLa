import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;

public class TestEnc {
    public static void main(String[] args) throws Exception {
        URL url = new URL("http://localhost:18760/api/oauth/anyTenant/login");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
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
        String contentType = conn.getContentType();
        System.out.println("Content-Type: " + contentType);

        InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        byte[] rawBytes = is.readAllBytes();
        System.out.println("Raw bytes length: " + rawBytes.length);

        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < Math.min(rawBytes.length, 120); i++) {
            hex.append(String.format("%02x ", rawBytes[i] & 0xff));
        }
        System.out.println("Hex: " + hex.toString().trim());

        String utf8 = new String(rawBytes, "UTF-8");
        String gbk = new String(rawBytes, "GBK");
        System.out.println("As-UTF8: " + utf8.substring(0, Math.min(200, utf8.length())));
        System.out.println("As-GBK: " + gbk.substring(0, Math.min(200, gbk.length())));
    }
}