docker run --rm --link postgresdb:dbhost ^
    --link graphdb:graphdb ^
    -e SLEEPTIME=60 ^
    jvsoest/test