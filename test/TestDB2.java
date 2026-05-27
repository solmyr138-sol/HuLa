import java.sql.*;

public class TestDB2 {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection(
            "jdbc:mysql://127.0.0.1:13306/luohuo_dev?serverTimezone=Asia/Shanghai&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true",
            "root", "123456"
        );
        PreparedStatement ps = conn.prepareStatement(
            "SELECT id, username, mobile, nick_name, password_error_num, tenant_id, state, system_type FROM def_user WHERE mobile = '13333333333' AND is_del = false"
        );
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            System.out.println("ID=" + rs.getLong("id") + " username=" + rs.getString("username") + " mobile=" + rs.getString("mobile") + " tenant=" + rs.getLong("tenant_id") + " errNum=" + rs.getInt("password_error_num") + " state=" + rs.getString("state") + " sysType=" + rs.getInt("system_type"));
        }
        // Reset error count
        PreparedStatement reset = conn.prepareStatement("UPDATE def_user SET password_error_num = 0, password_error_last_time = null WHERE mobile = '13333333333' AND is_del = false");
        System.out.println("Reset " + reset.executeUpdate() + " users");
        conn.close();
    }
}