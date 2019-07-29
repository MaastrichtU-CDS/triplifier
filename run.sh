sh ontop bootstrap \
    -b "http://maastro.nl/projects/bms" \
    -p properties.properties \
    -m mapping.obda \
    -t ontology.owl

sh ontop mapping to-r2rml -i mapping.obda -o mapping.ttl -t ontology.owl
sh ontop mapping pretty-r2rml -i mapping.ttl -o mapping.ttl

rm mapping.obda
python3 pyScripts/enrich.py