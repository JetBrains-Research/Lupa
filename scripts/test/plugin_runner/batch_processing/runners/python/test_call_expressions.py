from pathlib import Path
from tempfile import TemporaryDirectory

from plugin_runner.analyzers import AVAILABLE_ANALYZERS, Analyzer
from plugin_runner.python_venv.common import create_venv
from test.plugin_runner.batch_processing import DUMMY_CONFIG
from test.plugin_runner.batch_processing.command_builder import CommandBuilder, SCRIPT_PATH
from test.plugin_runner.batch_processing.runners.python import CALL_EXPRESSIONS_DATASET, CALL_EXPRESSIONS_OUTPUT
from utils.file_utils import get_file_content
from utils.run_process_utils import run_in_subprocess


def test_python_call_expressions() -> None:
    analyzer = Analyzer.get_analyzer_by_name(AVAILABLE_ANALYZERS, 'python-call-expressions')

    with TemporaryDirectory() as tmpdir:
        venv_path = Path(tmpdir) / '.venv'
        create_venv(venv_path)

        command_builder = CommandBuilder(
            CALL_EXPRESSIONS_DATASET,
            Path(tmpdir),
            DUMMY_CONFIG,
            analyzer.name,
            task_name='python-cli',
            kwargs=f'venv={venv_path}',
        )

        run_in_subprocess(command_builder.build())

        assert get_file_content(Path(tmpdir) / analyzer.output_file) == get_file_content(CALL_EXPRESSIONS_OUTPUT)
