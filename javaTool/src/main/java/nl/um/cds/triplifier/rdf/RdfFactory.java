package nl.um.cds.triplifier.rdf;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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
        // Priority 1: Check for custom hostname specified via command line
        String customHostname = props.getProperty("custom.hostname");
        if (customHostname != null && !customHostname.trim().isEmpty()) {
            return customHostname;
        }

        // Priority 2: Try to get actual hostname
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // Priority 3: Fallback to localhost (already set above)
        return hostname;
    }

    /**
     * Clear the repository, or only the given context (which is the actual scope of the factory)
     * @param completeRepository boolean indicating to clean the whole repository (value = true) or only the current context/graph (value = false)
     */
    public void clearData(boolean completeRepository) {
        if (completeRepository) {
            logger.info("Clearing whole repository");
            this.conn.clear();
        } else {
            logger.info("Clearing context " + this.context.stringValue());
            this.conn.clear(this.context);
        }
    }

    /**
     * Return all statements within the set context. If context is not given, all statements in repository are returned.
     * @return List object containing Statement instances
     */
    public List<Statement> getAllStatementsInContext() {
        RepositoryResult<Statement> statements = this.conn.getStatements(null, null, null, this.context);
        List<Statement> returnStatements = new ArrayList<Statement>();

        while(statements.hasNext()) {
            returnStatements.add(statements.next());
        }

        return returnStatements;
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

    /**
     * Add multiple statements to current repository (and current context if given in class constructor)
     * @param statements List of statements to be added
     */
    public void addStatements(Iterable<Statement> statements) {
        this.conn.add(statements, this.context);
    }

    String getProperty(String key) {
        return props.getProperty(key);
    }

    void initializeRdfStore() throws RepositoryException {
        String repoType = props.getProperty("repo.type", "");
        String repoUrl = props.getProperty("repo.url");
        String repoId = props.getProperty("repo.id");
        String repoUser = props.getProperty("repo.user");
        String repoPass = props.getProperty("repo.password");

        switch (repoType) {
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
            case "memory":
            default:
                this.repo = new SailRepository(new MemoryStore());
                this.repo.init();
                break;
        }

        this.conn = repo.getConnection();

        int attempts = 0;
        while (!this.connectionExists() && attempts < 10) {
            attempts += 1;
            logger.info("Could not connect to repository, waiting 10 seconds for a new attempt ("+ attempts +" of 10)");
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.conn = repo.getConnection();
        }

        if(!this.connectionExists()) {
            throw new RepositoryException("Could not connect to " + repoUrl + " with repository ID " + repoId);
        }
    }

    boolean connectionExists() {
        try {
            this.conn.getNamespaces();
            return true;
        } catch (RepositoryException ex) {
            return false;
        }
    }
}
