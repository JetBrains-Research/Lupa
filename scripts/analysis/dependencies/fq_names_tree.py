import os
from typing import List, Tuple

from anytree import NodeMixin
from anytree import RenderTree
from anytree.exporter import UniqueDotExporter

from column_names_utils import ImportDirectivesColumn
from fq_names_types import FqNamesDict
from utils import Extensions, write_to_file, create_directory

"""
Script fow working with import dependencies fq name tree.
It contain methods for deleting nodes with small occurrence, merging single child nodes to one, splitting thee into
subtrees for showing it separately, if subtree is not large or meaning.
"""


class FqNameNode(NodeMixin):

    def __init__(self, name, parent=None, count: int = 0, unique_count: int = None):
        super(FqNameNode, self).__init__()
        self.name = name
        self.parent = parent
        self.count = count
        self.unique_count = unique_count
        self.full_name = name if parent is None or parent.is_root else f"{parent.full_name}.{name}"

    def __repr__(self):
        return str(f"{self.name} [{self.count}]")


def save_to_png(node: FqNameNode, path_to_result_dir: str):
    if len(node.children) == 0:
        return
    tree_dot_dir = os.path.join(path_to_result_dir, "tree_dot")
    create_directory(tree_dot_dir)
    filename = os.path.join(tree_dot_dir, f"{node.full_name}.{Extensions.PNG}")
    UniqueDotExporter(
        node,
        edgeattrfunc=lambda parent, child: "style=bold,label=%d" % (child.count or 0),
    ).to_picture(filename)


def save_to_txt(node: FqNameNode, path_to_result_dir: str):
    tree_txt_dir = os.path.join(path_to_result_dir, "tree_txt")
    create_directory(tree_txt_dir)
    filename = os.path.join(tree_txt_dir, f"{node.full_name}.{Extensions.TXT}")
    write_to_file(filename, str(RenderTree(node)))


def delete_extra_child_nodes(parent: FqNameNode, max_children: int):
    """ Recursively leave not much then `max_children` children in the ascending sorted by count order.
    :param parent: node to remove extra children
    :param max_children: max number of children that is allowed to leave
    """
    parent.children = sorted(parent.children, key=lambda n: -n.count)[:max_children]
    for node in parent.children:
        delete_extra_child_nodes(node, max_children)


def delete_rare_nodes(parent: FqNameNode, min_count: int):
    """ Recursively leave only children with count greater then `min_count`.
    :param parent: node to remove extra rare children
    :param min_count: min value of children count that is allowed to leave
    """
    parent.children = list(filter(lambda n: n.count > min_count, parent.children))
    for node in parent.children:
        delete_rare_nodes(node, min_count)


def merge_single_child_nodes(parent: FqNameNode):
    """ Recursively merge and concatenate names of parent and child nodes if this child is the only one for `parent`.
    :param parent: node to merge
    """
    if len(parent.children) == 1:
        parent.name = ".".join([parent.name, parent.children[0].name])
        parent.children = parent.children[0].children
    for node in parent.children:
        merge_single_child_nodes(node)


def calc_unique_count(parent: FqNameNode) -> int:
    parent.unique_count = 1 + sum(calc_unique_count(child) for child in parent.children)
    return parent.unique_count


def is_leaf(node: FqNameNode):
    return node.is_leaf or node.name[0].isupper()


def is_huge(node: FqNameNode, max_leaf_subpackages: float, max_occurrence: int, max_u_occurrence: int) -> bool:
    """ Check is node huge to be considerate as package and show separately. """
    if node.unique_count < max_u_occurrence or (not node.is_root and node.count < max_occurrence):
        return True
    children_count = len(node.children)
    leaf_children_count = len(list(filter(lambda child: is_leaf(child), node.children)))
    return children_count == 0 or leaf_children_count / children_count > max_leaf_subpackages


def split_to_subtrees(
    parent: FqNameNode,
    max_leaf_subpackages: float,
    max_occurrence: int,
    max_u_occurrence: int,
) -> List[FqNameNode]:
    """ Recursively split tree into subtrees to make each subtree smaller and representative.
    :param parent: node to remove extra rare children
    :param max_leaf_subpackages: if for `parent` node percent of leaf children is more then `max_leaf_subpackages`
    we consider `parent` as root of new subtree spit it from given tree
    :param max_occurrence: if parent count value is more then `max_occurrence` we do not consider `parent` as root
    of new subtree
    :param max_u_occurrence: if parent subtree size is more then `max_u_occurrence` we do not consider `parent` as root
    of new subtree
    :return roots of subtrees
    """
    if parent.unique_count is None:
        parent.unique_count = calc_unique_count(parent)
    if is_leaf(parent):
        return []
    if is_huge(parent, max_leaf_subpackages, max_occurrence, max_u_occurrence):
        parent_root = FqNameNode(parent.full_name, None, parent.count, parent.unique_count)
        parent_root.children = parent.children
        parent.children = []
        return [parent_root]
    sub_roots = []
    for node in parent.children:
        sub_roots += split_to_subtrees(node, max_leaf_subpackages, max_occurrence, max_u_occurrence)
    return sub_roots


def build_fq_name_tree(fq_names_dict: FqNamesDict) -> FqNameNode:
    root = FqNameNode("root")
    recursive_build_fq_name_tree(root, fq_names_dict)
    return root


def build_fq_name_tree_decomposition(
    fq_names_dict: FqNamesDict,
    max_subpackages: int,
    max_leaf_subpackages: int,
    min_occurrence: int,
    max_occurrence: int,
    max_u_occurrence: int,
) -> Tuple[FqNameNode, List[FqNameNode]]:
    root = build_fq_name_tree(fq_names_dict)

    delete_rare_nodes(root, min_occurrence)
    delete_extra_child_nodes(root, max_subpackages)
    merge_single_child_nodes(root)

    sub_roots = split_to_subtrees(root, max_leaf_subpackages, max_occurrence, max_u_occurrence)

    return root, sub_roots


def recursive_build_fq_name_tree(parent: FqNameNode, fq_names_dict: FqNamesDict):
    if len(fq_names_dict) == 0:
        return 0
    for key, value in fq_names_dict.items():
        if key != ImportDirectivesColumn.COUNT:
            node = FqNameNode(key, parent, value[ImportDirectivesColumn.COUNT])
            recursive_build_fq_name_tree(node, value)
