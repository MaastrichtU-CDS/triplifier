package nl.um.cds.triplifier.rdf;

import nl.um.cds.triplifier.ForeignKeySpecification;
import nl.um.cds.triplifier.rdf.ontology.DBO;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class OntologyFactory extends RdfFactory{
    private String baseIri = "";
    private Logger logger = Logger.getLogger(this.getClass());

    public OntologyFactory(String baseIri, Properties props) {
        this(props);
        this.baseIri = baseIri;
        this.initialize();
    }

    public OntologyFactory(Properties props) {
        super(props);

        String hostname = getHostname();
        this.baseIri = "http://" + hostname + "/rdf/ontology/";

        this.initialize();
    }

    private void initialize() throws RepositoryException {
        this.initializeRdfStore();
        this.context = vf.createIRI(this.baseIri);
        this.conn.setNamespace("db", this.baseIri);
        DBO.addOntologyToDatabaseConnection(this.conn);
    }

    public void processTable(String tableName, List<String> columns, List<String> primaryKeys, List<ForeignKeySpecification> foreignKeys, String schemaName, String catalogName) {
        IRI tableClassIRI = this.getClassForTable(tableName);

        this.addStatement(tableClassIRI, RDF.TYPE, OWL.CLASS);
        this.addStatement(tableClassIRI, RDFS.SUBCLASSOF, DBO.DATABASETABLE);
        this.addStatement(tableClassIRI, RDFS.LABEL, vf.createLiteral(tableName));

        this.addStatement(tableClassIRI, DBO.TABLE, vf.createLiteral(tableName));
        if(catalogName != null) {
            this.addStatement(tableClassIRI, DBO.CATALOG, vf.createLiteral(catalogName));
        }
        if(schemaName != null) {
            this.addStatement(tableClassIRI, DBO.SCHEMA, vf.createLiteral(schemaName));
        }

        for(String column : columns) {
            this.addColumn(tableName, column);
        }

        this.addPrimaryKeys(tableName, primaryKeys);
        this.addForeignKeys(foreignKeys);
    }

    private IRI addColumn(String tableName, String column) {
        IRI columnClassIRI = this.getClassForColumn(tableName, column);

        this.addStatement(columnClassIRI, RDF.TYPE, OWL.CLASS);
        this.addStatement(columnClassIRI, RDFS.SUBCLASSOF, DBO.DATABASECOLUMN);
        this.addStatement(columnClassIRI, RDFS.LABEL, vf.createLiteral(tableName + "." + column));

        this.addStatement(columnClassIRI, DBO.TABLE, this.getClassForTable(tableName));
        this.addStatement(columnClassIRI, DBO.COLUMN, vf.createLiteral(column));

        return columnClassIRI;
    }

    private void addPrimaryKeys(String tableName, List<String> primaryKeys) {
        for(String pKeyColumn : primaryKeys) {
            IRI columnClassIRI = this.getClassForColumn(tableName, pKeyColumn);
            this.addStatement(columnClassIRI, RDFS.SUBCLASSOF, DBO.PRIMARYKEY);
        }
    }

    private void addForeignKeys(List<ForeignKeySpecification> foreignKeys) {

        for(ForeignKeySpecification fKeyColumn : foreignKeys) {
            IRI columnClassIRI = this.getClassForColumn(fKeyColumn.getForeignKeyTable(), fKeyColumn.getForeignKeyColumn());
            this.addStatement(columnClassIRI, RDFS.SUBCLASSOF, DBO.FOREIGNKEY);

            IRI sourceIRI = this.addColumn(fKeyColumn.getForeignKeyTable(), fKeyColumn.getForeignKeyColumn());
            //create target IRI to be sure it exists
            IRI targetIRI = this.addColumn(fKeyColumn.getPrimaryKeyTable(), fKeyColumn.getPrimaryKeyColumn());
            this.addStatement(sourceIRI, RDFS.SUBCLASSOF, DBO.FOREIGNKEY);
        }
    }

    public IRI getClassForTable(String tableName) {
        String tableNameUriSafe = tableName.replaceAll(" ", "_");
        return vf.createIRI(this.baseIri, tableNameUriSafe);
    }

    public IRI getClassForColumn(String tableName, String columnName) {
        String tableNameUriSafe = tableName.replaceAll(" ", "_");
        String columnNameUriSafe = columnName.replaceAll(" ", "_");
        return vf.createIRI(this.baseIri, tableNameUriSafe + "." + columnNameUriSafe);
    }

    public void exportData(String filePathName) throws FileNotFoundException, IOException {
        RepositoryResult<Statement> result = this.conn.getStatements(null, null, null, true);
        FileOutputStream fos = new FileOutputStream(filePathName);

        RDFHandler rdfxmlWriter = new RDFXMLWriter(fos);
        this.conn.export(rdfxmlWriter);

        fos.flush();
        fos.close();
    }

    public TupleQueryResult getTablesFromOntology() {
        String sparqlQuery = "PREFIX dbo: <" + DBO.NAMESPACE + "> SELECT * WHERE { ?tableClass rdfs:subClassOf dbo:TableRow. OPTIONAL { ?tableClass dbo:schema ?schema. }. OPTIONAL { ?tableClass dbo:catalog ?catalog. }. ?tableClass dbo:table ?tableName. }";
        logger.debug(sparqlQuery);
        TupleQuery tq = this.conn.prepareTupleQuery(sparqlQuery);
        TupleQueryResult result = tq.evaluate();
        return result;
    }

    public TupleQueryResult getColumnsForTableFromOntology(String tableClassUri) {
        String sparqlQuery = "PREFIX dbo: <" + DBO.NAMESPACE + "> SELECT * WHERE { ?columnClassUri dbo:table <"+tableClassUri+">. ?columnClassUri dbo:column ?columnName. }";
        logger.debug(sparqlQuery);
        TupleQuery tq = this.conn.prepareTupleQuery(sparqlQuery);
        TupleQueryResult result = tq.evaluate();
        return result;
    }

    public TupleQueryResult getForeignKeys() {
        String sparqlQuery = "PREFIX dbo: <" + DBO.NAMESPACE + "> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT * WHERE { \n" +
                "            ?fkPredicate rdfs:subPropertyOf dbo:ColumnReference. \n" +
                "            ?fkPredicate rdfs:domain ?columnClassUri. \n" +
                "            ?fkPredicate rdfs:range ?targetClassUri. }";
        logger.debug(sparqlQuery);
        TupleQuery tq = this.conn.prepareTupleQuery(sparqlQuery);
        TupleQueryResult result = tq.evaluate();
        return result;
    }

    public TupleQueryResult getPrimaryKeysForTableFromOntology(String tableClassUri) {
        String sparqlQuery = "PREFIX dbo: <" + DBO.NAMESPACE + "> SELECT * WHERE { ?columnClassUri dbo:table <"+tableClassUri+">. ?columnClassUri dbo:column ?columnName. ?columnClassUri rdfs:subClassOf* dbo:PrimaryKey. }";
        logger.debug(sparqlQuery);
        TupleQuery tq = this.conn.prepareTupleQuery(sparqlQuery);
        TupleQueryResult result = tq.evaluate();
        return result;
    }
}
