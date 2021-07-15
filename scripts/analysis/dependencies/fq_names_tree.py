import os
from typing import Dict, List, Union

from anytree import NodeMixin
from anytree import RenderTree
from anytree.exporter import UniqueDotExporter

from analysis.dependencies.column_names_utils import ImportDirectivesColumn
from utils import Extensions

"""
Script fow working with import dependencies fq name tree. 
It contain methods for deleting nodes with small occurrence, merging single child nodes to one, splitting thee into 
subtrees for showing it separately, if subtree is not large or meaning.
"""

FqNamesDict = Dict[str, Union[Dict, int, str]]


class FqNameNode(NodeMixin):

    def __init__(self, name, parent=None, count: int = 0, unique_count: int = None):
        super(FqNameNode, self).__init__()
        self.name = name
        self.parent = parent
        self.count = count
        self.unique_count = unique_count

    def __repr__(self):
        return str(f"{self.name} [{self.count}]")

    def str_path(self, sep="_", with_root=True) -> str:
        cut_index = 0 if with_root else 1
        return sep.join([node_path.name for node_path in self.path[cut_index:]])

    def is_huge(self, max_leaf_subpackages) -> bool:
        children_count = len(self.children)
        leaf_children_count = len(list(filter(lambda node: node.is_leaf, self.children)))
        return children_count == 0 or leaf_children_count / children_count > max_leaf_subpackages


def save_to_png(node: FqNameNode, path_to_result_dir: str):
    if len(node.children) == 0:
        return
    path_to_node_tree = os.path.join(path_to_result_dir, f"{node.str_path()}.{Extensions.PNG}")
    UniqueDotExporter(node,
                      edgeattrfunc=lambda parent, child: "style=bold,label=%d" % (child.count or 0)).to_picture(
        path_to_node_tree)


def save_to_txt(node: FqNameNode, path_to_result_dir: str, filename=f"fq_names_tree.{Extensions.TXT}"):
    with open(os.path.join(path_to_result_dir, filename), "w") as tree_text_file:
        tree_text_file.write(str(RenderTree(node)))


def delete_extra_child_nodes(parent: FqNameNode, max_children: int):
    """ Recursively leave not much then `max_children` children in the ascending sorted by count order.
    :param parent: node to remove extra children
    :param max_children: max number of children that is allowed to leave
    """
    parent.children = list(sorted(parent.children, key=lambda n: -n.count))[:max_children]
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


def split_to_subtrees(parent: FqNameNode, max_leaf_subpackages: int, max_occurrence: int, max_u_occurrence: int) \
        -> List[FqNameNode]:
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
    if parent.unique_count < max_u_occurrence or (not parent.is_root and parent.count < max_occurrence) or \
            parent.is_huge(max_leaf_subpackages):
        parent_root = FqNameNode(parent.name, None, parent.count, parent.unique_count)
        parent_root.children = parent.children
        parent.children = []
        return [parent_root]
    packages = []
    for node in parent.children:
        packages += split_to_subtrees(node, max_leaf_subpackages, max_occurrence, max_u_occurrence)
    return packages


def build_fq_name_tree(fq_names_dict: FqNamesDict) -> FqNameNode:
    root = FqNameNode("root")
    recursive_build_fq_name_tree(root, fq_names_dict)
    return root


def recursive_build_fq_name_tree(parent: FqNameNode, fq_names_dict: FqNamesDict):
    if len(fq_names_dict) == 0:
        return 0
    for key, value in fq_names_dict.items():
        if key != ImportDirectivesColumn.COUNT:
            node = FqNameNode(key, parent, value[ImportDirectivesColumn.COUNT])
            recursive_build_fq_name_tree(node, value)
