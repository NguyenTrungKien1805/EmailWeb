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
        // Mặc định = máy local. Trên Render, các giá trị này đến từ biến môi trường.
        String url = "jdbc:postgresql://" + env("DB_HOST", "localhost")
                + ":" + env("DB_PORT", "5432")
                + "/" + env("DB_NAME", "emaildb")
                + "?sslmode=" + env("DB_SSL", "disable")
                + "&channelBinding=disable";
        return DriverManager.getConnection(url,
                env("DB_USER", "postgres"),
                env("DB_PASS", "1234"));
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