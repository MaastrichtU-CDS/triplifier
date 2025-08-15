package nl.um.cds.triplifier.rdf.ontology;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class DBO {
    public static final String NAMESPACE = "http://um-cds/ontologies/databaseontology/";
    public static final IRI DATABASETABLE = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "TableRow");
    public static final IRI DATABASECOLUMN = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "ColumnCell");
    public static final IRI DATABASECELL = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "Cell");
    public static final IRI PRIMARYKEY = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "PrimaryKey");
    public static final IRI FOREIGNKEY = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "ForeignKey");

    public static final IRI HAS_COLUMN = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "has_column");
    public static final IRI HAS_VALUE = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "has_value");
    public static final IRI HAS_CELL = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "has_cell");
    public static final IRI HAS_UNIT = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "has_unit");
    // public static final IRI COLUMNREFERENCE = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "ColumnReference");
    public static final IRI HAS_TARGET_COLUMN = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "has_target_column");
    public static final IRI FK_REFERS_TO = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "fk_refers_to");

    public static final IRI TABLE = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "table");
    public static final IRI CATALOG = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "catalog");
    public static final IRI SCHEMA = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "schema");
    public static final IRI COLUMN = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "column");

    public static void addOntologyToDatabaseConnection(RepositoryConnection conn) {
        conn.setNamespace("dbo", NAMESPACE);

        conn.add(DATABASETABLE, RDF.TYPE, OWL.CLASS);
        conn.add(DATABASECOLUMN, RDF.TYPE, OWL.CLASS);
        conn.add(PRIMARYKEY, RDF.TYPE, OWL.CLASS);
        conn.add(PRIMARYKEY, RDFS.SUBCLASSOF, DATABASECOLUMN);
        conn.add(FOREIGNKEY, RDF.TYPE, OWL.CLASS);
        conn.add(FOREIGNKEY, RDFS.SUBCLASSOF, DATABASECOLUMN);

        conn.add(HAS_COLUMN, RDF.TYPE, OWL.OBJECTPROPERTY);
        conn.add(HAS_COLUMN, RDFS.DOMAIN, DATABASETABLE);
        conn.add(HAS_COLUMN, RDFS.RANGE, DATABASECOLUMN);

        conn.add(HAS_VALUE, RDF.TYPE, OWL.OBJECTPROPERTY);
        conn.add(HAS_VALUE, RDFS.DOMAIN, DATABASECOLUMN);
        conn.add(HAS_VALUE, RDFS.RANGE, DATABASECELL);

        conn.add(HAS_CELL, RDF.TYPE, OWL.DATATYPEPROPERTY);
        conn.add(HAS_CELL, RDFS.DOMAIN, DATABASECOLUMN);
        conn.add(HAS_UNIT, RDF.TYPE, OWL.ANNOTATIONPROPERTY);
        conn.add(HAS_UNIT, RDFS.DOMAIN, DATABASECOLUMN);

        conn.add(HAS_TARGET_COLUMN, RDF.TYPE, OWL.ANNOTATEDPROPERTY);
        conn.add(HAS_TARGET_COLUMN, RDFS.DOMAIN, DATABASECOLUMN);

        conn.add(FK_REFERS_TO, RDF.TYPE, OWL.OBJECTPROPERTY);
        conn.add(FK_REFERS_TO, RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral("foreign key refers to"));
        conn.add(FK_REFERS_TO, RDFS.DOMAIN, DATABASECOLUMN);
        conn.add(FK_REFERS_TO, RDFS.RANGE, PRIMARYKEY);

        conn.add(TABLE, RDF.TYPE, OWL.ANNOTATIONPROPERTY);
        conn.add(TABLE, RDFS.RANGE, DATABASETABLE);
        conn.add(TABLE, RDFS.DOMAIN, DATABASECOLUMN);
        conn.add(CATALOG, RDF.TYPE, OWL.ANNOTATIONPROPERTY);
        conn.add(CATALOG, RDFS.DOMAIN, DATABASETABLE);
        conn.add(SCHEMA, RDF.TYPE, OWL.ANNOTATIONPROPERTY);
        conn.add(SCHEMA, RDFS.DOMAIN, DATABASETABLE);
        conn.add(COLUMN, RDF.TYPE, OWL.ANNOTATIONPROPERTY);
        conn.add(COLUMN, RDFS.DOMAIN, DATABASECOLUMN);
    }

}
