FROM openjdk:8

RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    curl \
    unzip \
    dos2unix

RUN python3 -m pip install rdflib

# install ontop libraries
RUN curl -o ontop-distribution-3.0.0-beta-2.zip -L https://sourceforge.net/projects/ontop4obda/files/ontop-3.0.0-beta-2/ontop-distribution-3.0.0-beta-2.zip/download
RUN unzip ontop-distribution-3.0.0-beta-2.zip

# install python scripts
RUN mkdir /pyScripts
COPY pyScripts/enrich.py /pyScripts/enrich.py

# install run script
COPY run.sh /run.sh
RUN dos2unix run.sh

# set the run script
CMD ["bash", "run.sh"]