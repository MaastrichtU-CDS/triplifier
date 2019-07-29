import rdflib

# Create stores
sourceGraph = rdflib.Graph()
targetGraph = rdflib.Graph()

sourceGraph.parse("/output.ttl", format="xml")
classUriList = sourceGraph.query("SELECT ?classUri WHERE { ?classUri rdf:type owl:Class. }")

def removeNamespace(uri):
    for namespace in sourceGraph.namespaces():
        uri = uri.replace(str(namespace[1]), "")
    return uri

def getNamespaceForUri(uri):
    for namespace in sourceGraph.namespaces():
        if uri.startswith(str(namespace[1])):
            return str(namespace[1])

def processClassInstances(classUri):
    instanceUriList = sourceGraph.query("SELECT ?instanceUri WHERE { ?instanceUri rdf:type <%s>. }" % classUri)
    for instanceUriRow in instanceUriList:
        instanceUri = instanceUriRow["instanceUri"]
        print(instanceUri)
        propertyList = sourceGraph.query("SELECT ?pred ?obj WHERE { <%s> ?pred ?obj . FILTER (?pred != rdf:type).}" % instanceUri)
        for propertyRow in propertyList:
            colName = removeNamespace(propertyRow["pred"])
            classNamespace = getNamespaceForUri(propertyRow["pred"])
            columnObject = instanceUri + "_" + colName
            dbColumnLabel = colName
            dbColumnObject = propertyRow["pred"] + "_class"
            dbValue = propertyRow["obj"]

            query = """
                PREFIX dbo: <http://um-cds/ontologies/databaseontology/>
                PREFIX owl: <http://www.w3.org/2002/07/owl#>
                INSERT {
                    ?instanceUri rdf:type ?instanceClass;
                                 dbo:has_column ?columnObject.
                    ?columnObject rdf:type dbo:DatabaseColumn, ?dbColumnObject;
                                  dbo:has_value ?dbValue.
                    
                    ?dbColumnObject rdf:type owl:Class;
                                    rdfs:label ?dbColumnLabel;
                                    rdfs:subClassOf dbo:DatabaseColumn.

                    ?instanceClass rdf:type owl:Class.
                    ?instanceClass rdfs:subClassOf dbo:DatabaseTable.
                } WHERE {
                    BIND(<%s> AS ?instanceUri).
                    BIND(<%s> AS ?instanceClass).
                    BIND(<%s> AS ?columnObject).
                    BIND(<%s> AS ?dbColumnObject).
                    BIND("%s" AS ?dbColumnLabel).
                    BIND("%s" AS ?dbValue).
                }
            """ % (instanceUri, classUri, columnObject, dbColumnObject, dbColumnLabel, dbValue)
            targetGraph.update(query)

for classUriRow in classUriList:
    print(classUriRow["classUri"])
    processClassInstances(classUriRow["classUri"])

targetGraph.update("""
    PREFIX dbo: <http://um-cds/ontologies/databaseontology/>
    PREFIX owl: <http://www.w3.org/2002/07/owl#>
    INSERT {
        dbo:has_column rdf:type owl:ObjectProperty.
        dbo:has_value rdf:type owl:DatatypeProperty.
        dbo:has_unit rdf:type owl:AnnotationProperty.
        dbo:DatabaseColumn rdf:type owl:Class.
        dbo:DatabaseTable rdf:type owl:Class.
    } WHERE {

    }
""")

outputTriples = targetGraph.serialize(format="turtle")
with open("/output_reform.ttl", 'w') as f:
    f.write(outputTriples.decode("utf-8"))