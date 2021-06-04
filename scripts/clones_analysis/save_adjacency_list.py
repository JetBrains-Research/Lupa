import argparse
import logging
import os


def main():
    args = parse_args()
    logging.getLogger().setLevel(logging.INFO)

    clone_adjacency_list = {}
    read_pairs(clone_adjacency_list, args.path80, 80)
    read_pairs(clone_adjacency_list, args.path100, 100)
    logging.info("Constructed dict with adjacency list")

    save_clones(clone_adjacency_list, args.output)
    logging.info("Saved clones")


def read_pairs(clone_adjacency_list, path_to_pairs, closeness):
    cnt = 0
    with open(path_to_pairs, 'r') as fin:
        for line in fin:
            add_clone(clone_adjacency_list, line.strip(), closeness)
            cnt += 1
            if cnt % 1_000_000 == 0:
                logging.info(f"Finished processing {cnt} clone (closeness {closeness})")


def add_clone(clone_adjacency_list, line, closeness):
    project1_id, method1_id, project2_id, method2_id = line.split(",")
    add_edge(clone_adjacency_list, project1_id, method1_id, project2_id, method2_id, closeness)
    add_edge(clone_adjacency_list, project2_id, method2_id, project1_id, method1_id, closeness)


def add_edge(clone_adjacency_list, project1, method1, project2, method2, closeness):
    if method1 not in clone_adjacency_list:
        clone_adjacency_list[method1] = f"{project1}:{method2},{project2},{closeness}"
    else:
        clone_adjacency_list[method1] += f";{method2},{project2},{closeness}"


def save_clones(clone_adjacency_list, path):
    with open(os.path.join(path, "clones_adjacency_test.txt"), 'w+') as fout:
        for method_id, data in clone_adjacency_list.items():
            line = ",".join([str(method_id), data]) + "\n"
            fout.write(line)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("path80", help="Input file with 80 closeness clones")
    parser.add_argument("path100", help="Input file with 100 closeness clones")
    parser.add_argument("output", help="Output directory")
    return parser.parse_args()


if __name__ == "__main__":
    main()
