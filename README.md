# Triplifier

This repository creates the tooling project for the Triplifier.

## Python package

The Python code is packaged for distribution. You can install it from GitHub Packages:

```bash
pip install --extra-index-url https://pypi.pkg.github.com/<OWNER> triplifier
```

After installation the `triplifier` command is available to run the tool.


## References
* Soest J van, Choudhury A, Gaikwad N, Sloep M, Dumontier M, Dekker A (2019) [Annotation of Existing Databases using Semantic Web Technologies: Making Data more FAIR.](http://ceur-ws.org/Vol-2849/#paper-11) CEUR-WS, Edinburgh, pp 94–101


## Run as command-line tool

The basic configuration for the Python port can be run with the following command:

```
triplifier -c triplifier.yaml
```

The YAML configuration file contains the database connection information using SQLAlchemy style URLs.

**SQLite database file**
```yaml
db:
  url: sqlite:///my.db
  user: user
  password: pass
```

**Postgres database file**
```yaml
db:
  url: "postgresql://postgres:postgres@localhost:5432/my_database"
```

### Optional arguments

By default, the tool will generate an ontology file (ontology.owl) and a turtle file containing the materialized triples (output.ttl) relative to the execution folder. To change this, the following additional arguments can be used:

* -o <output_path_for_materialized_triples_file>
* -t <output_path_for_ontology_file>
* -c <configuration_file>
* -b <base_uri_for_ontology>
* --ontologyAndOrData <ontology|data>

The `--ontologyAndOrData` option allows running only the ontology extraction
(`ontology`) or only the data materialization (`data`) given an ontology file.
If omitted, both steps are executed.
 
 ## Annotations using the result
 An example of annotations (and insertions) can be found in the following repository:
 * [https://gitlab.com/UM-CDS/fair/data/maastro-rectum](https://gitlab.com/UM-CDS/fair/data/maastro-rectum)
