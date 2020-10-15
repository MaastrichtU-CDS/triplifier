package nl.um.cds.triplifier;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseInspector {
    private Connection connection = null;
    private DatabaseMetaData dbMetaData = null;
    private static Logger logger = Logger.getLogger(DatabaseInspector.class);

    public DatabaseInspector(String jdbcDriver, String jdbcUrl, String jdbcUser, String jdbcPass) {
        this.connectDatabase(jdbcDriver, jdbcUrl, jdbcUser, jdbcPass);
    }

    private void connectDatabase(String jdbcDriver, String jdbcUrl, String jdbcUser, String jdbcPass) {
        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            logger.error("Could not find JDBC driver name: " + jdbcDriver + ". Application will exit");
            System.exit(1);
        }
        logger.debug("JDBC Driver loaded");


        try {
            if (jdbcUrl.contains("integratedSecurity")) {
                logger.debug("Skipping username/password, as integratedSecurity is found");
                this.connection = DriverManager.getConnection(jdbcUrl);
            } else {
                this.connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass);
            }
        } catch (SQLException e) {
            logger.error("Could not connect to the database. Is there an active connection to the database? Are the credentials correct?");
            logger.error("JDBC URL used: " + jdbcUrl);
            e.printStackTrace();
            System.exit(2);
        }
        try {
            this.dbMetaData = this.connection.getMetaData();
        } catch (SQLException e) {
            logger.error("Could not get database metadata");
            System.exit(3);
        }

        logger.debug("Connected to database");
    }

    public List<Map<String,String>> getTableNames() throws SQLException {
        List<Map<String, String>> returnList = new ArrayList<Map<String, String>>();

        ResultSet rs = this.dbMetaData.getTables(null, null, null, new String[]{"TABLE"});

        while(rs.next()) {
            String tableName = rs.getString("TABLE_NAME");
            String catalog = rs.getString("TABLE_CAT");
            String schema = rs.getString("TABLE_SCHEM");

            Map<String, String> resultMap = new HashMap<String, String>();
            resultMap.put("name", tableName);
            resultMap.put("catalog", catalog);
            resultMap.put("schema", schema);

            returnList.add(resultMap);
        }

        return returnList;
    }

    public List<String> getColumnNames(String tableName) throws SQLException {
        List<String> returnList = new ArrayList<String>();

        ResultSet rsColumn = this.dbMetaData.getColumns(null, null, tableName, null);
        while(rsColumn.next()) {
            String columnName = rsColumn.getString("COLUMN_NAME");
            returnList.add(columnName);
        }

        return returnList;
    }

    public List<String> getPrimaryKeyColumns(String catalog, String schema, String tableName) throws SQLException {
        List<String> returnList = new ArrayList<String>();

        ResultSet rsColumn = this.dbMetaData.getPrimaryKeys(catalog, schema, tableName);
        while(rsColumn.next()) {
            String columnName = rsColumn.getString("COLUMN_NAME");
            returnList.add(columnName);
        }

        return returnList;
    }

    public List<ForeignKeySpecification> getForeignKeyColumns(String catalog, String schema, String tableName) throws SQLException {
        List<ForeignKeySpecification> returnList = new ArrayList<ForeignKeySpecification>();

        try {
            ResultSet rsColumn = this.dbMetaData.getImportedKeys(catalog, schema, tableName);
            while (rsColumn.next()) {
                ForeignKeySpecification fkSpec = new ForeignKeySpecification(
                        rsColumn.getString("PKTABLE_NAME"),
                        rsColumn.getString("PKCOLUMN_NAME"),
                        rsColumn.getString("FKTABLE_NAME"),
                        rsColumn.getString("PKCOLUMN_NAME")
                );
                returnList.add(fkSpec);
            }
        } catch (SQLException e) {
            logger.debug("No FK found for table " + tableName);
        }

        return returnList;
    }


}
