from typing import Dict, Union

import pandas as pd
import plotly.express as px

"""
Script for various diagrams visualisation.
"""


def show_bar_plot(stats: Dict[str, Union[int, float]], x_label: str, y_label: str, title: str) -> None:
    """ Method for statistics visualization as a bar chart
        :param stats: dict which for string value name stores it's statistics int or float value
        :param x_label: label for axis with value names
        :param y_label: label for axis with values
        :param title: plot title
    """
    df = pd.DataFrame.from_dict({x_label: stats.keys(), y_label: stats.values()})
    df = df.sort_values(by=[y_label], ascending=False)
    stats_df = pd.DataFrame({x_label: df[x_label].values,
                             y_label: df[y_label].values})
    fig = px.bar(stats_df, x=x_label, y=y_label, title=title)
    fig.show()
