package nl.um.cds.triplifier;

import java.sql.*;
import java.util.Map;

public class MainApp {
    String jdbcDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    String jdbcUrl = "jdbc:sqlserver://localhost:1433;databaseName=variandw1";
    String jdbcUser = "sa";
    String jdbcPass = "";

    public MainApp() {
        try {
            DatabaseInspector dbInspect = new DatabaseInspector(this.jdbcDriver, this.jdbcUrl, this.jdbcUser, this.jdbcPass);

            for(Map<String,String> tableName : dbInspect.getTableNames()) {
                System.out.println("Table name: " + tableName);

                for(String columnName : dbInspect.getColumnNames(tableName.get("name"))) {
                    System.out.println("    column: " + columnName);
                }

                for(String columnName : dbInspect.getPrimaryKeyColumns(tableName.get("catalog"), tableName.get("schema"), tableName.get("name"))) {
                    System.out.println("    PK column: " + columnName);
                }

                for(ForeignKeySpecification fkSpec : dbInspect.getForeignKeyColumns(tableName.get("catalog"), tableName.get("schema"), tableName.get("name"))) {
                    System.out.println("    FK column: " + fkSpec.foreignKeyColumn + " | " + fkSpec.primaryKeyTable + "." + fkSpec.primaryKeyColumn);
                }
            }
        } catch (SQLException e) {
            System.out.println("Could not connect to database with url " + this.jdbcUrl);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Could not find JDBC driver " + this.jdbcDriver);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MainApp mainApp = new MainApp();
    }
}
