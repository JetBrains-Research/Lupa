import pandas as pd
import plotly.express as px

"""
Script for various diagrams visualisation.
"""


def show_bar_plot(path_to_csv: str, x_label: str, y_label: str):
    df = pd.read_csv(path_to_csv)
    df = df.sort_values(by=[y_label], ascending=False)
    stats_df = pd.DataFrame({x_label: df[x_label].values,
                             y_label: df[y_label].values})
    fig = px.bar(stats_df, x=x_label, y=y_label)
    fig.show()
