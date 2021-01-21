package nl.um.cds.triplifier.rdf;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

abstract class RdfFactory {
    private Properties props;
    private Repository repo = null;
    private static final Logger logger = Logger.getLogger(RdfFactory.class);
    RepositoryConnection conn = null;
    ValueFactory vf = SimpleValueFactory.getInstance();
    IRI context;

    public RdfFactory(Properties props) {
        this.props = props;
    }

    String getHostname() {
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return hostname;
    }

    public void dropDataGraph() {
        logger.info("Clearing context " + this.context.stringValue());
        this.conn.clear(this.context);
    }

    void addStatement(IRI subject, IRI predicate, IRI object) {
        if (this.context != null) {
            this.conn.add(this.vf.createStatement(subject, predicate, object, this.context));
        } else {
            this.conn.add(this.vf.createStatement(subject, predicate, object));
        }
    }

    void addStatement(IRI subject, IRI predicate, Value object) {
        if (this.context != null) {
            this.conn.add(this.vf.createStatement(subject, predicate, object, this.context));
        } else {
            this.conn.add(this.vf.createStatement(subject, predicate, object));
        }
    }

    String getProperty(String key) {
        return props.getProperty(key);
    }

    void initializeRdfStore() {
        String repoType = props.getProperty("repo.type");
        String repoUrl = props.getProperty("repo.url");
        String repoId = props.getProperty("repo.id");
        String repoUser = props.getProperty("repo.user");
        String repoPass = props.getProperty("repo.password");

        switch (repoType) {
            case "memory":
                this.repo = new SailRepository(new MemoryStore());
                this.repo.init();
                break;
            case "rdf4j":
                HTTPRepository httpRepo = new HTTPRepository(repoUrl, repoId);
                if (!("".equals(repoUser) || repoUser == null)) {
                    httpRepo.setUsernameAndPassword(repoUser, repoPass);
                }
                this.repo = httpRepo;
                break;
            case "sparql":
                this.repo = new SPARQLRepository(repoUrl);
                break;
        }

        this.conn = repo.getConnection();
    }
}
