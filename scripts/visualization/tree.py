from anytree import Node, RenderTree
from anytree import NodeMixin
from anytree.exporter import UniqueDotExporter
from typing import Dict


class CNode(NodeMixin):
    def __init__(self, name, parent: int = None, count: int = None, unique_count: int = None):
        super(CNode, self).__init__()
        self.name = name
        self.parent = parent
        self.count = count
        self.unique_count = unique_count

    def __repr__(self):
        return str(f"{self.name} [{self.count}]")


def build_tree_from_dict(fq_names_dict: Dict, parent: Node):
    if len(fq_names_dict) == 0:
        return 0
    for key, value in fq_names_dict.items():
        if key != 'cnt':
            node = CNode(key, parent, value['cnt'])
            node.unique_count = build_tree_from_dict(value, node)
    return 1 + sum(child.unique_count for child in parent.children)


def show_tree_recursive(parent: CNode, max_unique_count=30):
    for node in parent.children:
        if node.unique_count > max_unique_count:
            show_tree_recursive(node, max_unique_count)
        else:
            parent.children = []
            UniqueDotExporter(node,
                              edgeattrfunc=lambda parent, child: "style=bold,label=%d" % (child.count or 0)).to_picture(
                f"{node.name}.png")


def show_tree(fq_names_map: Dict):
    root = CNode(".")
    build_tree_from_dict(fq_names_map, root)
    print(RenderTree(root))

    draw_level = 0
    for node in root.children:
        UniqueDotExporter(node,
                          edgeattrfunc=lambda parent, child: "style=bold,label=%d" % (child.count or 0)).to_picture(
            f"{node.name}.png")
