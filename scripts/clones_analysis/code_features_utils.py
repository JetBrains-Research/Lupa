import os
import pandas as pd
from pygments import highlight
from pygments.lexers import KotlinLexer
from pygments.formatters import HtmlFormatter

from column_names_utils import Methods_columns
from utils import get_file_lines


def add_code_features(df: pd.DataFrame, dataset_path: str):
    df[Methods_columns.method_text.value] = df.apply(lambda row: get_method_text(row, dataset_path), axis=1)
    df[Methods_columns.highlighted_code.value] = df[Methods_columns.method_text.value].apply(
        lambda code: highlight_code(code))


def highlight_code(code: str) -> str:
    formatter = HtmlFormatter(cssclass='pygments')
    html_code = highlight(code, KotlinLexer(), formatter)
    css = formatter.get_style_defs('.pygments')
    template = """<style>
    {}
    max-width: 150px;
    </style>{}
    """
    return template.format(css, html_code).strip()


def clean_indents(text: str) -> str:
    lines = text.split('\n')
    line_length = len(lines[0])
    lines[0] = lines[0].lstrip()
    to_clean = line_length - len(lines[0])
    for i in range(1, len(lines)):
        lines[i] = lines[i][to_clean:]

    return "\n".join(lines)


def get_method_text(row: pd.Series, dataset_path: str) -> str:
    method_lines = get_file_lines(os.path.join(dataset_path, row.file))[row.start_line:row.end_line + 1]
    return clean_indents(''.join(method_lines))


def is_method_empty(text: str) -> bool:
    first_opened = text.find("{")
    first_closed = text.find("}")
    if first_opened == -1 or first_closed == -1:
        return False
    return text[first_opened + 1:first_closed].strip() == ""
