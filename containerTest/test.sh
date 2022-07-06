touch ontology.owl
touch output.ttl

echo 'jdbc.url = jdbc:postgresql://dbhost/my_database' >> triplifier.properties
echo 'jdbc.user = postgres' >> triplifier.properties
echo 'jdbc.password = postgres' >> triplifier.properties
echo 'jdbc.driver = org.postgresql.Driver' >> triplifier.properties

docker run --rm \
    --link postgresdb:dbhost \
    -v $(pwd)/ontology.owl:/ontology.owl \
    -v $(pwd)/output.ttl:/output.ttl \
    -v $(pwd)/triplifier.properties:/triplifier.properties \
    ghcr.io/maastrichtu-cds/triplifier:latest

rm triplifier.properties