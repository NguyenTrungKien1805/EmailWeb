package murach.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

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

        // 1. Nếu trên Render có cài DB_URL thì dùng trực tiếp
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl != null && !dbUrl.isBlank()) {
            return DriverManager.getConnection(dbUrl);
        }

        // 2. Dự phòng khi chạy local hoặc khai báo riêng lẻ từng biến
        String host = env("DB_HOST", "localhost");
        String port = env("DB_PORT", "5432");
        String dbName = env("DB_NAME", "emaildb");
        String ssl = env("DB_SSL", "disable");

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?sslmode=" + ssl;
        return DriverManager.getConnection(url, env("DB_USER", "postgres"), env("DB_PASS", "1234"));
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