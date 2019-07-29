ontop/ontop bootstrap \
    -b "$BASE_IRI" \
    -p /ontop.properties \
    -m mapping.obda \
    -t ontology.owl

ontop/ontop mapping to-r2rml -i mapping.obda -o mapping.ttl -t ontology.owl
ontop/ontop mapping pretty-r2rml -i mapping.ttl -o mapping.ttl

# enrich the ontology
rm mapping.obda
cd pyScripts && python3 enrich.py

# make entities of database columns (instead of predicates)
cd /

ontop/ontop materialize \
    -m mapping.ttl \
    -o output.ttl \
    -t ontology.owl \
    -p ontop.properties

cd pyScripts && python3 reform_triples.py