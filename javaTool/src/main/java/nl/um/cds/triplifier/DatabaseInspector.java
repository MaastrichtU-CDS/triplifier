package nl.um.cds.triplifier;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseInspector {
    private Connection connection = null;
    private DatabaseMetaData dbMetaData = null;

    public DatabaseInspector(String jdbcDriver, String jdbcUrl, String jdbcUser, String jdbcPass) throws SQLException, ClassNotFoundException {
        this.connectDatabase(jdbcDriver, jdbcUrl, jdbcUser, jdbcPass);
    }

    private void connectDatabase(String jdbcDriver, String jdbcUrl, String jdbcUser, String jdbcPass) throws SQLException, ClassNotFoundException {
        Class.forName(jdbcDriver);
        System.out.println("JDBC Driver loaded");

        this.connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass);
        this.dbMetaData = this.connection.getMetaData();

        System.out.println("Connected to database");
    }

    public List<Map<String,String>> getTableNames() throws SQLException {
        List<Map<String, String>> returnList = new ArrayList<Map<String, String>>();

        ResultSet rs = this.dbMetaData.getTables(null, null, null, new String[]{"TABLE"});

        while(rs.next()) {
            String tableName = rs.getString("TABLE_NAME");
            String catalog = rs.getString("TABLE_CAT");
            String schema = rs.getString("TABLE_SCHEM");
            System.out.println("Found table: " + tableName);

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

        ResultSet rsColumn = this.dbMetaData.getExportedKeys(catalog, schema, tableName);
        while(rsColumn.next()) {
            ForeignKeySpecification fkSpec = new ForeignKeySpecification(
                    rsColumn.getString("PKTABLE_NAME"),
                    rsColumn.getString("PKCOLUMN_NAME"),
                    rsColumn.getString("FKTABLE_NAME"),
                    rsColumn.getString("PKCOLUMN_NAME")
            );
            returnList.add(fkSpec);
        }

        return returnList;
    }


}
