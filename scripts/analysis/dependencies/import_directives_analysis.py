import argparse
import itertools
import json
import os
import sys
from collections import Counter
from typing import Tuple, List, Callable

from column_names_utils import ImportDirectivesColumn
from fq_names_tree import save_to_txt, save_to_png, build_fq_name_tree_decomposition, FqNameNode
from fq_names_types import FqNamesStats, FqNamesGroups, FqNamesDict
from utils import Extensions, get_file_lines, create_directory
from visualization.diagram import show_bar_plot

"""
This script process mined imports directive list and runs analysis and visualization.
Usage: import_directives_analysis.py [-h] --input INPUT --output OUTPUT [--ignore IGNORE] [--max-package-len MAX_PACKAGE_LEN] [--max-subpackages MAX_SUBPACKAGES]
                                     [--max-leaf-subpackages MAX_LEAF_SUBPACKAGES] [--min-occurrence MIN_OCCURRENCE] [--max-occurrence MAX_OCCURRENCE]
                                     [--max-u-occurrence MAX_U_OCCURRENCE] [--show-dot-trees SHOW_DOT_TREES] [--show-txt-trees SHOW_TXT_TREES]
                                     [--show-bar-plots SHOW_BAR_PLOTS] [--show-csv SHOW_CSV] [--show-package-csv SHOW_PACKAGE_CSV]

Run with -h flag to get the description of the above flags.
The results will be placed in result directory.
Output: 
1. "total.csv" and plotly bar chart with raw import dependencies fq names occurrence statistics
2. "total_by_prefix.csv" and plotly bar chart with import dependencies fq names occurrence statistics grouped by package 
   -- name's prefix MAX_PACKAGE_LEN length
3. "total_by_package.csv" and plotly bar chart with import dependencies fq names occurrence statistics grouped by package 
   prefix got by building fq names tree and detecting
   import dependencies packages names
4. "root.png" - dot packages tree with occurrence on edges, build from import dependencies names according to 
   filters and simplifications with selected parameters (MAX_SUBPACKAGES, MAX_LEAF_SUBPACKAGES, MIN_OCCURRENCE, 
   MAX_OCCURRENCE, MAX_U_OCCURRENCE)
5. "{package_name}.png" - dot import dependencies fq names subtrees with occurrence on edges for each package
6. "root.txt" - txt packages tree with occurrence on edges
7. "{package_name}.txt" - txt import dependencies fq names subtrees with occurrence on edges for each package

The examples of each representation you can see in module README.md file.
"""


def stat_to_row(stat: Tuple) -> str:
    return ",".join([stat[0], str(stat[1])]) + "\n"


def save_stats_to_csv(path_to_dir: str, filename: str, fq_names_stats: FqNamesStats):
    csv_file_path = os.path.join(path_to_dir, filename)
    with open(csv_file_path, 'w+') as csv_file:
        csv_file.write(stat_to_row((ImportDirectivesColumn.FQ_NAME.value, ImportDirectivesColumn.COUNT.value)))
        for fq_name_count in fq_names_stats.items():
            csv_file.write(stat_to_row(fq_name_count))


def save_stats_to_bar_plot(fq_names_stats: FqNamesStats, title: str):
    show_bar_plot(fq_names_stats,
                  ImportDirectivesColumn.FQ_NAME.value,
                  ImportDirectivesColumn.COUNT.value,
                  title)


def get_package_by_len(fq_name: str, prefix_len: int) -> str:
    fq_name_path_list = fq_name.split(".")
    fq_name_path_len = max(1, min(len(fq_name_path_list) - 1, prefix_len))
    return ".".join(fq_name_path_list[:fq_name_path_len])


def get_prefix_by_package(fq_name: str, packages: List[str], prefix_len: int) -> str:
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


def group_fq_names_by(fq_names: List[str], group_by_function: Callable[[str], str]) -> FqNamesGroups:
    return {group_name: list(group_members) for group_name, group_members in
            itertools.groupby(sorted(fq_names), group_by_function)}


def fq_names_groups_to_stats(fq_names_groups: FqNamesGroups) -> FqNamesStats:
    return {group_name: len(group_members)
            for group_name, group_members in fq_names_groups.items()}


def fq_names_groups_to_dict(fq_names_groups: FqNamesGroups) -> FqNamesDict:
    return {group_name: fq_names_to_dict(list(map(lambda fq_name: ".".join(fq_name.split(".")[1:]), group_members)))
            for group_name, group_members in fq_names_groups.items()}


def fq_names_to_dict(fq_names: List[str]) -> FqNamesDict:
    cnt = len(fq_names)
    if cnt == 1 or cnt == len(set(fq_names)):
        return {ImportDirectivesColumn.COUNT: cnt}

    fq_names = list(filter(lambda fq_name: fq_name != "", fq_names))
    grouped_fq_names = group_fq_names_by(fq_names, lambda fq_name: fq_name.split(".")[0])

    grouped_fq_names_dict = fq_names_groups_to_dict(grouped_fq_names)
    grouped_fq_names_dict[ImportDirectivesColumn.COUNT] = cnt
    return grouped_fq_names_dict


def fq_names_to_csv(fq_names: List[str],
                    path_to_result_dir: str):
    fq_names_count = Counter(fq_names)
    save_stats_to_csv(path_to_result_dir, f"total.{Extensions.CSV}", fq_names_count)


def fq_names_to_bar_plot(fq_names: List[str]):
    fq_names_count = Counter(fq_names)
    save_stats_to_bar_plot(fq_names_count, "Fq names occurrence statistics")


def fq_names_by_prefix_to_csv(fq_names: List[str],
                              path_to_result_dir: str,
                              prefix_len: int):
    fq_names_by_prefix = group_fq_names_by(fq_names, lambda fq_name: get_package_by_len(fq_name, prefix_len))
    fq_names_by_prefix_stats = fq_names_groups_to_stats(fq_names_by_prefix)

    save_stats_to_csv(path_to_result_dir, f"total_by_prefix.{Extensions.CSV}", fq_names_by_prefix_stats)
    save_stats_to_bar_plot(fq_names_by_prefix_stats, "Fq names grouped by prefix occurrence statistics")


def fq_names_by_packages_to_csv(fq_names: List[str], packages: List[str],
                                max_package_len: int,
                                path_to_result_dir: str):
    """ Group fq names by package name and save occurrence statistics in csv format. """
    fq_names_by_package = group_fq_names_by(fq_names,
                                            lambda fq_name: get_prefix_by_package(fq_name, packages, max_package_len))
    fq_names_by_package_stats = fq_names_groups_to_stats(fq_names_by_package)

    save_stats_to_csv(path_to_result_dir, f"total_by_package.{Extensions.CSV}", fq_names_by_package_stats)
    save_stats_to_bar_plot(fq_names_by_package_stats, "Fq names grouped by package occurrence statistics")


def fq_names_to_json(fq_names_dict: FqNamesDict,
                     path_to_result_dir: str):
    """ Present fq names in json format. """
    with open(os.path.join(path_to_result_dir, f"fq_names_tree.{Extensions.JSON}"), 'w') as result_file:
        json.dump(fq_names_dict, result_file)


def fq_names_to_trees(root: FqNameNode, sub_roots: List[FqNameNode],
                      path_to_result_dir: str,
                      show: Callable[[FqNameNode, str], None]):
    """ Present fq names trees decomposition in given format. """
    show(root, path_to_result_dir)
    for sub_root in sub_roots:
        show(sub_root, path_to_result_dir)


def filter_fq_names(fq_names: List[str], filter_packages: List[str]) -> List[str]:
    return list(filter(lambda fq_name: "." in fq_name and
                                       not any(fq_name.startswith(package) for package in filter_packages), fq_names))


def get_fq_names(path_to_fq_names: str, path_to_ignored_packages: str):
    if path_to_ignored_packages is not None:
        ignored_packages = get_file_lines(path_to_ignored_packages)
    else:
        ignored_packages = []

    fq_names = list(map(lambda fq_names_info: fq_names_info.split(",")[1].rstrip(),
                        get_file_lines(path_to_fq_names)))[1:]
    return filter_fq_names(fq_names, ignored_packages)


def analyze(path_to_fq_names: str, path_to_result_dir: str, path_to_ignored_packages: str,
            max_package_len: int, max_subpackages: int, max_leaf_subpackages: int,
            min_occurrence: int, max_occurrence: int, max_u_occurrence: int,
            show_dot_trees: bool, show_txt_tree: bool, show_bar_plots: bool, show_csv: bool, show_package_csv: bool):
    create_directory(path_to_result_dir)

    fq_names = get_fq_names(path_to_fq_names, path_to_ignored_packages)
    print(len(fq_names))
    fq_names_dict = fq_names_to_dict(fq_names)
    root, sub_roots = build_fq_name_tree_decomposition(fq_names_dict, max_subpackages, max_leaf_subpackages,
                                                       min_occurrence, max_occurrence, max_u_occurrence)

    if show_csv:
        fq_names_to_csv(fq_names, path_to_result_dir)

    if show_package_csv:
        packages = [sub_root.full_name for sub_root in sub_roots]
        fq_names_by_packages_to_csv(fq_names, packages, max_package_len, path_to_result_dir)

    if show_bar_plots:
        fq_names_to_bar_plot(fq_names)

    if show_dot_trees:
        fq_names_to_trees(root, sub_roots, path_to_result_dir, save_to_png)

    if show_txt_tree:
        fq_names_to_trees(root, sub_roots, path_to_result_dir, save_to_txt)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('--input', type=str, help='path to csv file with fq_names', required=True)
    parser.add_argument('--output', type=str, help='path to output dir with result', required=True)
    parser.add_argument('--ignore', type=str, default=None, help='path to csv file with imports to ignore')

    parser.add_argument('--max-package-len', type=int, default=3,
                        help='max length of package name to group by')
    parser.add_argument('--max-subpackages', type=int, default=10000,
                        help='max number of subpackages to visualize, ignore extra with least count')
    parser.add_argument('--max-leaf-subpackages', type=int, default=0.8,
                        help='max percent of leaf subpackages to consider path as package')
    parser.add_argument('--min-occurrence', type=int, default=0,
                        help='min number of packages occurrence to analyze, ignore if less')
    parser.add_argument('--max-occurrence', type=int, default=1000,
                        help='max number of packages occurrence to visualize in main tree, '
                             'draw as a separate tree if less')
    parser.add_argument('--max-u-occurrence', type=int, default=50,
                        help='max number of unique packages occurrence to visualize in main tree, '
                             'draw as a separate tree if less')

    parser.add_argument('--show-dot-trees', type=bool, default=True,
                        help='visualize fn names statistics as package tree with detailed subtrees as dot png pictures')
    parser.add_argument('--show-txt-trees', type=bool, default=True,
                        help='visualize fn names statistics as text fq names tree with detailed subtrees')
    parser.add_argument('--show-bar-plots', type=bool, default=True,
                        help='visualize fn names statistics as bar plot')
    parser.add_argument('--show-csv', type=bool, default=True,
                        help='visualize fn names statistics as csv with fq names and occurrence')
    parser.add_argument('--show-package-csv', type=bool, default=True,
                        help='visualize fn names statistics as csv with fq package names and occurrence')

    args = parser.parse_args(sys.argv[1:])

    analyze(args.input, args.output, args.ignore,
            args.max_package_len, args.max_subpackages, args.max_leaf_subpackages,
            args.min_occurrence, args.max_occurrence, args.max_u_occurrence,
            args.show_dot_trees, args.show_txt_trees, args.show_bar_plots, args.show_csv, args.show_package_csv)
