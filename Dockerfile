FROM openjdk:8

RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    curl \
    unzip \
    dos2unix

RUN python3 -m pip install rdflib requests

# install triplifier library
COPY ./triplifier-1.0-SNAPSHOT-jar-with-dependencies.jar triplifier.jar

# install run script
COPY run.sh /run.sh
RUN dos2unix run.sh

# set the run script
CMD ["bash", "run.sh"]