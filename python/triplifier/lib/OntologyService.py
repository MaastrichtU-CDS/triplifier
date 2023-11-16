from datasources.triples import RDFLibSource
from pathlib import Path
import rdflib as rdf

class OntologyService(RDFLibSource):
    def __init__(self, base_iri: str = "http://data.local/ontology/", path: Path = None, graph: rdf.Graph = None) -> None:
        RDFLibSource.__init__(self, path, graph)
        self.__base_iri = base_iri