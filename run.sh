############ set properties ############
if [ -z "$SLEEPTIME" ]; then
    SLEEPTIME=0
    echo "SLEEPTIME set to $SLEEPTIME seconds"
fi

if [ -z "$DB_JDBC" ]; then
    DB_JDBC="jdbc:postgresql://dbhost/bms"
    echo "DB_JDBC set to $DB_JDBC"
fi

if [ -z "$DB_USER" ]; then
    DB_USER="postgres"
    echo "DB_USER set to $DB_USER"
fi

if [ -z "$DB_PASS" ]; then
    DB_PASS="postgres"
    echo "DB_PASS set to $DB_PASS"
fi

if [ -z "$DB_DRIVER" ]; then
    DB_DRIVER="org.postgresql.Driver"
    echo "JDBC driver set to $DB_DRIVER"
fi

if [ -z "$BASE_IRI" ]; then
    BASE_IRI="http://localhost/rdf/"
    echo "BASE_IRI set to $BASE_IRI"
fi

if [ -z "$R2RML_ENDPOINT" ]; then
    R2RML_ENDPOINT="http://graphdb:7200/repositories/r2rml"
    echo "R2RML_ENDPOINT set to $R2RML_ENDPOINT"
fi

if [ -z "$OUTPUT_ENDPOINT" ]; then
    OUTPUT_ENDPOINT="http://graphdb:7200/repositories/data"
    echo "OUTPUT_ENDPOINT set to $OUTPUT_ENDPOINT"
fi

if [ -z "$GRAPH_NAME" ]; then
    GRAPH_NAME="http://data.local/rdf"
    echo "GRAPH_NAME set to $GRAPH_NAME"
fi

echo "jdbc.url = $DB_JDBC" > ontop.properties
echo "jdbc.user = $DB_USER" >> ontop.properties
echo "jdbc.password = $DB_PASS" >> ontop.properties
echo "jdbc.driver = $DB_DRIVER" >> ontop.properties

############ run script ############

ontop/ontop bootstrap \
    -b "$BASE_IRI" \
    -p /ontop.properties \
    -m mapping.obda \
    -t ontology.owl

ontop/ontop mapping to-r2rml -i mapping.obda -o mapping.ttl -t ontology.owl
ontop/ontop mapping pretty-r2rml -i mapping.ttl -o mapping.ttl

rm mapping.obda
cd pyScripts && python3 enrich.py