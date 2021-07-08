import itertools
import os
import sys
from collections import Counter
from typing import Tuple, Dict, List


def stat_to_row(stat: Tuple) -> str:
    return ",".join([stat[0], str(stat[1])]) + "\n"


def write_stats_to_csv(path_to_dir: str, filename: str, fq_names_count: Dict) -> str:
    csv_columns = ('FqName', 'Count')
    csv_file_path = os.path.join(path_to_dir, filename)
    with open(csv_file_path, 'w+') as csv_file:
        csv_file.write(stat_to_row(csv_columns))
        for fq_name_count in fq_names_count.items():
            csv_file.write(stat_to_row(fq_name_count))


def get_package(fq_name: str, prefix_len: int = 2, det="_"):
    fq_name_path_list = fq_name.split(".")
    fq_name_path_len = max(1, min(len(fq_name_path_list) - 1, prefix_len))
    return det.join(fq_name_path_list[:fq_name_path_len])


def get_longest_common_prefix(fq_names: List[str]):
    pref_len = len(fq_names[0])
    while pref_len >= 0:
        pref = fq_names[0][:pref_len + 1]
        if all([fq_name.startswith(pref) for fq_name in fq_names]):
            return pref.strip('.')
        pref_len -= 1
    return ""


def imports_stats(path_to_import_fq_names: str,
                  path_to_dir: str = "results"):
    with open(path_to_import_fq_names, 'r') as fq_names_file:
        fq_names = fq_names_file.read().split('\n')

    fq_names_count = Counter(fq_names)
    write_stats_to_csv(path_to_dir, "total.csv", fq_names_count)

    fq_names_by_prefix = itertools.groupby(fq_names, lambda fq_name: get_package(fq_name))
    for prefix, prefix_fq_names in fq_names_by_prefix:
        prefix_fq_names_count = Counter(prefix_fq_names)
        write_stats_to_csv(path_to_dir, f"{prefix}.csv", prefix_fq_names_count)

    fq_names_by_prefix = {prefix: list(prefix_fq_names) for prefix, prefix_fq_names in
                          itertools.groupby(fq_names, lambda fq_name: get_package(fq_name, det='.'))}
    fq_names_by_prefix_count = {get_longest_common_prefix(prefix_fq_names): len(prefix_fq_names) for
                                prefix, prefix_fq_names in fq_names_by_prefix.items()}
    write_stats_to_csv(path_to_dir, "total_by_prefix.csv", fq_names_by_prefix_count)


if __name__ == '__main__':
    path_to_fq_names = sys.argv[1]
    imports_stats(path_to_fq_names)
