import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;

public class TestDB {
    public static void main(String[] args) throws Exception {
        java.sql.Connection conn = java.sql.DriverManager.getConnection(
            "jdbc:mysql://127.0.0.1:13306/luohuo_dev?serverTimezone=Asia/Shanghai&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true",
            "root", "root"
        );
        // Check user 13333333333
        java.sql.PreparedStatement ps = conn.prepareStatement(
            "SELECT id, username, mobile, nick_name, password_error_num, tenant_id, state, salt, system_type FROM def_user WHERE mobile = '13333333333' AND is_del = false"
        );
        java.sql.ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            System.out.println("ID: " + rs.getLong("id"));
            System.out.println("Username: " + rs.getString("username"));
            System.out.println("Mobile: " + rs.getString("mobile"));
            System.out.println("NickName: " + rs.getString("nick_name"));
            System.out.println("ErrorNum: " + rs.getInt("password_error_num"));
            System.out.println("TenantId: " + rs.getLong("tenant_id"));
            System.out.println("State: " + rs.getString("state"));
            System.out.println("Salt: " + rs.getString("salt"));
            System.out.println("SystemType: " + rs.getInt("system_type"));
            System.out.println("---");
        }

        // Reset password error count
        java.sql.PreparedStatement reset = conn.prepareStatement(
            "UPDATE def_user SET password_error_num = 0 WHERE mobile = '13333333333' AND is_del = false"
        );
        int updated = reset.executeUpdate();
        System.out.println("Reset password_error_num for " + updated + " users");

        conn.close();
    }
}