from typing import Dict, List, Any
from rdflib import URIRef, Literal, RDF

from .rdf_factory import RdfFactory
from .statement_collector import StatementCollector
from .ontology.dbo import DBO, HAS_COLUMN, HAS_CELL, HAS_VALUE
from .ontology_factory import OntologyFactory
import sqlite3


class DataFactory(RdfFactory):
    def __init__(self, ontology_factory: OntologyFactory, props: Dict[str, str]):
        super().__init__(props)
        hostname = "localhost"
        self.base_iri = props.get("repo.dataUri", f"http://{hostname}/rdf/data/")
        self.ontology_factory = ontology_factory
        self.value_index: Dict[str, Dict[str, List[URIRef]]] = {}
        self.initialize()

    def initialize(self) -> None:
        self.initialize_rdf_store()
        self.context = URIRef(self.base_iri)
        self.graph.bind("data", self.base_iri)

    def convert_data(self) -> None:
        url = self.props.get("jdbc.url")
        if not url or not url.startswith("jdbc:sqlite:"):
            raise ValueError("Only sqlite connections are supported in this python port")
        path = url.replace("jdbc:sqlite:", "")
        conn = sqlite3.connect(path)

        for table_info in self.ontology_factory.tables.values():
            table_class_uri = table_info["class"]
            table_name = table_info["class"].split("/")[-1]
            self.process_table(conn, table_class_uri, table_name)

        self.generate_foreign_key_relations()

    def process_table(self, conn: sqlite3.Connection, table_class_uri: URIRef, table_name: str) -> None:
        cursor = conn.cursor()
        cursor.execute(f"SELECT * FROM {table_name}")
        columns = [d[0] for d in cursor.description]
        row_id = 0
        for row in cursor.fetchall():
            row_iri = self.get_table_row_iri(table_name, table_class_uri, row_id, columns, row)
            collector = StatementCollector(self.context)
            collector.add_statement(row_iri, RDF.type, table_class_uri)
            self.process_columns(row, columns, table_class_uri, row_iri, collector)
            self.add_statements(collector.get_statements())
            collector.clear()
            row_id += 1

    def get_table_row_iri(
        self,
        table_name: str,
        table_class_uri: URIRef,
        row_id: int,
        columns: List[str],
        row: Any,
    ) -> URIRef:
        base_iri_table = self.base_iri + table_name + "/"
        iri = URIRef(base_iri_table + str(row_id))

        pk_columns = self.ontology_factory.tables[table_name]["primary_keys"]
        if pk_columns:
            pkey_value = "_".join(str(row[columns.index(pk)]) for pk in pk_columns)
            iri = URIRef(base_iri_table + pkey_value)
        return iri

    def process_columns(
        self,
        row: Any,
        columns: List[str],
        table_class_uri: URIRef,
        row_iri: URIRef,
        collector: StatementCollector,
    ) -> None:
        for col_name, value in zip(columns, row):
            column_class_uri = self.ontology_factory.get_class_for_column(
                table_class_uri.split("/")[-1], col_name
            )
            column_row_iri = URIRef(f"{row_iri}/{col_name}")
            collector.add_statement(column_row_iri, RDF.type, column_class_uri)
            collector.add_statement(row_iri, HAS_COLUMN, column_row_iri)
            if value is not None:
                value_node = URIRef(f"{column_row_iri}/value")
                collector.add_statement(column_row_iri, HAS_CELL, value_node)
                collector.add_statement(value_node, RDF.type, DBO.Cell)
                collector.add_statement(value_node, HAS_VALUE, Literal(str(value)))
                idx = self.value_index.setdefault(str(column_class_uri), {})
                idx.setdefault(str(value), []).append(row_iri)

    def generate_foreign_key_relations(self) -> None:
        for fk in self.ontology_factory.foreign_keys:
            source_idx = self.value_index.get(str(fk["source_class"]), {})
            target_idx = self.value_index.get(str(fk["target_class"]), {})
            for value, subjects in source_idx.items():
                targets = target_idx.get(value, [])
                for s in subjects:
                    for t in targets:
                        self.add_statement(s, fk["predicate"], t)

    def export_data(self, file_path: str) -> None:
        self.graph.serialize(destination=file_path, format="nt")
