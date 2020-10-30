package rafikov.uniqueWords;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rafikov.uniqueWords.exceptions.DataBaseException;

import java.io.File;
import java.sql.*;
import java.util.function.Consumer;

public class DBHandler implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(DBHandler.class);

    private String insertSQL;
    private String updateSQL;
    private String dropTableSQL;
    private String createTableSQL;
    private String selectSQL;

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
        selectSQL = "select id, word, count from " + tableName;
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
        executeChangingQuery(dropTableSQL);
        executeChangingQuery(createTableSQL);
    }

    private void insertWord(String word, int count) throws DataBaseException {
        executeChangingQuery(insertSQL, word, count);
    }

    private void updateWord(String word, int count) throws DataBaseException {
        executeChangingQuery(updateSQL, word, count, word);
    }

    private void executeChangingQuery(String SQL, Object... params) throws DataBaseException {
        try (PreparedStatement statement = this.connection.prepareStatement(SQL)) {
            for (int i = 1; i <= params.length; i++) {
                statement.setObject(i, params[i-1]);
            }
            statement.execute();
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataBaseException("Execute error", ex);
        }
    }

    public void addWord(String word, int count) throws DataBaseException {
        updateWord(word, count);
        insertWord(word, count);
    }

    public void printWordsTable(String format, Consumer<String> printer) {
        logger.debug("formatString=\"{}\", query={}", format, selectSQL);
        try (Statement statement = this.connection.createStatement()) {
            ResultSet result = statement.executeQuery(selectSQL);
            while (result.next()) {
                printer.accept(String.format(format,
                        result.getInt(1),
                        result.getString(2),
                        result.getInt(3)));
            }
        } catch (SQLException ex) {
            printer.accept("print stopped by error: " + ex.getMessage());
            logger.error(ex.getMessage(), ex);
        }
    }

    public int getCountWords() throws DataBaseException {
        try (Statement statement = this.connection.createStatement()) {
            ResultSet result = statement.executeQuery("select count(*) from " + tableName);
            return result.getInt(1);
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataBaseException("Execute error", ex);
        }
    }

    public void commit() {
        try {
            if (connection != null) {
                logger.debug("committing to db");
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
