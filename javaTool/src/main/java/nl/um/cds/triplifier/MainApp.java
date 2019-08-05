package nl.um.cds.triplifier;

import nl.um.cds.triplifier.rdf.OntologyFactory;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;

public class MainApp {
//    String jdbcDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
//    String jdbcUrl = "jdbc:sqlserver://hix-dwhds-01.ad.maastro.nl:1433;databaseName=Rectum_Sage";
//    String jdbcUser = "usr_sage";
//    String jdbcPass = "";
    String jdbcDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    String jdbcUrl = "jdbc:sqlserver://localhost:1433;databaseName=variandw1";
    String jdbcUser = "sa";
    String jdbcPass = "";

    public MainApp() {
        OntologyFactory of = new OntologyFactory();
        try {
            DatabaseInspector dbInspect = new DatabaseInspector(this.jdbcDriver, this.jdbcUrl, this.jdbcUser, this.jdbcPass);
            createOntology(dbInspect, of);
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

    private void createOntology(DatabaseInspector dbInspect, OntologyFactory of) throws SQLException {
        for(Map<String,String> tableName : dbInspect.getTableNames()) {
            System.out.println("Table name: " + tableName);
            List<String> columns = dbInspect.getColumnNames(tableName.get("name"));
            List<String> primaryKeys = dbInspect.getPrimaryKeyColumns(tableName.get("catalog"), tableName.get("schema"), tableName.get("name"));
            List<ForeignKeySpecification> foreignKeys = dbInspect.getForeignKeyColumns(tableName.get("catalog"), tableName.get("schema"), tableName.get("name"));

            of.processTable(tableName.get("name"), columns, primaryKeys, foreignKeys, tableName.get("schema"), tableName.get("catalog"));
        }

        try {
            of.exportData("C:\\Users\\johan\\Documents\\Repositories\\FAIR\\triplifier\\javaTool\\ontology.owl");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
