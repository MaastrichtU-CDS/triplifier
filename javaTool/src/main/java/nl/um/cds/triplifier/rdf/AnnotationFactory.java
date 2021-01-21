package nl.um.cds.triplifier.rdf;

import nl.um.cds.triplifier.rdf.ontology.DBO;

import java.util.Properties;

public class AnnotationFactory extends RdfFactory{

    public AnnotationFactory(Properties props) {
        super(props);
        this.initialize();
    }

    private void initialize() {
        this.initializeRdfStore();
        this.context = vf.createIRI("http://annotation.local/");
    }
}
