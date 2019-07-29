echo .> ontology.owl
echo .> mapping.ttl

docker run --rm --link postgresdb:dbhost -v %cd%\ontology.owl:/ontology.owl -v %cd%\mapping.ttl:/mapping.ttl jvsoest/test