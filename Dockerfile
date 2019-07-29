FROM openjdk:8

RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    curl \
    unzip \
    dos2unix

RUN python3 -m pip install rdflib requests

# install ontop libraries
RUN mkdir /ontop
RUN curl -o /ontop/ontop-distribution-3.0.0-beta-2.zip -L https://sourceforge.net/projects/ontop4obda/files/ontop-3.0.0-beta-2/ontop-distribution-3.0.0-beta-2.zip/download
#COPY ontop-distribution-3.0.0-beta-2.zip /ontop/ontop-distribution-3.0.0-beta-2.zip
RUN cd /ontop && unzip ontop-distribution-3.0.0-beta-2.zip
RUN cd /ontop/jdbc && curl -o postgresql-42.2.6.jar https://jdbc.postgresql.org/download/postgresql-42.2.6.jar

# install python scripts
RUN mkdir /pyScripts
COPY pyScripts/enrich.py /pyScripts/enrich.py
COPY pyScripts/read_literal_properties.sparql /pyScripts/read_literal_properties.sparql
COPY pyScripts/working_query_domain_range.sparql /pyScripts/working_query_domain_range.sparql
COPY pyScripts/reform_triples.py /pyScripts/reform_triples.py
COPY pyScripts/uploadData.py /pyScripts/uploadData.py

# install run script
COPY run.sh /run.sh
COPY convertData.sh /convertData.sh
RUN dos2unix run.sh
RUN dos2unix convertData.sh

# set the run script
CMD ["bash", "run.sh"]