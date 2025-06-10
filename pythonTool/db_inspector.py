import sqlite3
from typing import List, Dict
from .foreign_key_specification import ForeignKeySpecification


class DatabaseInspector:
    """Simplified database inspector using sqlite3."""

    def __init__(self, props: Dict[str, str]):
        self.props = props
        self.connection = None
        self.connect_database(
            props.get("jdbc.url"),
            props.get("jdbc.user"),
            props.get("jdbc.password"),
        )

    def connect_database(self, url: str, user: str, password: str) -> None:
        # Only supports SQLite connections for simplicity
        # jdbc.url expects format 'jdbc:sqlite:/path/to/db'
        if url and url.startswith("jdbc:sqlite:"):
            path = url.replace("jdbc:sqlite:", "")
            self.connection = sqlite3.connect(path)
        else:
            raise ValueError("Only sqlite URLs are supported in this python port")

    def get_table_names(self) -> List[Dict[str, str]]:
        cursor = self.connection.cursor()
        cursor.execute(
            "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name"
        )
        return [
            {"name": row[0], "catalog": None, "schema": None}
            for row in cursor.fetchall()
        ]

    def get_column_names(self, table_name: str) -> List[str]:
        cursor = self.connection.cursor()
        cursor.execute(f"PRAGMA table_info('{table_name}')")
        return [row[1] for row in cursor.fetchall()]

    def get_primary_key_columns(self, catalog: str, schema: str, table_name: str) -> List[str]:
        cursor = self.connection.cursor()
        cursor.execute(f"PRAGMA table_info('{table_name}')")
        return [row[1] for row in cursor.fetchall() if row[5] == 1]

    def get_foreign_key_columns(self, catalog: str, schema: str, table_name: str) -> List[ForeignKeySpecification]:
        cursor = self.connection.cursor()
        cursor.execute(f"PRAGMA foreign_key_list('{table_name}')")
        fk_list = []
        for row in cursor.fetchall():
            fk_list.append(
                ForeignKeySpecification(
                    primary_key_table=row[2],
                    primary_key_column=row[4],
                    foreign_key_table=table_name,
                    foreign_key_column=row[3],
                )
            )
        return fk_list
