import java.sql.*;
import java.time.Year;

/**
 * DBUtil FILE
 * 		- Used for Database utility and IDE connection with database
 * 		- Used to generate next applicant ID and Parent ID (auto incrementing methods)
 */

public final class DBUtil {

    private DBUtil() {}

    // Returns a new JDBC Connection - connects IDE to SQL
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                Constants.DB_URL, Constants.DB_USER, Constants.DB_PASS);
    }


    // Generates the next applicant ID in the format {@code YYYY-NNNN}.
    // Reads the latest ID for the current year from the database.
    public static String generateApplicantId(Connection conn) throws SQLException {
        String year = String.valueOf(Year.now().getValue());
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT applicant_id FROM applicant_t " +
                "WHERE applicant_id LIKE ? ORDER BY applicant_id DESC LIMIT 1")) {
            ps.setString(1, year + "-%");
            ResultSet rs = ps.executeQuery();
            return rs.next()
                ? String.format("%s-%04d", year,
                    Integer.parseInt(rs.getString(1).split("-")[1]) + 1)
                : year + "-0001";
        }
    }

    // Generates the next parent ID in the format {@code P-NNNN}.
    // Reads the latest ID from the database.
    public static String generateParentId(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT parent_id FROM parent_t ORDER BY parent_id DESC LIMIT 1")) {
            return rs.next()
                ? String.format("P-%04d",
                    Integer.parseInt(rs.getString(1).split("-")[1]) + 1)
                : "P-0001";
        }
    }
}