import json
import requests
import urllib
import os
import subprocess
import sys
from subprocess import Popen, PIPE

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
query = "DROP GRAPH <%s>" % graphName
dropRequest = requests.put(transactionUrl + "?action=UPDATE&update=%s" % query)
commitRequest = requests.put(transactionUrl + "?action=COMMIT")

#####################
# Load RDF store with new data
#####################

turtle = ""
with open('/output.ttl', 'r') as myfile:
    turtle=myfile.read()

loadRequest = requests.post((outputEndpoint + "/statements?context=%3C" + graphName + "%3E"),
    data=turtle, 
    headers={
        "Content-Type": "application/x-turtle"
    }
)
