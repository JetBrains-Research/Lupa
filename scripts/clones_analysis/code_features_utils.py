import os
from pygments import highlight
from pygments.lexers import KotlinLexer
from pygments.formatters import HtmlFormatter


def add_code_features(df, dataset_path):
    df['method_text'] = df.apply(lambda row: get_method_text(row, dataset_path), axis=1)
    df['highlighted_code'] = df['method_text'].apply(lambda code: highlight_code(code))


def highlight_code(code):
    formatter = HtmlFormatter(cssclass='pygments')
    html_code = highlight(code, KotlinLexer(), formatter)
    css = formatter.get_style_defs('.pygments')
    template = """<style>
    {}
    max-width: 150px;
    </style>{}
    """
    return template.format(css, html_code).strip()


def clean_indents(text):
    lines = text.split('\n')
    line_length = len(lines[0])
    lines[0] = lines[0].lstrip()
    to_clean = line_length - len(lines[0])
    for i in range(1, len(lines)):
        lines[i] = lines[i][to_clean:]

    return "\n".join(lines)


def get_method_text(row, dataset_path):
    with open(os.path.join(dataset_path, row.file)) as fin:
        method_lines = fin.readlines()[row.start_line:row.end_line + 1]
    return clean_indents(''.join(method_lines))


def is_method_empty(text):
    first_opened = text.find("{")
    first_closed = text.find("}")
    if first_opened == -1 or first_closed == -1:
        return False
    return text[first_opened + 1:first_closed].strip() == ""
