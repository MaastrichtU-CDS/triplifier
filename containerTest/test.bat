echo .> ontology.owl
echo .> mapping.ttl
echo .> output_reform.ttl
echo .> output.ttl

docker run --rm --link postgresdb:dbhost ^
    -v %cd%\ontology.owl:/ontology.owl ^
    -v %cd%\mapping.ttl:/mapping.ttl ^
    -v %cd%\output_reform.ttl:/output_reform.ttl ^
    -v %cd%\output.ttl:/output.ttl ^
    registry.gitlab.com/um-cds/fair/tools/triplifier:remoteEndpoint
