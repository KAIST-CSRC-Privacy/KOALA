package kr.ac.kaist.csrc.koala.utils.db;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;

public class KoalaDAO {

	// Database connection URL
	static String url = "jdbc:sqlite:data/koala.db";

	/**
	 * Establishes a connection to the database.
	 *
	 * @return A connection to the database, or null if a connection cannot be established.
	 */
	public static Connection makeDBConnect() {
		try {
			return DriverManager.getConnection(url);
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
				"Failed to connect to the database. Please check './data/Koala.db' file",
				"Database Connection Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	/**
	 * Checks if a table exists in the database.
	 *
	 * @param conn      The database connection.
	 * @param tableName The name of the table to check.
	 * @return true if the table exists, false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	public static boolean tableExists(Connection conn, String tableName) throws SQLException {
		try {
			DatabaseMetaData metadata = conn.getMetaData();
			try (ResultSet rs = metadata.getTables(null, null, tableName, null)) {
				return rs.next();
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Failed to check if the table exists in the database.",
				"Database Error", JOptionPane.ERROR_MESSAGE);
			throw e; // Re-throw the exception to handle it in the caller method
		}
	}


	/**
	 * Reads the names of all tables in the database.
	 *
	 * @param conn The database connection.
	 * @return A Vector containing the names of the tables.
	 */
	public static Vector<String> readDBTableName(Connection conn) {
		Vector<String> tableNames = new Vector<>();
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			try (ResultSet resultSet = metaData.getTables(null, null, null, new String[]{"TABLE"})) {
				while (resultSet.next()) {
					String name = resultSet.getString("TABLE_NAME");
					if (!name.contains("sql_")) { // Exclude SQLite internal tables
						tableNames.add(name);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to read table names from the database.", "Database Error",
				JOptionPane.ERROR_MESSAGE);
		}
		return tableNames;
	}

	/**
	 * Reads data from a specified table in the database.
	 *
	 * @param conn      The database connection.
	 * @param tableName The name of the table to read from.
	 * @return A 2D String array containing the table data.
	 */
	public static String[][] readDBTable(Connection conn, String tableName) {
		String formattedTableName = tableName.matches("^\\d+$") ? "\"" + tableName + "\"" : tableName;

		try (Statement statement = conn.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM " + formattedTableName)) {
			ArrayList<String[]> rows = new ArrayList<>();
			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			String[] columnNames = new String[columnCount];
			for (int i = 1; i <= columnCount; i++) {
				columnNames[i - 1] = metaData.getColumnName(i).toUpperCase();
			}
			rows.add(columnNames); // First row is column names

			while (resultSet.next()) {
				String[] row = new String[columnCount];
				for (int j = 0; j < columnCount; j++) {
					row[j] = resultSet.getString(j + 1);
				}
				rows.add(row);
			}

			return rows.toArray(new String[0][]);
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to read data from the table: " + formattedTableName,
				"Database Error", JOptionPane.ERROR_MESSAGE);
			return new String[0][0];
		}
	}


	/**
	 * Updates a table with modified data, first clearing existing data.
	 *
	 * @param conn         The database connection.
	 * @param tableName    The name of the table to update.
	 * @param modifiedData The new data to insert into the table.
	 */
	public static void updateDBTable(Connection conn, String tableName, String[][] modifiedData) {
		String deleteSQL = "DELETE FROM " + tableName;
		StringBuilder placeholders = new StringBuilder("(");
		for (int i = 0; i < modifiedData[0].length; i++) {
			placeholders.append("?");
			if (i < modifiedData[0].length - 1) {
				placeholders.append(", ");
			}
		}
		placeholders.append(")");
		String insertSQL = "INSERT INTO " + tableName + " VALUES " + placeholders;

		try {
			conn.setAutoCommit(false);
			try (Statement stmt = conn.createStatement()) {
				stmt.executeUpdate(deleteSQL);
			}
			try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
				for (int i = 1; i < modifiedData.length; i++) {
					for (int j = 0; j < modifiedData[i].length; j++) {
						pstmt.setString(j + 1, modifiedData[i][j]);
					}
					pstmt.executeUpdate();
				}
			}
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException ex) {
				JOptionPane.showMessageDialog(null, "Failed to rollback changes after an error.", "Database Error",
					JOptionPane.ERROR_MESSAGE);
			}
			JOptionPane.showMessageDialog(null, "Failed to update the table: " + tableName + " with new data.",
				"Database Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, "Failed to set auto-commit mode on the connection.",
					"Database Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Saves new data to a newly created table, first checking if the table exists and dropping it if so.
	 *
	 * @param conn         The database connection.
	 * @param tableName    The name of the new table.
	 * @param modifiedData The data to save in the new table.
	 * @throws SQLException if a database access error occurs or the SQL statements fail.
	 */
	public static void saveNewDBTable(Connection conn, String tableName, String[][] modifiedData) throws SQLException {
		// Check if the table exists and drop it if it does
		String dropTableSQL = "DROP TABLE IF EXISTS \"" + tableName + "\";";

		// Construct SQL command to create the new table with column names
		StringBuilder createTableSQL = new StringBuilder("CREATE TABLE \"" + tableName + "\" (");
		for (int i = 0; i < modifiedData[0].length; i++) {
			String columnName = "\"" + modifiedData[0][i] + "\""; // Quote column names
			createTableSQL.append(columnName).append(" TEXT");
			if (i < modifiedData[0].length - 1) {
				createTableSQL.append(", ");
			}
		}
		createTableSQL.append(");");

		try {
			conn.setAutoCommit(false);

			try (Statement stmt = conn.createStatement()) {
				stmt.execute(dropTableSQL); // Drop existing table if any
				stmt.execute(createTableSQL.toString()); // Create new table
			}

			// Insert new data into the table
			StringBuilder placeholders = new StringBuilder("(");
			for (int i = 0; i < modifiedData[0].length; i++) {
				placeholders.append("?");
				if (i < modifiedData[0].length - 1) {
					placeholders.append(", ");
				}
			}
			placeholders.append(")");
			String insertSQL = "INSERT INTO \"" + tableName + "\" VALUES " + placeholders;

			try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
				for (int i = 1; i < modifiedData.length; i++) { // Skip header row
					for (int j = 0; j < modifiedData[i].length; j++) {
						pstmt.setString(j + 1, modifiedData[i][j]); // Set values for each column
					}
					pstmt.executeUpdate(); // Insert the row
				}
			}
			conn.commit(); // Commit the transaction
		} finally {
			conn.setAutoCommit(true); // Restore auto-commit mode
		}
	}

	/**
	 * Drops (deletes) a specified table from the database if it exists.
	 *
	 * @param conn      The database connection.
	 * @param tableName The name of the table to be dropped.
	 */
	public static void dropTable(Connection conn, String tableName) {
		String sql = "DROP TABLE IF EXISTS \"" + tableName + "\";";

		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			JOptionPane.showMessageDialog(null, "Table '" + tableName + "' has been successfully dropped.",
				"Table Dropped", JOptionPane.INFORMATION_MESSAGE);
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to drop the table: " + tableName + ".\n" + e.getMessage(),
				"Database Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
