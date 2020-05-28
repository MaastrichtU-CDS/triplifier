package nl.um.cds.triplifier;

import nl.um.cds.triplifier.rdf.DataFactory;
import nl.um.cds.triplifier.rdf.OntologyFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MainApp {

    public MainApp(String[] args) {
        String propertiesFilePath = "triplifier.properties";
        String ontologyFilePath = "ontology.owl";
        String outputFilePath = "output.ttl";
        boolean ontologyParsing = true;
        boolean dataParsing = true;

        String jdbcDriver = "";
        String jdbcUrl = "";
        String jdbcUser = "";
        String jdbcPass = "";

        String repoType = "memory";
        String repoUrl = "";
        String repoId = "";

        String baseUri = null;

        for(int i=0; i<args.length; i++) {
            if ("-p".equals(args[i])) {
                propertiesFilePath = args[i + 1];
            } else if ("-o".equals(args[i])) {
                outputFilePath = args[i + 1];
            } else if ("-t".equals(args[i])) {
                ontologyFilePath = args[i + 1];
            } else if ("-b".equals(args[i])) {
                baseUri = args[i + 1];
            } else if ("--ontologyAndOrData".equals(args[i])) {
                if ("ontology".equals(args[i+1])) {
                    dataParsing = false;
                } else if("data".equals(args[i+1])) {
                    ontologyParsing = false;
                }
            }
        }

        Properties props = new Properties();
        try {
            System.out.println(new File(propertiesFilePath).getAbsolutePath());
            FileInputStream fis = new FileInputStream(new File(propertiesFilePath));
            props.load(fis);

            jdbcDriver = props.getProperty("jdbc.driver");
            jdbcUrl = props.getProperty("jdbc.url");
            jdbcUser = props.getProperty("jdbc.user");
            jdbcPass = props.getProperty("jdbc.password");

            repoType = props.getProperty("repo.type");
            repoUrl = props.getProperty("repo.url");
            repoId = props.getProperty("repo.id");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        OntologyFactory of = new OntologyFactory();
        if(baseUri != null) {
            of = new OntologyFactory(baseUri);
        }
        DataFactory df = new DataFactory(of, repoType, repoUrl, repoId);

        try {
            if(ontologyParsing) {
                System.out.println("Start extracting ontology: " + System.currentTimeMillis());
                DatabaseInspector dbInspect = new DatabaseInspector(jdbcDriver, jdbcUrl, jdbcUser, jdbcPass);
                createOntology(dbInspect, of, ontologyFilePath);
                System.out.println("Done extracting ontology: " + System.currentTimeMillis());
                System.out.println("Ontology exported to " + ontologyFilePath);
            }

            if(dataParsing) {
                System.out.println("Start extracting data: " + System.currentTimeMillis());
                df.convertData(jdbcDriver, jdbcUrl, jdbcUser, jdbcPass);
                if ("memory".equals(repoType)) {
                    System.out.println("Start exporting data file: " + System.currentTimeMillis());
                    df.exportData(outputFilePath);
                    System.out.println("Data exported to " + outputFilePath);
                }
                System.out.println("Done: " + System.currentTimeMillis());

            }
        } catch (SQLException e) {
            System.out.println("Could not connect to database with url " + jdbcUrl);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MainApp mainApp = new MainApp(args);
    }

    private void createOntology(DatabaseInspector dbInspect, OntologyFactory of, String ontologyOutputFilePath) throws SQLException {
        for(Map<String,String> tableName : dbInspect.getTableNames()) {
            System.out.println("Table name: " + tableName);
            List<String> columns = dbInspect.getColumnNames(tableName.get("name"));
            List<String> primaryKeys = dbInspect.getPrimaryKeyColumns(tableName.get("catalog"), tableName.get("schema"), tableName.get("name"));
            List<ForeignKeySpecification> foreignKeys = dbInspect.getForeignKeyColumns(tableName.get("catalog"), tableName.get("schema"), tableName.get("name"));

            of.processTable(tableName.get("name"), columns, primaryKeys, foreignKeys, tableName.get("schema"), tableName.get("catalog"));
        }

        try {
            of.exportData(ontologyOutputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
