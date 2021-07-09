import sys
from collections import Counter


def count_imports_stats(path_to_import_fq_names: str, path_to_stats: str = "result.csv"):
    csv_columns = ['FqName', 'Count']

    with open(path_to_import_fq_names, 'r') as fq_names_file:
        fq_names = fq_names_file.read().split('\n')
        fq_names_count = Counter(fq_names)

    with open(path_to_stats, 'w') as csv_file:
        csv_file.write(",".join(csv_columns) + "\n")
        for fq_name_count in fq_names_count.items():
            csv_file.write(",".join([fq_name_count[0], str(fq_name_count[1])]) + "\n")


if __name__ == '__main__':
    path_to_fq_names = sys.argv[1]
    count_imports_stats(path_to_fq_names)
