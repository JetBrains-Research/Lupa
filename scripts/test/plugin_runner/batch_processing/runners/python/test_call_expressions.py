from distutils.version import StrictVersion
from pathlib import Path
from tempfile import TemporaryDirectory

import pandas as pd

from plugin_runner.analyzers import AVAILABLE_ANALYZERS, Analyzer
from plugin_runner.python_venv.common import create_requirements_file, create_venv, install_requirements
from test.plugin_runner.batch_processing import DUMMY_CONFIG
from test.plugin_runner.batch_processing.command_builder import CommandBuilder
from test.plugin_runner.batch_processing.runners.python import CALL_EXPRESSIONS_DATASET, CALL_EXPRESSIONS_OUTPUT
from utils.df_utils import assert_df_equals
from utils.run_process_utils import run_in_subprocess


def test_python_call_expressions() -> None:
    analyzer = Analyzer.get_analyzer_by_name(AVAILABLE_ANALYZERS, 'python-call-expressions')

    with TemporaryDirectory() as tmpdir:
        venv_path = Path(tmpdir) / '.venv'
        create_venv(venv_path)

        requirements_file = create_requirements_file({'streamlit': StrictVersion('1.13.0')}, Path(tmpdir))
        install_requirements(venv_path, requirements_file, no_package_dependencies=True, no_cache=True, for_each=False)

        command_builder = CommandBuilder(
            CALL_EXPRESSIONS_DATASET,
            Path(tmpdir),
            DUMMY_CONFIG,
            analyzer.name,
            task_name='python-cli',
            kwargs=f'venv={venv_path}',
        )

        run_in_subprocess(command_builder.build())

        actual = pd.read_csv(Path(tmpdir) / analyzer.output_file)
        expected = pd.read_csv(CALL_EXPRESSIONS_OUTPUT)

        assert_df_equals(actual, expected, sort_by_columns=actual.columns.tolist())
