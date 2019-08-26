package nl.um.cds.triplifier.rdf;

import nl.um.cds.triplifier.ForeignKeySpecification;
import nl.um.cds.triplifier.rdf.ontology.DBO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OntologyFactory {
    private Repository repo = null;
    private RepositoryConnection conn = null;
    private String baseIri = "";
    private ValueFactory vf = SimpleValueFactory.getInstance();

    public OntologyFactory(String baseIri) {
        this.baseIri = baseIri;
        this.initialize();
    }

    public OntologyFactory() {
        this("http://localhost/rdf/ontology/");
    }

    private void initialize() {
        this.repo = new SailRepository(new MemoryStore());
        this.repo.init();
        this.conn = repo.getConnection();

        this.conn.setNamespace("db", this.baseIri);
        DBO.addOntologyToDatabaseConnection(this.conn);
    }

    public void processTable(String tableName, List<String> columns, List<String> primaryKeys, List<ForeignKeySpecification> foreignKeys, String schemaName, String catalogName) {
        IRI tableClassIRI = this.getClassForTable(tableName);

        this.conn.add(tableClassIRI, RDF.TYPE, OWL.CLASS);
        this.conn.add(tableClassIRI, RDFS.SUBCLASSOF, DBO.DATABASETABLE);
        this.conn.add(tableClassIRI, RDFS.LABEL, vf.createLiteral(tableName));

        this.conn.add(tableClassIRI, DBO.TABLE, vf.createLiteral(tableName));
        if(catalogName != null) {
            this.conn.add(tableClassIRI, DBO.CATALOG, vf.createLiteral(catalogName));
        }
        if(schemaName != null) {
            this.conn.add(tableClassIRI, DBO.SCHEMA, vf.createLiteral(schemaName));
        }

        for(String column : columns) {
            this.addColumn(tableName, column);
        }

        this.addPrimaryKeys(tableName, primaryKeys);
        this.addForeignKeys(foreignKeys);
    }

    private IRI addColumn(String tableName, String column) {
        IRI columnClassIRI = this.getClassForColumn(tableName, column);

        this.conn.add(columnClassIRI, RDF.TYPE, OWL.CLASS);
        this.conn.add(columnClassIRI, RDFS.SUBCLASSOF, DBO.DATABASECOLUMN);
        this.conn.add(columnClassIRI, RDFS.LABEL, vf.createLiteral(tableName + "." + column));

        this.conn.add(columnClassIRI, DBO.TABLE, this.getClassForTable(tableName));
        this.conn.add(columnClassIRI, DBO.COLUMN, vf.createLiteral(column));

        return columnClassIRI;
    }

    private void addPrimaryKeys(String tableName, List<String> primaryKeys) {
        for(String pKeyColumn : primaryKeys) {
            IRI columnClassIRI = this.getClassForColumn(tableName, pKeyColumn);
            this.conn.add(columnClassIRI, RDFS.SUBCLASSOF, DBO.PRIMARYKEY);
        }
    }

    private void addForeignKeys(List<ForeignKeySpecification> foreignKeys) {

        for(ForeignKeySpecification fKeyColumn : foreignKeys) {
            IRI columnClassIRI = this.getClassForColumn(fKeyColumn.getForeignKeyTable(), fKeyColumn.getForeignKeyColumn());
            this.conn.add(columnClassIRI, RDFS.SUBCLASSOF, DBO.FOREIGNKEY);

            String predicateName = fKeyColumn.getForeignKeyTable() + "_" + fKeyColumn.getForeignKeyColumn() + "_refersTo_" + fKeyColumn.getPrimaryKeyTable() + "_" + fKeyColumn.getPrimaryKeyColumn();
            String predicateLabel = fKeyColumn.getForeignKeyTable() + "." + fKeyColumn.getForeignKeyColumn() + " refers to " + fKeyColumn.getPrimaryKeyTable() + "." + fKeyColumn.getPrimaryKeyColumn();

            IRI predicateIRI = vf.createIRI(this.baseIri, predicateName);
            this.conn.add(predicateIRI, RDF.TYPE, OWL.OBJECTPROPERTY);
            this.conn.add(predicateIRI, RDFS.LABEL, vf.createLiteral(predicateLabel));
            this.conn.add(predicateIRI, RDFS.SUBPROPERTYOF, DBO.COLUMNREFERENCE);

            IRI sourceIRI = this.addColumn(fKeyColumn.getForeignKeyTable(), fKeyColumn.getForeignKeyColumn());
            //create target IRI to be sure it exists
            IRI targetIRI = this.addColumn(fKeyColumn.getPrimaryKeyTable(), fKeyColumn.getPrimaryKeyColumn());
            this.conn.add(sourceIRI, RDFS.SUBCLASSOF, DBO.FOREIGNKEY);

            this.conn.add(predicateIRI, RDFS.DOMAIN, sourceIRI);
            this.conn.add(predicateIRI, RDFS.RANGE, targetIRI);
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
        TupleQuery tq = this.conn.prepareTupleQuery("PREFIX dbo: <" + DBO.NAMESPACE + "> SELECT * WHERE { ?tableClass rdfs:subClassOf dbo:DatabaseTable. OPTIONAL { ?tableClass dbo:schema ?schema. }. OPTIONAL { ?tableClass dbo:catalog ?catalog. }. ?tableClass dbo:table ?tableName. }");
        TupleQueryResult result = tq.evaluate();
        return result;
    }

    public TupleQueryResult getColumnsForTableFromOntology(String tableClassUri) {
        TupleQuery tq = this.conn.prepareTupleQuery("PREFIX dbo: <" + DBO.NAMESPACE + "> SELECT * WHERE { ?columnClassUri dbo:table <"+tableClassUri+">. ?columnClassUri dbo:column ?columnName. }");
        TupleQueryResult result = tq.evaluate();
        return result;
    }

    public TupleQueryResult getForeignKeyResults(String columnClassUri) {
        TupleQuery tq = this.conn.prepareTupleQuery("PREFIX dbo: <" + DBO.NAMESPACE + "> SELECT * WHERE { ?columnClassUri rdfs:subClassOf* dbo:ForeignKey. ?fkPredicate rdfs:domain ?columnClassUri. ?fkPredicate rdfs:range ?targetClassUri. BIND (<"+columnClassUri+"> AS ?columnClassUri). }");
        TupleQueryResult result = tq.evaluate();
        return result;
    }

    public TupleQueryResult getPrimaryKeysForTableFromOntology(String tableClassUri) {
        TupleQuery tq = this.conn.prepareTupleQuery("PREFIX dbo: <" + DBO.NAMESPACE + "> SELECT * WHERE { ?columnClassUri dbo:table <"+tableClassUri+">. ?columnClassUri dbo:column ?columnName. ?columnClassUri rdfs:subClassOf* dbo:PrimaryKey. }");
        TupleQueryResult result = tq.evaluate();
        return result;
    }
}
