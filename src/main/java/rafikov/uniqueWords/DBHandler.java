package rafikov.uniqueWords;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rafikov.uniqueWords.exceptions.DataBaseException;

import java.io.File;
import java.sql.*;
import java.util.function.Consumer;

/**
 * Class for write unique words in database.
 * @author Aydar Rafikov
 */
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

    /**
     * Return single instance class.
     * @param baseName name database
     * @param tableName name table in database
     * @return DBHandler instance
     */
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

    /**
     * Connecting to DataBase.
     * @return this instance
     * @throws DataBaseException Cant open connection with DataBase.
     */
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

    /**
     * Create (overwrite) table.
     * @throws DataBaseException Cant execute query.
     */
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

    /**
     * Add word and count to DataBase.
     * If this word exists, past count adding with current.
     * @param word word
     * @param count count words
     * @throws DataBaseException Cant execute query.
     */
    public void addWord(String word, int count) throws DataBaseException {
        updateWord(word, count);
        insertWord(word, count);
    }


    /**
     * Method prints all unique words in table in printer.
     * @param format string format to write
     * @param printer string consumer
     */
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

    /**
     * Method gives count unique words for accepted table in DataBase.
     * @return count unique words
     * @throws DataBaseException Some problems with DataBase.
     */
    public int getCountWords() throws DataBaseException {
        try (Statement statement = this.connection.createStatement()) {
            ResultSet result = statement.executeQuery("select count(*) from " + tableName);
            return result.getInt(1);
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataBaseException("Execute error", ex);
        }
    }

    /**
     * Do committing changes in DataBase.
     */
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
