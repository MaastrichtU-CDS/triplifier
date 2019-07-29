#This script can be used to add domains and ranges to the triples 
# created by the automatic bootstrapping of the datasource using ontop.
import rdflib
import re

# Create stores
r2rml = rdflib.Graph()
ontology = rdflib.Graph()

# Fill in-memory stores
r2rml.parse("mapping.ttl", format="n3")
ontology.parse("ontology.owl")

# Read query and remove comments
with open('working_query_domain_range.sparql', 'r') as file:
    query = file.read()
    query = re.sub('(#\s).*\n', ' ',query).replace('\n', ' ')

# Run query
queryResult = r2rml.query(query)

# Loop over result rows
for result in queryResult:
    # Add domain triple to ontology store
    ontology.add([result["predicate"], rdflib.term.URIRef("http://www.w3.org/2000/01/rdf-schema#domain"), result["domainClass"]])
    # Add range triple to ontology store
    ontology.add([result["predicate"], rdflib.term.URIRef("http://www.w3.org/2000/01/rdf-schema#range"), result["rangeClass"]])

# Read query for domain - predicates for literals
with open('read_literal_properties.sparql', 'r') as file:
    query = file.read()
    query = re.sub('(#\s).*\n', ' ',query).replace('\n', ' ')

# Run query
queryResultLiterals = r2rml.query(query)

for result in queryResultLiterals:
    # print(result)
    # Add domain triple to ontology store
    ontology.add([result["predicate"], rdflib.term.URIRef("http://www.w3.org/2000/01/rdf-schema#domain"), result["domainClass"]])

# Export and overwrite updated ontology store
ontologyUpdatedString = ontology.serialize(format="pretty-xml")
with open("ontology.owl", 'w') as f:
    f.write(ontologyUpdatedString.decode("utf-8"))