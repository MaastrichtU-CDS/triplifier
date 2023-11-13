# in the folder above, run:
#   python -m tests.test_inspect.py

import unittest
import json
from lib import DatabaseInspector

class TestModule(unittest.TestCase):
    def test_read_csv(self):
        db_inspector = DatabaseInspector.DatabaseInspector()
        csv_metadata = db_inspector.get_csv_info("./tests")
        answer = [
            {
                "name": "iris.csv", 
                "columns": [
                    {
                        "name": "sepal_length",
                        "type": "float64"
                    },
                    {
                        "name": "sepal_width",
                        "type": "float64"
                    },
                    {
                        "name": "petal_length",
                        "type": "float64"
                    },
                    {
                        "name": "petal_width",
                        "type": "float64"
                    },
                    {
                        "name": "species",
                        "type": "object"
                    }
                ]
            }
        ]
        self.assertListEqual(csv_metadata, answer)

if __name__ == '__main__':
    unittest.main()