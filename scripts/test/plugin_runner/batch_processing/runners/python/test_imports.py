from pathlib import Path

from plugin_runner.analyzers import AVAILABLE_ANALYZERS, Analyzer
from test.plugin_runner.batch_processing import DUMMY_CONFIG
from test.plugin_runner.batch_processing.command_builder import CommandBuilder, SCRIPT_PATH
from test.plugin_runner.batch_processing.runners.python import IMPORTS_DATASET, IMPORTS_DATASET_OUTPUT
from tempfile import TemporaryDirectory

from utils.file_utils import get_file_content
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

        assert get_file_content(Path(tmpdir) / analyzer.output_file) == get_file_content(IMPORTS_DATASET_OUTPUT)
