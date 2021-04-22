package nl.um.cds.triplifier.rdf;

import nl.um.cds.triplifier.rdf.ontology.DBO;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DataFactory extends RdfFactory{
    private final OntologyFactory ontologyFactory;
    private final String baseIri;
    private static final Logger logger = Logger.getLogger(DataFactory.class);

    public DataFactory(OntologyFactory ontologyFactory, Properties props) {
        super(props);
        String hostname = super.getHostname();

        this.baseIri = "http://" + hostname + "/rdf/data/";
        this.ontologyFactory = ontologyFactory;
        this.initialize();
    }

    private void initialize(){
        this.initializeRdfStore();
        this.context = vf.createIRI("http://data.local/");
        this.conn.setNamespace("data", this.baseIri);
    }

    public void convertData() {
        try {
            Connection conn = this.connectDatabase(
                    this.getProperty("jdbc.driver"),
                    this.getProperty("jdbc.url"),
                    this.getProperty("jdbc.user"),
                    this.getProperty("jdbc.password"));

            TupleQueryResult tableList = this.ontologyFactory.getTablesFromOntology();
            while(tableList.hasNext()) {
                BindingSet tableObject = tableList.next();
                String tableClassUri = tableObject.getValue("tableClass").stringValue();
                String tableName = tableObject.getValue("tableName").stringValue();

                String catalog = null;
                if(tableObject.hasBinding("catalog")) {
                    catalog = tableObject.getValue("catalog").stringValue();
                }

                String schema = null;
                if(tableObject.hasBinding("schema")) {
                    schema = tableObject.getValue("schema").stringValue();
                }

                this.processTable(conn, tableClassUri, tableName, catalog, schema);
            }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        //generateForeignKeyRelations();
    }

    private void generateForeignKeyRelations() {
        TupleQueryResult foreignKeys = ontologyFactory.getForeignKeys();
        while (foreignKeys.hasNext()) {
            BindingSet foreignKey = foreignKeys.next();
            String fkPredicate = foreignKey.getValue("fkPredicate").stringValue();
            String columnClassUri = foreignKey.getValue("columnClassUri").stringValue();
            String targetClassUri = foreignKey.getValue("targetClassUri").stringValue();

            String insertQuery = "PREFIX dbo: <http://um-cds/ontologies/databaseontology/>\n" +
                    "        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "\n" +
                    "        INSERT {\n" +
                    "            ?sources <"+fkPredicate+"> ?targets.\n" +
                    "        } WHERE {\n" +
                    "\n" +
                    "            ?sources rdf:type <"+columnClassUri+">;\n" +
                    "                     dbo:has_value ?columnValue.\n" +
                    "\n" +
                    "            ?targets rdf:type <"+targetClassUri+">;\n" +
                    "                     dbo:has_value ?columnValue.\n" +
                    "        }";
            logger.debug(insertQuery);
            Update update = this.conn.prepareUpdate(insertQuery);
            update.execute();
        }
    }

    private void processTable(Connection conn, String tableClassUri, String tableName, String catalog, String schema) {
        String query = "SELECT * FROM " + schema + "." + tableName;

        if(schema == null) {
            query = "SELECT * FROM " + tableName;
        }

        logger.info("Start processing table " + tableName);

        try {
            ResultSet sqlQueryResult = conn.prepareStatement(query).executeQuery();

            int resultRowId = 0;
            int numberRows = sqlQueryResult.getFetchSize();
            StatementCollector collector = new StatementCollector(this.context);

            while(sqlQueryResult.next()) {
                IRI tableRowIRI = this.getTableRowIRI(tableName, tableClassUri, resultRowId, sqlQueryResult);

                collector.addStatement(tableRowIRI, RDF.TYPE, vf.createIRI(tableClassUri));
                this.processColumns(sqlQueryResult, tableClassUri, tableRowIRI, collector);

                this.conn.add(collector.getStatements());
                collector.clearList();

                resultRowId++;
                logger.debug("Processed row " + resultRowId + " of " + numberRows);
            }
        } catch (SQLException e) {
            logger.warn("Could not execute query: " + query);
        }

    }

    public IRI getTableRowIRI(String tableName, String tableClassUri, int resultRowId, ResultSet queryResult) throws SQLException {
        String baseIriTable = this.baseIri + tableName + "/";
        IRI tableRowIRI = vf.createIRI(baseIriTable + resultRowId);

        TupleQueryResult pKeyList = this.ontologyFactory.getPrimaryKeysForTableFromOntology(tableClassUri);
        if(pKeyList.hasNext()) {
            String pKeyValue = "";
            boolean isFirst = true;
            while (pKeyList.hasNext()) {
                BindingSet pKey = pKeyList.next();
                if(!isFirst) {
                    pKeyValue += "_";
                } else {
                    isFirst = false;
                }

                String pKeyColumnName = pKey.getValue("columnName").stringValue();
                pKeyValue += queryResult.getString(pKeyColumnName);
            }
            tableRowIRI = vf.createIRI(baseIriTable + pKeyValue);
        }

        return tableRowIRI;
    }

    private void processColumns(ResultSet rowResults, String tableClassUri, IRI tableRowIRI, StatementCollector collector) throws SQLException {
        TupleQueryResult columnList = this.ontologyFactory.getColumnsForTableFromOntology(tableClassUri);
        while(columnList.hasNext()) {
            BindingSet columnResult = columnList.next();

            String columnClassUriString = columnResult.getValue("columnClassUri").stringValue();
            IRI columnClassUri = vf.createIRI(columnClassUriString);
            String columnName = columnResult.getValue("columnName").stringValue();

            IRI columnRowIRI = vf.createIRI(tableRowIRI.stringValue() + "/" + columnName.replaceAll(" ", "_"));
            String literalValue = rowResults.getString(columnName);

            collector.addStatement(columnRowIRI, RDF.TYPE, columnClassUri);
            collector.addStatement(tableRowIRI, DBO.HAS_COLUMN, columnRowIRI);
            // if there's no literal value for this column, then we can skip the creation of the column instance?
            if(literalValue != null) {
                IRI columnRowIRIValue = vf.createIRI(columnRowIRI.stringValue() + "/value");
                collector.addStatement(columnRowIRI, DBO.HAS_CELL, columnRowIRIValue);

                literalValue = StringEscapeUtils.escapeHtml(literalValue);
                collector.addStatement(columnRowIRIValue, RDF.TYPE, DBO.DATABASECELL);
                collector.addStatement(columnRowIRIValue, DBO.HAS_VALUE, vf.createLiteral(literalValue));
            }
        }
    }

    private Connection connectDatabase(String jdbcDriver, String jdbcUrl, String jdbcUser, String jdbcPass) throws SQLException, ClassNotFoundException {
        Class.forName(jdbcDriver);
        logger.debug("JDBC Driver loaded");

        Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass);
        logger.debug("Connected to database");

        return connection;
    }

    public void exportData(String filePathName) throws IOException {
        RepositoryResult<Statement> result = this.conn.getStatements(null, null, null, true);
        FileOutputStream fos = new FileOutputStream(filePathName);

        RDFHandler nTriplesWriter = new NTriplesWriter(fos);
        this.conn.export(nTriplesWriter);

        fos.flush();
        fos.close();
    }
}
