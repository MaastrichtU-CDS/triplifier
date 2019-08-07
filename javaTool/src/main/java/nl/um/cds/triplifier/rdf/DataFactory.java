package nl.um.cds.triplifier.rdf;

import nl.um.cds.triplifier.rdf.ontology.DBO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataFactory {
    private Repository repo = null;
    private RepositoryConnection conn = null;
    private OntologyFactory ontologyFactory = null;
    private String baseIri = "";
    private ValueFactory vf = SimpleValueFactory.getInstance();

    public DataFactory(OntologyFactory ontologyFactory) {
        this("http://localhost/rdf/data/", ontologyFactory);
    }

    public DataFactory(String baseIri, OntologyFactory ontologyFactory) {
        this.baseIri = baseIri;
        this.ontologyFactory = ontologyFactory;
        this.initialize();
    }

    private void initialize() {
        this.repo = new SailRepository(new MemoryStore());
        this.repo.init();
        this.conn = repo.getConnection();

        this.conn.setNamespace("data", this.baseIri);
    }

    public void convertData(String jdbcDriver, String jdbcUrl, String jdbcUser, String jdbcPass) {
        try {
            Connection conn = this.connectDatabase(jdbcDriver, jdbcUrl, jdbcUser, jdbcPass);

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

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void processTable(Connection conn, String tableClassUri, String tableName, String catalog, String schema) {
        String query = "SELECT * FROM " + schema + "." + tableName;

        if(schema == null) {
            query = "SELECT * FROM " + tableName;
        }

        try {
            ResultSet queryResult = conn.prepareStatement(query).executeQuery();

            int resultRowId = 0;
            while(queryResult.next()) {
                String baseIriTable = this.baseIri + tableName + "/";

                IRI tableRowIRI = vf.createIRI(baseIriTable + resultRowId);
                this.conn.add(tableRowIRI, RDF.TYPE, vf.createIRI(tableClassUri));
                this.processColumns(queryResult, tableClassUri, tableRowIRI);

                resultRowId++;
            }
        } catch (SQLException e) {
            System.out.println("Could not execute query: " + query);
        }

    }

    private void processColumns(ResultSet rowResults, String tableClassUri, IRI tableRowIRI) throws SQLException {
        TupleQueryResult columnList = this.ontologyFactory.getColumnsForTableFromOntology(tableClassUri);
        while(columnList.hasNext()) {
            BindingSet columnResult = columnList.next();

            IRI columnClassUri = vf.createIRI(columnResult.getValue("columnClassUri").stringValue());
            String columnName = columnResult.getValue("columnName").stringValue();

            IRI columnRowIRI = vf.createIRI(tableRowIRI.stringValue() + "/" + columnName);
            this.conn.add(columnRowIRI, RDF.TYPE, columnClassUri);
            this.conn.add(columnRowIRI, DBO.HAS_VALUE, vf.createLiteral(rowResults.getString(columnName)));
            this.conn.add(tableRowIRI, DBO.HAS_COLUMN, columnRowIRI);
        }
    }

    private Connection connectDatabase(String jdbcDriver, String jdbcUrl, String jdbcUser, String jdbcPass) throws SQLException, ClassNotFoundException {
        Class.forName(jdbcDriver);
        System.out.println("JDBC Driver loaded");

        Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass);
        System.out.println("Connected to database");

        return connection;
    }

    public void exportData(String filePathName) throws FileNotFoundException, IOException {
        RepositoryResult<Statement> result = this.conn.getStatements(null, null, null, true);
        FileOutputStream fos = new FileOutputStream(filePathName);

        RDFHandler nTriplesWriter = new NTriplesWriter(fos);
        this.conn.export(nTriplesWriter);

        fos.flush();
        fos.close();
    }
}
