package nl.um.cds.triplifier.rdf;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.List;

public class StatementCollector {
    private IRI contextIRI;
    private List<Statement> statements;
    private ValueFactory vf;

    public StatementCollector() {
        this.contextIRI = null;
        this.statements = new ArrayList<>();
        this.vf = SimpleValueFactory.getInstance();
    }

    public StatementCollector(IRI contextIRI) {
        this();
        this.contextIRI = contextIRI;
    }

    public void addStatement(IRI subject, IRI predicate, IRI object) {
        if (this.contextIRI != null) {
            this.statements.add(this.vf.createStatement(subject, predicate, object, this.contextIRI));
        } else {
            this.statements.add(this.vf.createStatement(subject, predicate, object));
        }
    }

    public void addStatement(Resource subject, IRI predicate, Value object) {
        if (this.contextIRI != null) {
            this.statements.add(this.vf.createStatement(subject, predicate, object, this.contextIRI));
        } else {
            this.statements.add(this.vf.createStatement(subject, predicate, object));
        }
    }

    public Iterable<Statement> getStatements() {
        return statements;
    }

    public void clearList() {
        this.statements.clear();
    }

}
