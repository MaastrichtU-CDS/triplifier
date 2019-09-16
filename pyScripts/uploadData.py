import json
import requests
import urllib
import os
import subprocess
import sys
from subprocess import Popen, PIPE
from datetime import datetime

outputEndpoint = os.environ["OUTPUT_ENDPOINT"]
graphName = os.environ["GRAPH_NAME"]

#####################
# Clear RDF store
#####################
transactionRequest = requests.post(outputEndpoint + "/transactions", 
    headers={
        "Accept": "application/json"
    }
)
transactionUrl = transactionRequest.headers["Location"]
#query = "DROP GRAPH <%s>" % graphName
query = "DROP ALL;"

print("start dropping database: " + str(datetime.now()))
dropRequest = requests.put(transactionUrl + "?action=UPDATE&update=%s" % query)
commitRequest = requests.put(transactionUrl + "?action=COMMIT")
print("done dropping database: " + str(datetime.now()))

#####################
# Load RDF store with new data
#####################

num_lines = sum(1 for line in open('/output.ttl'))
print("Number of triples: %s" % num_lines)

print("Read ttl file: " + str(datetime.now()))
turtle = ""
with open('/output.ttl', 'r') as myfile:
    turtle=myfile.read()

print("Start uploading triples: " + str(datetime.now()))
loadRequest = requests.post((outputEndpoint + "/statements?context=%3C" + graphName + "%3E"),
    data=turtle, 
    headers={
        "Content-Type": "application/x-turtle"
    }
)
print("Done uploading triples: " + str(datetime.now()))
