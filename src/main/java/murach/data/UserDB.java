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
        String url = "jdbc:postgresql://" + env("DB_HOST", "dpg-dbl9oeoae00c73bjh9q0-a.singapore-postgres.render.com")
                + ":" + env("DB_PORT", "5432")
                + "/" + env("DB_NAME", "emaildb")
                + "?sslmode=" + env("DB_SSL", "require")
                + "&channelBinding=disable";
        return DriverManager.getConnection(url,
                env("DB_USER", "TrungKien"),
                env("DB_PASS", "997pQaiGcb58y1XJSX5x2twqjCJD6ysm"));   // "1234" chỉ là mặc định cho máy local
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