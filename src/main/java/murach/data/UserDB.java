package murach.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import murach.business.User;

public class UserDB {

    private static final String URL  = "jdbc:postgresql://localhost:5432/emaildb";
    private static final String USER = "postgres";
    private static final String PASS = "1234";   // sửa lại

    private static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void insert(User user) throws SQLException {
        String sql = "INSERT INTO email_list (email, first_name, last_name) "
                + "VALUES (?, ?, ?) "
                + "ON CONFLICT (email) DO NOTHING";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getLastName());
            ps.executeUpdate();
        }
    }
}