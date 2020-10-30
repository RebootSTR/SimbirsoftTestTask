package rafikov.uniqueWords;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rafikov.uniqueWords.exceptions.DataBaseException;

import java.io.File;
import java.sql.*;

public class DBHandler implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(DBHandler.class);

    private String insertSQL;
    private String updateSQL;
    private String dropTableSQL;
    private String createTableSQL;

    private Connection connection = null;
    private String tableName;
    private final String DATABASE_URL;
    private static final String JDBC_DRIVER = "org.sqlite.JDBC";

    private static DBHandler instance = null;

    // single pattern
    private DBHandler(String baseName, String tableName) {
        DATABASE_URL = "jdbc:sqlite:db/" + baseName;
        this.tableName = tableName;
        fillSqlQueries();
    }

    public static synchronized DBHandler getInstance(String baseName, String tableName) {
        if (instance == null)
            instance = new DBHandler(baseName, tableName);
        return instance;
    }

    private void fillSqlQueries() {
        insertSQL = "INSERT INTO " + tableName + " (word, `count`) " +
                    "SELECT ?, ? " +
                    "WHERE (Select Changes() = 0)";
        updateSQL = "UPDATE " + tableName + " " +
                    "SET count=(SELECT `count` from " + tableName + " where word=?)+? " +
                    "WHERE word=?";
        dropTableSQL = "drop table if exists " + tableName;
        createTableSQL = "create table if not exists " + tableName + " ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'word' TEXT, 'count' INTEGER);";

    }

    public DBHandler connect() throws DataBaseException {
        try {
            // register Driver
            Class.forName(JDBC_DRIVER);
            // connecting to DB
            new File("db").mkdirs();
            connection = DriverManager.getConnection(DATABASE_URL);
            connection.setAutoCommit(false);
            logger.info("Successfully connect to DataBase");
        } catch (ClassNotFoundException | SQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataBaseException("Database not connected", ex);
        }
        return this;
    }

    public void createTable() throws DataBaseException {
        logger.debug("creating table with name={}", tableName);
        try (Statement statement = this.connection.createStatement()) {
            statement.execute(dropTableSQL);
            statement.execute(createTableSQL);
        } catch (SQLException ex) {
            throw new DataBaseException("Execute error", ex);
        }
    }

    private void insertWord(String word, int count) throws DataBaseException {
        try (PreparedStatement statement = this.connection.prepareStatement(insertSQL)) {
            statement.setObject(1, word);
            statement.setObject(2, count);
            statement.execute();
        } catch (SQLException ex) {
            throw new DataBaseException("Execute error", ex);
        }
    }

    private void updateWord(String word, int count) throws DataBaseException {
        try (PreparedStatement statement = this.connection.prepareStatement(updateSQL)) {
            statement.setObject(1, word);
            statement.setObject(2, count);
            statement.setObject(3, word);
            statement.execute();
        } catch (SQLException ex) {
            throw new DataBaseException("Execute error", ex);
        }
    }

    public void addWord(String word, int count) throws DataBaseException {
        updateWord(word, count);
        insertWord(word, count);
    }

    public void commit() {
        try {
            if (connection != null) {
                connection.commit();
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
