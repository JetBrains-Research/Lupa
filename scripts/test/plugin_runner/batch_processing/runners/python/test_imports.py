from pathlib import Path
from tempfile import TemporaryDirectory

import pandas as pd

from plugin_runner.analyzers import AVAILABLE_ANALYZERS, Analyzer
from test.plugin_runner.batch_processing import DUMMY_CONFIG
from test.plugin_runner.batch_processing.command_builder import CommandBuilder
from test.plugin_runner.batch_processing.runners.python import IMPORTS_DATASET, IMPORTS_DATASET_OUTPUT
from utils.df_utils import assert_df_equals
from utils.run_process_utils import run_in_subprocess


def test_python_imports() -> None:
    analyzer = Analyzer.get_analyzer_by_name(AVAILABLE_ANALYZERS, 'python-imports')

    with TemporaryDirectory() as tmpdir:
        command_builder = CommandBuilder(
            IMPORTS_DATASET,
            Path(tmpdir),
            DUMMY_CONFIG,
            analyzer.name,
        )

        run_in_subprocess(command_builder.build())

        actual = pd.read_csv(Path(tmpdir) / analyzer.output_file)
        expected = pd.read_csv(IMPORTS_DATASET_OUTPUT)

        assert_df_equals(actual, expected, sort_by_columns=actual.columns.tolist())
