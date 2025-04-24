import java.sql.*;

public class DBHelper {
    private static final String DB_URL = "jdbc:sqlite:todo.db";

    static {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            String userTable = "CREATE TABLE IF NOT EXISTS users (" +
                               "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                               "username TEXT UNIQUE," +
                               "password TEXT)";
            stmt.execute(userTable);

            String taskTable = "CREATE TABLE IF NOT EXISTS tasks (" +
                               "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                               "user_id INTEGER," +
                               "description TEXT," +
                               "deadline TEXT," +
                               "status INTEGER DEFAULT 0," +
                               "FOREIGN KEY(user_id) REFERENCES users(id))";
            stmt.execute(taskTable);

            // Add columns if they don't exist (for migration)
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "tasks", "deadline");
            if (!rs.next()) {
                stmt.execute("ALTER TABLE tasks ADD COLUMN deadline TEXT");
            }
            rs = meta.getColumns(null, null, "tasks", "status");
            if (!rs.next()) {
                stmt.execute("ALTER TABLE tasks ADD COLUMN status INTEGER DEFAULT 0");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
