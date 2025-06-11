from rdflib import Namespace, RDF, RDFS, OWL

DBO = Namespace("http://um-cds/ontologies/databaseontology/")

DATABASETABLE = DBO.TableRow
DATABASECOLUMN = DBO.ColumnCell
DATABASECELL = DBO.Cell
PRIMARYKEY = DBO.PrimaryKey
FOREIGNKEY = DBO.ForeignKey

HAS_COLUMN = DBO.has_column
HAS_VALUE = DBO.has_value
HAS_CELL = DBO.has_cell
HAS_UNIT = DBO.has_unit
COLUMNREFERENCE = DBO.ColumnReference

TABLE = DBO.table
CATALOG = DBO.catalog
SCHEMA = DBO.schema
COLUMN = DBO.column


def add_ontology_to_graph(graph):
    graph.bind("dbo", DBO)
    graph.add((DATABASETABLE, RDF.type, OWL.Class))
    graph.add((DATABASECOLUMN, RDF.type, OWL.Class))
    graph.add((DATABASECELL, RDF.type, OWL.Class))
    graph.add((PRIMARYKEY, RDF.type, OWL.Class))
    graph.add((PRIMARYKEY, RDFS.subClassOf, DATABASECOLUMN))
    graph.add((FOREIGNKEY, RDF.type, OWL.Class))
    graph.add((FOREIGNKEY, RDFS.subClassOf, DATABASECOLUMN))

    graph.add((HAS_COLUMN, RDF.type, OWL.ObjectProperty))
    graph.add((HAS_COLUMN, RDFS.domain, DATABASETABLE))
    graph.add((HAS_COLUMN, RDFS.range, DATABASECOLUMN))

    graph.add((HAS_VALUE, RDF.type, OWL.ObjectProperty))
    graph.add((HAS_VALUE, RDFS.domain, DATABASECOLUMN))
    graph.add((HAS_VALUE, RDFS.range, DATABASECELL))

    graph.add((HAS_CELL, RDF.type, OWL.DatatypeProperty))
    graph.add((HAS_CELL, RDFS.domain, DATABASECOLUMN))
    graph.add((HAS_UNIT, RDF.type, OWL.AnnotationProperty))
    graph.add((HAS_UNIT, RDFS.domain, DATABASECOLUMN))

    graph.add((COLUMNREFERENCE, RDF.type, OWL.ObjectProperty))
    graph.add((COLUMNREFERENCE, RDFS.domain, DATABASECOLUMN))
    graph.add((COLUMNREFERENCE, RDFS.range, DATABASECOLUMN))

    graph.add((TABLE, RDF.type, OWL.AnnotationProperty))
    graph.add((TABLE, RDFS.range, DATABASETABLE))
    graph.add((TABLE, RDFS.domain, DATABASECOLUMN))
    graph.add((CATALOG, RDF.type, OWL.AnnotationProperty))
    graph.add((CATALOG, RDFS.domain, DATABASETABLE))
    graph.add((SCHEMA, RDF.type, OWL.AnnotationProperty))
    graph.add((SCHEMA, RDFS.domain, DATABASETABLE))
    graph.add((COLUMN, RDF.type, OWL.AnnotationProperty))
    graph.add((COLUMN, RDFS.domain, DATABASECOLUMN))
