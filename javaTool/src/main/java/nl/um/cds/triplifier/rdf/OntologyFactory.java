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
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "PrimaryKey"), RDF.TYPE, OWL.CLASS);
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "PrimaryKey"), RDFS.SUBCLASSOF, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseColumn"));
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "ForeignKey"), RDF.TYPE, OWL.CLASS);
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "ForeignKey"), RDFS.SUBCLASSOF, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseColumn"));

        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "has_column"), RDF.TYPE, OWL.OBJECTPROPERTY);
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "has_column"), RDFS.DOMAIN, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseTable"));
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "has_column"), RDFS.RANGE, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseColumn"));

        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "has_value"), RDF.TYPE, OWL.DATATYPEPROPERTY);
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "has_value"), RDFS.DOMAIN, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseColumn"));
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "has_unit"), RDF.TYPE, OWL.ANNOTATIONPROPERTY);
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "has_unit"), RDFS.DOMAIN, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseColumn"));

        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "ColumnReference"), RDF.TYPE, OWL.OBJECTPROPERTY);
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "ColumnReference"), RDFS.DOMAIN, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseColumn"));
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "ColumnReference"), RDFS.RANGE, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseColumn"));

        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "table"), RDF.TYPE, OWL.ANNOTATIONPROPERTY);
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "table"), RDFS.DOMAIN, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseTable"));
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "table"), RDFS.DOMAIN, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseColumn"));
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "catalog"), RDF.TYPE, OWL.ANNOTATIONPROPERTY);
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "catalog"), RDFS.DOMAIN, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseTable"));
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "schema"), RDF.TYPE, OWL.ANNOTATIONPROPERTY);
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "schema"), RDFS.DOMAIN, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseTable"));
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "column"), RDF.TYPE, OWL.ANNOTATIONPROPERTY);
        this.conn.add(vf.createIRI(this.conn.getNamespace("dbo"), "column"), RDFS.DOMAIN, vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseColumn"));
    }

    public void processTable(String tableName, List<String> columns, List<String> primaryKeys, List<ForeignKeySpecification> foreignKeys, String schemaName, String catalogName) {
        IRI DbTableClassIRI = vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseTable");
        IRI DbColumnClassIRI = vf.createIRI(this.conn.getNamespace("dbo"), "DatabaseColumn");
        IRI tableClassIRI = this.getClassForTable(tableName);

        this.conn.add(tableClassIRI, RDF.TYPE, OWL.CLASS);
        this.conn.add(tableClassIRI, RDFS.SUBCLASSOF, DbTableClassIRI);
        this.conn.add(tableClassIRI, RDFS.LABEL, vf.createLiteral(tableName));

        this.conn.add(tableClassIRI, vf.createIRI(this.conn.getNamespace("dbo"), "table"), vf.createLiteral(tableName));
        this.conn.add(tableClassIRI, vf.createIRI(this.conn.getNamespace("dbo"), "catalog"), vf.createLiteral(catalogName));
        this.conn.add(tableClassIRI, vf.createIRI(this.conn.getNamespace("dbo"), "schema"), vf.createLiteral(schemaName));

        for(String column : columns) {
            IRI columnClassIRI = this.getClassForColumn(tableName, column);

            this.conn.add(columnClassIRI, RDF.TYPE, OWL.CLASS);
            this.conn.add(columnClassIRI, RDFS.SUBCLASSOF, DbColumnClassIRI);
            this.conn.add(columnClassIRI, RDFS.LABEL, vf.createLiteral(tableName + "." + column));

            this.conn.add(columnClassIRI, vf.createIRI(this.conn.getNamespace("dbo"), "table"), vf.createLiteral(tableName));
            this.conn.add(columnClassIRI, vf.createIRI(this.conn.getNamespace("dbo"), "column"), vf.createLiteral(column));
        }

        this.addPrimaryKeys(tableName, primaryKeys);
        this.addForeignKeys(foreignKeys);
    }

    private void addPrimaryKeys(String tableName, List<String> primaryKeys) {
        IRI primaryKeyClassIRI = vf.createIRI(this.conn.getNamespace("dbo"), "PrimaryKey");

        for(String pKeyColumn : primaryKeys) {
            IRI columnClassIRI = this.getClassForColumn(tableName, pKeyColumn);
            this.conn.add(columnClassIRI, RDF.TYPE, primaryKeyClassIRI);
        }
    }

    private void addForeignKeys(List<ForeignKeySpecification> foreignKeys) {
        IRI foreignKeyClassIRI = vf.createIRI(this.conn.getNamespace("dbo"), "ForeignKey");

        for(ForeignKeySpecification fKeyColumn : foreignKeys) {
            IRI columnClassIRI = this.getClassForColumn(fKeyColumn.getForeignKeyTable(), fKeyColumn.getForeignKeyColumn());
            this.conn.add(columnClassIRI, RDF.TYPE, foreignKeyClassIRI);

            String predicateName = fKeyColumn.getForeignKeyTable() + "_" + fKeyColumn.getForeignKeyColumn() + "_refersTo_" + fKeyColumn.getPrimaryKeyTable() + "_" + fKeyColumn.getPrimaryKeyColumn();
            IRI predicateIRI = vf.createIRI(this.baseIri, predicateName);
            this.conn.add(predicateIRI, RDF.TYPE, OWL.OBJECTPROPERTY);
            this.conn.add(predicateIRI, RDFS.SUBCLASSOF, vf.createIRI(this.conn.getNamespace("dbo"), "ColumnReference"));

            IRI sourceIRI = this.getClassForColumn(fKeyColumn.getForeignKeyTable(), fKeyColumn.getForeignKeyColumn());
            IRI targetIRI = this.getClassForColumn(fKeyColumn.getPrimaryKeyTable(), fKeyColumn.getPrimaryKeyColumn());

            //create target IRI to be sure it exists
            this.conn.add(targetIRI, RDF.TYPE, OWL.CLASS);

            this.conn.add(predicateIRI, RDFS.DOMAIN, sourceIRI);
            this.conn.add(predicateIRI, RDFS.DOMAIN, targetIRI);
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
}
