import pandas as pd
import subprocess
import argparse

parser = argparse.ArgumentParser()
parser.add_argument("--csv_path", help="Path to csv file with github repositories data")
parser.add_argument("--output", help="Output directory")
args = parser.parse_args()

dataset = pd.read_csv(args.csv_path)
for project in dataset.name:
    p = subprocess.Popen(["git", "clone", f"https://github.com/{project}.git"],
                         cwd=args.output)
    p.wait()
