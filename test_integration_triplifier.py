import unittest
import subprocess
import os
import time
import yaml
import rdflib

class TriplifierIntegrationTest(unittest.TestCase):
    POSTGRES_CONTAINER = "postgresdb"
    DB_PORT = "5432"
    DB_USER = "postgres"
    DB_PASSWORD = "postgres"
    DB_NAME = "my_database"
    DB_URL = f"postgresql://{DB_USER}:{DB_PASSWORD}@localhost:{DB_PORT}/{DB_NAME}"
    SETUP_FOLDER = "containerTest"
    SETUP_SCRIPT = "setupdb.sh"
    CONFIG_FILE = "test.yaml"
    OUTPUT_FILES = ["output.ttl", "ontology.owl"]

    @classmethod
    def setUpClass(cls):
        # Start PostgreSQL container
        subprocess.run(["bash", cls.SETUP_SCRIPT], cwd=cls.SETUP_FOLDER, check=True)
        # Wait for DB to be ready
        for _ in range(30):
            # Check if PostgreSQL is ready
            result = subprocess.run([
                "docker", "exec", cls.POSTGRES_CONTAINER, "pg_isready", "-U", cls.DB_USER
            ], capture_output=True)
            if result.returncode == 0:
                # Check if the database exists and has tables
                db_check = subprocess.run([
                    "docker", "exec", cls.POSTGRES_CONTAINER,
                    "psql", "-U", cls.DB_USER, "-d", cls.DB_NAME,
                    "-c", "SELECT tablename FROM pg_tables WHERE schemaname='public';"
                ], capture_output=True, text=True)
                if db_check.returncode == 0 and "tablename" in db_check.stdout and len(db_check.stdout.strip().splitlines()) > 2:
                    break
            time.sleep(2)
        else:
            raise RuntimeError("PostgreSQL did not become ready in time")
        # Create config file
        with open(cls.CONFIG_FILE, "w") as f:
            yaml.dump({"db": {"url": cls.DB_URL}}, f)

    @classmethod
    def tearDownClass(cls):
        # Remove config file
        if os.path.exists(cls.CONFIG_FILE):
            os.remove(cls.CONFIG_FILE)
        # Stop PostgreSQL container
        subprocess.run(["docker", "stop", cls.POSTGRES_CONTAINER], check=False)

        # Remove the created output files
        for f in cls.OUTPUT_FILES:
            if os.path.exists(f):
                os.remove(f)

    def setUp(self):
        # Remove output files if they exist
        for f in self.OUTPUT_FILES:
            if os.path.exists(f):
                os.remove(f)

    def test_triplifier_outputs(self):
        # Run the triplifier tool
        result = subprocess.run([
            "python", "-m", "pythonTool.main_app", "-c", self.CONFIG_FILE
        ], capture_output=True, text=True)
        self.assertEqual(result.returncode, 0, f"Triplifier run failed: {result.stderr}")
        for f in self.OUTPUT_FILES:
            self.assertTrue(os.path.exists(f), f"{f} not generated")

        # Compare output.ttl
        g1 = rdflib.Graph()
        g2 = rdflib.Graph()
        g1.parse("output.ttl", format="turtle")
        g2.parse("test_output.ttl", format="turtle")

        # Compare if the graphs are isomorphic (structurally identical)
        isomorphic = g1.isomorphic(g2)
        if not isomorphic:
            missing_triples = set(g2) - set(g1)
            extra_triples = set(g1) - set(g2)
            msg = "output.ttl is not isomorphic to test_output.ttl\n"
            if missing_triples:
                msg += f"Missing triples in output.ttl:\n"
                for triple in missing_triples:
                    msg += f"  {triple}\n"
            if extra_triples:
                msg += f"Extra triples in output.ttl:\n"
                for triple in extra_triples:
                    msg += f"  {triple}\n"
            self.fail(msg)
        else:
            self.assertTrue(isomorphic, "output.ttl is not isomorphic to test_output.ttl")

        # Compare ontology.owl
        g1_owl = rdflib.Graph()
        g2_owl = rdflib.Graph()
        g1_owl.parse("ontology.owl", format="xml")
        g2_owl.parse("test_ontology.owl", format="xml")

        isomorphic_owl = g1_owl.isomorphic(g2_owl)
        if not isomorphic_owl:
            missing_triples_owl = set(g2_owl) - set(g1_owl)
            extra_triples_owl = set(g1_owl) - set(g2_owl)
            print("ontology.owl is not isomorphic to test_ontology.owl")
            if missing_triples_owl:
                print("Missing triples in ontology.owl:")
                for triple in missing_triples_owl:
                    print(f"  {triple}")
            if extra_triples_owl:
                print("Extra triples in ontology.owl:")
                for triple in extra_triples_owl:
                    print(f"  {triple}")
        self.assertTrue(isomorphic_owl, "ontology.owl is not isomorphic to test_ontology.owl")

if __name__ == "__main__":
    unittest.main()
