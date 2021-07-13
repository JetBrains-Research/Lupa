import pandas as pd
import sys
import plotly.express as px


def show_bar_plot(path_to_csv: str, x_label, y_label):
    df = pd.read_csv(path_to_csv)
    df = df.sort_values(by=[y_label], ascending=False)
    stats_df = pd.DataFrame({x_label: df[x_label].values,
                             y_label: df[y_label].values})
    fig = px.bar(stats_df, x=x_label, y=y_label)
    fig.show()


if __name__ == '__main__':
    path_to_stats = sys.argv[1]
    x_label = sys.argv[2]
    y_label = sys.argv[3]
    show_bar_plot(path_to_stats, x_label, y_label)
