!#!/bin/bash

# This script is used to test the Python tool in a Docker container

# Start the Docker container with a PostgreSQL database and a dummy database
cd containerTest && sh setupdb.sh && cd ..

# Wait for the database to be ready by checking connectivity
until docker exec postgresdb pg_isready -U postgres; do
    echo "Waiting for PostgreSQL to be ready..."
    sleep 2
done

# Create a test configuration file for the Python tool
echo "db:" > test.yaml
echo "  url: \"postgresql://postgres:postgres@localhost:5432/my_database\"" >> test.yaml

# Run the Python tool with the test configuration file
time python -m pythonTool.main_app -c test.yaml

# Check if the tool ran successfully
if [ $? -eq 0 ]; then
    echo "Python tool ran successfully. Output available in ontology.owl and output.ttl."
else
    echo "Python tool failed to run."
fi

# Clean up the test configuration file
rm test.yaml

# Stop the Docker container
docker stop postgresdb