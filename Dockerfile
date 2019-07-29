FROM openjdk:8

RUN apt-get update && apt-get install -y \
    python3 \
    curl \
    unzip

# install ontop libraries
RUN curl -o ontop-distribution-3.0.0-beta-2.zip -L https://sourceforge.net/projects/ontop4obda/files/ontop-3.0.0-beta-2/ontop-distribution-3.0.0-beta-2.zip/download