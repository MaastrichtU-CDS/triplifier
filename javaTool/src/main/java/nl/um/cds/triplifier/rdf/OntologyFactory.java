package nl.um.cds.triplifier.rdf;

import nl.um.cds.triplifier.ForeignKeySpecification;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.URIUtil;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.util.List;

public class OntologyFactory {
    private Repository repo = null;
    private RepositoryConnection conn = null;
    private String baseIri = "";
    private ValueFactory vf = SimpleValueFactory.getInstance();

    public OntologyFactory(String baseIri) {
        this.initialize();
        this.baseIri = baseIri;
    }

    public OntologyFactory() {
        this("http://localhost/rdf/ontology/");
    }

    private void initialize() {
        this.repo = new SailRepository(new MemoryStore());
        this.repo.init();
        this.conn = repo.getConnection();

        this.conn.setNamespace("dbo", "http://um-cds/ontologies/databaseontology/");
        this.conn.setNamespace("db", this.baseIri);

        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseTable"), RDF.TYPE, OWL.CLASS);
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseColumn"), RDF.TYPE, OWL.CLASS);

        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "has_column"), RDF.TYPE, OWL.OBJECTPROPERTY);
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "has_column"), RDFS.DOMAIN, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseTable"));
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "has_column"), RDFS.RANGE, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseColumn"));

        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "has_value"), RDF.TYPE, OWL.DATATYPEPROPERTY);
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "has_value"), RDFS.DOMAIN, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseColumn"));
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "has_unit"), RDF.TYPE, OWL.ANNOTATIONPROPERTY);
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "has_unit"), RDFS.DOMAIN, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseColumn"));
    }

    public void processTable(String tableName, List<String> columns, List<String> primaryKeys, List<ForeignKeySpecification> foreignKeys) {
        IRI DbTableClassIRI = vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseTable");
        IRI DbColumnClassIRI = vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseColumn");
        IRI tableClassIRI = this.getClassForTable(tableName);

        this.conn.add(tableClassIRI, RDF.TYPE, OWL.CLASS);
        this.conn.add(tableClassIRI, RDFS.SUBCLASSOF, DbTableClassIRI);
        this.conn.add(tableClassIRI, RDFS.LABEL, vf.createLiteral(tableName));

        for(String column : columns) {
            IRI columnClassIRI = this.getClassForColumn(tableName, column);

            this.conn.add(columnClassIRI, RDF.TYPE, OWL.CLASS);
            this.conn.add(columnClassIRI, RDFS.SUBCLASSOF, DbColumnClassIRI);
            this.conn.add(columnClassIRI, RDFS.LABEL, vf.createLiteral(tableName + "." + column));
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

    public void exportData() {
        //TODO: implement export
        TupleQuery q = this.conn.prepareTupleQuery("SELECT * WHERE { ?s ?p ?o. }");
        TupleQueryResult result = q.evaluate();
        while(result.hasNext()) {
            BindingSet set = result.next();
            System.out.println("S: " + set.getValue("s") + " | P: " + set.getValue("p") + " | O: " + set.getValue("o"));
        }
    }
}
