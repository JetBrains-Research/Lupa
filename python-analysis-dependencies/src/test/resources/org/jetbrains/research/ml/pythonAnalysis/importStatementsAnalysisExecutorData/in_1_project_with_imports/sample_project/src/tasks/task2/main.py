import itertools

import plotly.express as px

from src.tasks.task1.main import quick_math


def show_plot():
    qm = quick_math()
    fig = px.line(x=range(0, 10), y=list(itertools.repeat(qm, 10)))
    fig.show()


if __name__ == '__main__':
    show_plot()
