package murach.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.net.URI;

import murach.business.User;

public class UserDB {

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    private static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }

        String dbUrl = System.getenv("DB_URL");

        // Nếu DB_URL là URL Render dạng postgresql://
        if (dbUrl != null && !dbUrl.isBlank()) {
            if (dbUrl.startsWith("postgresql://")) {
                try {
                    URI uri = URI.create(dbUrl);

                    String userInfo = uri.getUserInfo();
                    String username = userInfo.split(":", 2)[0];
                    String password = userInfo.split(":", 2)[1];

                    String url = "jdbc:postgresql://"
                            + uri.getHost() + ":"
                            + (uri.getPort() == -1 ? 5432 : uri.getPort())
                            + uri.getPath() + "?sslmode=require";

                    return DriverManager.getConnection(url, username, password);

                } catch (Exception e) {
                    throw new SQLException("DB_URL không hợp lệ", e);
                }
            }

            // Nếu DB_URL đã đúng định dạng JDBC
            return DriverManager.getConnection(
                    dbUrl,
                    env("DB_USER", "postgres"),
                    env("DB_PASS", "1234")
            );
        }

        // Kết nối local hoặc dùng các biến môi trường riêng
        String host = env("DB_HOST", "localhost");
        String port = env("DB_PORT", "5432");
        String dbName = env("DB_NAME", "emaildb");
        String ssl = env("DB_SSL", "disable");

        String url = "jdbc:postgresql://" + host + ":"
                + port + "/" + dbName + "?sslmode=" + ssl;

        return DriverManager.getConnection(
                url,
                env("DB_USER", "postgres"),
                env("DB_PASS", "1234")
        );
    }

    public static void insert(User user) throws SQLException {
        try (Connection c = getConnection()) {

            try (Statement st = c.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS email_list ("
                        + "email VARCHAR(255) PRIMARY KEY, "
                        + "first_name VARCHAR(100) NOT NULL, "
                        + "last_name VARCHAR(100) NOT NULL)");
            }

            String sql = "INSERT INTO email_list (email, first_name, last_name) "
                    + "VALUES (?, ?, ?) "
                    + "ON CONFLICT (email) DO NOTHING";

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getFirstName());
                ps.setString(3, user.getLastName());
                ps.executeUpdate();
            }
        }
    }
}