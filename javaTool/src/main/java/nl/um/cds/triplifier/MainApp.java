package nl.um.cds.triplifier;

import java.sql.*;

public class MainApp {
    String jdbcDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    String jdbcUrl = "jdbc:sqlserver://hix-dwhds-01.ad.maastro.nl:1433;databaseName=Rectum_Sage";
    String jdbcUser = "usr_sage";
    String jdbcPass = "";

    public MainApp() {
        try {
            DatabaseInspector dbInspect = new DatabaseInspector(this.jdbcDriver, this.jdbcUrl, this.jdbcUser, this.jdbcPass);

            for(String tableName : dbInspect.getTableNames()) {
                System.out.println("Table name: " + tableName);

                for(String columnName : dbInspect.getColumnNames(tableName)) {
                    System.out.println("    column: " + columnName);
                }

                for(String columnName : dbInspect.getPrimaryKeyColumns(tableName)) {
                    System.out.println("    PK column: " + columnName);
                }

                for(ForeignKeySpecification fkSpec : dbInspect.getForeignKeyColumns(tableName)) {
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
