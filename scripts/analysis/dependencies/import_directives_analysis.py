import argparse
import itertools
import json
import os
import sys
from collections import Counter
from typing import Tuple, Dict, List

from column_names_utils import ImportDirectivesColumn
from fq_names_tree import build_fq_name_tree, save_to_txt, delete_rare_nodes, delete_extra_child_nodes, \
    merge_single_child_nodes, save_to_png, split_to_subtrees, FqNamesDict
from utils import Extensions, get_file_lines, create_directory
from visualization.diagram import show_bar_plot

"""
This script process mined imports directive list and runs analysis and visualization.
Usage: import_directives_analysis.py [-h] [--input INPUT] [--ignore IGNORE] 
                           [--max-package-len MAX_PACKAGE_LEN] [--max-subpackages MAX_SUBPACKAGES]
                           [--max-leaf-subpackages MAX_LEAF_SUBPACKAGES] [--min-occurrence MIN_OCCURRENCE] 
                           [--max-occurrence MAX_OCCURRENCE] [--max-u-occurrence MAX_U_OCCURRENCE]
Run with -h flag to get the description of the above flags.
The results will be placed in result directory.
Output: 
1. "total.csv" and plotly bar chart with raw import dependencies fq names occurrence statistics
2. "total_by_prefix.csv" and plotly bar chart with import dependencies fq names occurrence statistics grouped by package 
   -- name's prefix MAX_PACKAGE_LEN length
3. "total_by_package.csv" and plotly bar chart with import dependencies fq names occurrence statistics grouped by package 
   prefix got by building fq names tree and detecting
   import dependencies packages names
4. "root_small.png" - dot packages tree with occurrence on edges, build from import dependencies names according to 
   filters and simplifications with selected parameters (MAX_SUBPACKAGES, MAX_LEAF_SUBPACKAGES, MIN_OCCURRENCE, 
   MAX_OCCURRENCE, MAX_U_OCCURRENCE)
5. "root_{package_name}.png" - dot import dependencies fq names subtrees with occurrence on edges for each package
6. "fq_names_tree.txt" - file with text fq names tree representation
7. "fq_names_tree.json" - file with json fq names tree representation
"""


def stat_to_row(stat: Tuple) -> str:
    return ",".join([stat[0], str(stat[1])]) + "\n"


def write_stats_to_csv(path_to_dir: str, filename: str, fq_names_dict: FqNamesDict):
    csv_file_path = os.path.join(path_to_dir, filename)
    with open(csv_file_path, 'w+') as csv_file:
        csv_file.write(stat_to_row((ImportDirectivesColumn.FQ_NAME.value, ImportDirectivesColumn.COUNT.value)))
        for fq_name_count in fq_names_dict.items():
            csv_file.write(stat_to_row(fq_name_count))


def get_package_by_len(fq_name: str, prefix_len: int) -> str:
    fq_name_path_list = fq_name.split(".")
    fq_name_path_len = max(1, min(len(fq_name_path_list) - 1, prefix_len))
    return ".".join(fq_name_path_list[:fq_name_path_len])


def get_matched_package(fq_name: str, packages: List[str], prefix_len: int) -> str:
    max_package = ""
    for package in packages:
        if fq_name.startswith(package) and len(max_package) < len(package):
            max_package = package
    return get_package_by_len(fq_name, prefix_len) if max_package == "" else max_package


def get_longest_common_prefix(fq_names: List[str]) -> str:
    fq_names = [fq_name.split('.') for fq_name in fq_names]
    max_prefix_len = 0
    while all(len(fq_name) > max_prefix_len for fq_name in fq_names) and \
            all(fq_name[max_prefix_len] == fq_names[0][max_prefix_len] for fq_name in fq_names):
        max_prefix_len += 1
    return ".".join(fq_names[0][:max_prefix_len])


def fq_names_to_csv(fq_names: List[str],
                    path_to_result_dir: str):
    fq_names_count = Counter(fq_names)
    write_stats_to_csv(path_to_result_dir, f"total.{Extensions.CSV}", fq_names_count)
    show_bar_plot(os.path.join(path_to_result_dir, f"total.{Extensions.CSV}"),
                  ImportDirectivesColumn.FQ_NAME,
                  ImportDirectivesColumn.COUNT)


def fq_names_by_prefix_to_csv(fq_names: List[str],
                              path_to_result_dir: str,
                              prefix_len: int):
    fq_names_by_prefix = {prefix: list(prefix_fq_names) for prefix, prefix_fq_names in
                          itertools.groupby(sorted(fq_names), lambda fq_name: get_package_by_len(fq_name, prefix_len))}

    fq_names_by_prefix_count = {get_longest_common_prefix(prefix_fq_names): len(prefix_fq_names) for
                                prefix, prefix_fq_names in fq_names_by_prefix.items()}
    write_stats_to_csv(path_to_result_dir, f"total_by_prefix.{Extensions.CSV}", fq_names_by_prefix_count)
    show_bar_plot(os.path.join(path_to_result_dir, f"total_by_prefix.{Extensions.CSV}"),
                  ImportDirectivesColumn.FQ_NAME,
                  ImportDirectivesColumn.COUNT)


def fq_names_by_packages_to_csv(fq_names: List[str],
                                path_to_result_dir: str,
                                packages: List[str],
                                prefix_len: int):
    fq_names_by_package = {prefix: len(list(prefix_fq_names)) for prefix, prefix_fq_names in
                           itertools.groupby(sorted(fq_names),
                                             lambda fq_name: get_matched_package(fq_name, packages, prefix_len))}
    write_stats_to_csv(path_to_result_dir, f"total_by_package.{Extensions.CSV}", fq_names_by_package)
    show_bar_plot(os.path.join(path_to_result_dir, f"total_by_package.{Extensions.CSV}"),
                  ImportDirectivesColumn.FQ_NAME,
                  ImportDirectivesColumn.COUNT)


def fq_names_group_to_dict(fq_names_group: List[str]) -> Dict:
    cnt = len(fq_names_group)
    if cnt == 1 or cnt == len(set(fq_names_group)):
        return {ImportDirectivesColumn.COUNT: cnt}

    fq_names_map = fq_names_to_dict(
        list(map(lambda fq_name: ".".join(fq_name.split(".")[1:]), fq_names_group)))
    fq_names_map[ImportDirectivesColumn.COUNT] = cnt
    return fq_names_map


def fq_names_to_dict(fq_names: List[str]) -> Dict:
    fq_names = list(filter(lambda fq_name: fq_name != "", fq_names))
    return {prefix: fq_names_group_to_dict(list(fq_names_group)) for
            prefix, fq_names_group in
            itertools.groupby(sorted(fq_names), lambda fq_name: fq_name.split(".")[0])}


def fq_names_to_json(fq_names_dict: FqNamesDict,
                     path_to_result_dir: str):
    with open(os.path.join(path_to_result_dir, f"fq_names_tree.{Extensions.JSON}"), 'w') as result_file:
        json.dump(fq_names_dict, result_file)


def fq_names_to_tree(fq_names_dict: FqNamesDict, path_to_result_dir: str,
                     max_subpackages: int, max_leaf_subpackages: int,
                     min_occurrence: int, max_occurrence: int, max_u_occurrence: int) -> List[str]:
    root = build_fq_name_tree(fq_names_dict)
    save_to_txt(root, path_to_result_dir)

    delete_rare_nodes(root, min_occurrence)
    delete_extra_child_nodes(root, max_subpackages)
    merge_single_child_nodes(root)

    sub_roots = split_to_subtrees(root, max_leaf_subpackages, max_occurrence, max_u_occurrence)

    save_to_png(root, path_to_result_dir)
    for sub_root in sub_roots:
        save_to_png(sub_root, path_to_result_dir)
    return [subroot.str_path(sep=".", with_root=False) for subroot in sub_roots]


def filter_fq_names(fq_names: List[str], filter_packages: List[str]) -> List[str]:
    return list(filter(lambda fq_name: "." in fq_name and
                                       not any(fq_name.startswith(package) for package in filter_packages), fq_names))


def analyze(path_to_fq_names: str, path_to_ignored_packages: str,
            max_package_len: int, max_subpackages: int, max_leaf_subpackages: int,
            min_occurrence: int, max_occurrence: int, max_u_occurrence: int,
            path_to_result_dir: str = "result"):
    create_directory(path_to_result_dir)

    if path_to_ignored_packages is not None:
        ignored_packages = get_file_lines(path_to_ignored_packages)
    else:
        ignored_packages = []

    fq_names = get_file_lines(path_to_fq_names)
    fq_names = filter_fq_names(fq_names, ignored_packages)

    fq_names_dict = fq_names_to_dict(fq_names)
    fq_names_to_csv(fq_names, path_to_result_dir)
    fq_names_by_prefix_to_csv(fq_names, path_to_result_dir, max_package_len)
    fq_names_to_json(fq_names_dict, path_to_result_dir)

    packages = fq_names_to_tree(fq_names_dict, path_to_result_dir,
                                max_subpackages, max_leaf_subpackages,
                                min_occurrence, max_occurrence, max_u_occurrence)
    fq_names_by_packages_to_csv(fq_names, path_to_result_dir, packages, max_package_len)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--input', type=str, help='path to csv file with fq_names')
    parser.add_argument('--ignore', type=str, default=None, help='path to csv file with imports to ignore')
    parser.add_argument('--max-package-len', type=int, default=3,
                        help='max length of package name to group by')
    parser.add_argument('--max-subpackages', type=int, default=20,
                        help='max number of subpackages occurrence to visualize, ignore extra with least count')
    parser.add_argument('--max-leaf-subpackages', type=int, default=0.7,
                        help='max percent of leaf subpackages to consider path as package')
    parser.add_argument('--min-occurrence', type=int, default=5,
                        help='min number of packages occurrence to analyze, ignore if less')
    parser.add_argument('--max-occurrence', type=int, default=200,
                        help='max number of packages occurrence to visualize in main tree, '
                             'draw as a separate tree if less')
    parser.add_argument('--max-u-occurrence', type=int, default=50,
                        help='max number of unique packages occurrence to visualize in main tree, '
                             'draw as a separate tree if less')
    args = parser.parse_args(sys.argv[1:])

    analyze(args.input, args.ignore,
            args.max_package_len, args.max_subpackages, args.max_leaf_subpackages,
            args.min_occurrence, args.max_occurrence, args.max_u_occurrence)
