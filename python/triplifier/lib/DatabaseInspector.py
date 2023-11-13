import os
import pandas as pd
import json

class DatabaseInspector():

    def get_csv_info(self, folder_path):
        folder_info = [ ]

        for filename in os.listdir(folder_path):
            if filename.endswith(".csv"):
                file_path = os.path.join(folder_path, filename)

                # Read CSV file using pandas
                df = pd.read_csv(file_path)

                # Extract column information
                columns_info = [{"name": col, "type": str(df[col].dtype)} for col in df.columns]

                folder_info.append({"name": filename, "columns": columns_info})

        return folder_info