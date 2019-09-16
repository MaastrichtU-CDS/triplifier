############ set properties ############
if [ -z "$SLEEPTIME" ]; then
    SLEEPTIME=0
    echo "SLEEPTIME set to $SLEEPTIME seconds"
    export SLEEPTIME
fi

if [ -z "$DB_JDBC" ]; then
    DB_JDBC="jdbc:postgresql://dbhost/bms"
    echo "DB_JDBC set to $DB_JDBC"
    export DB_JDBC
fi

if [ -z "$DB_USER" ]; then
    DB_USER="postgres"
    echo "DB_USER set to $DB_USER"
    export DB_USER
fi

if [ -z "$DB_PASS" ]; then
    DB_PASS="postgres"
    echo "DB_PASS set to $DB_PASS"
    export DB_PASS
fi

if [ -z "$DB_DRIVER" ]; then
    DB_DRIVER="org.postgresql.Driver"
    echo "JDBC driver set to $DB_DRIVER"
    export DB_DRIVER
fi

if [ -z "$BASE_IRI" ]; then
    BASE_IRI="http://localhost/rdf/"
    echo "BASE_IRI set to $BASE_IRI"
    export BASE_IRI
fi

if [ -z "$OUTPUT_ENDPOINT" ]; then
    OUTPUT_ENDPOINT="http://graphdb:7200/repositories/data"
    echo "OUTPUT_ENDPOINT set to $OUTPUT_ENDPOINT"
    export OUTPUT_ENDPOINT
fi

if [ -z "$GRAPH_NAME" ]; then
    GRAPH_NAME="http://data.local/rdf"
    echo "GRAPH_NAME set to $GRAPH_NAME"
    export GRAPH_NAME
fi

echo "jdbc.url = $DB_JDBC" > triplifier.properties
echo "jdbc.user = $DB_USER" >> triplifier.properties
echo "jdbc.password = $DB_PASS" >> triplifier.properties
echo "jdbc.driver = $DB_DRIVER" >> triplifier.properties

############ run script ############
if [ $SLEEPTIME = 0 ]; then
    java -jar triplifier.jar
else
    while true
    do
        java -jar triplifier.jar
        cd /pyScripts && python3 uploadData.py && cd /
        echo "================================== SLEEP =================================="
        sleep $SLEEPTIME
    done
fi
