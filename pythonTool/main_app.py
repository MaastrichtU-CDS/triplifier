import argparse
import configparser
import logging
from pathlib import Path

from .db_inspector import DatabaseInspector
from .rdf.ontology_factory import OntologyFactory
from .rdf.data_factory import DataFactory
from .rdf.annotation_factory import AnnotationFactory


logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("triplifier")


def run_triplifier(args: argparse.Namespace) -> None:
    config = configparser.ConfigParser()
    config.read(args.properties)
    props = {k: v for section in config.sections() for k, v in config.items(section)}

    of = OntologyFactory(props, base_iri=args.baseuri)
    df = DataFactory(of, props)
    af = AnnotationFactory(props)

    db_inspect = DatabaseInspector(props)

    logger.info("Extracting ontology")
    for table in db_inspect.get_table_names():
        columns = db_inspect.get_column_names(table["name"])
        pks = db_inspect.get_primary_key_columns(table["catalog"], table["schema"], table["name"])
        fks = db_inspect.get_foreign_key_columns(table["catalog"], table["schema"], table["name"])
        of.process_table(table["name"], columns, pks, fks, table["schema"], table["catalog"])
    of.export_data(args.ontology)

    logger.info("Extracting data")
    df.convert_data()
    df.export_data(args.output)


def main():
    parser = argparse.ArgumentParser(description="Python Triplifier")
    parser.add_argument("-p", "--properties", default="triplifier.properties")
    parser.add_argument("-o", "--output", default="output.ttl")
    parser.add_argument("-t", "--ontology", default="ontology.owl")
    parser.add_argument("-b", "--baseuri", default=None)
    args = parser.parse_args()
    run_triplifier(args)


if __name__ == "__main__":
    main()
