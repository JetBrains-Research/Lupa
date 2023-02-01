from dataclasses import dataclass
from pathlib import Path
from typing import List, Optional


SCRIPT_PATH = Path(__file__).parents[4] / 'scripts' / 'plugin_runner' / 'batch_processing.py'


@dataclass
class CommandBuilder:
    input: Path
    output: Path
    batching_config: Path
    data: str
    use_db: bool = False
    start_from: Optional[int] = None
    task_name: Optional[str] = None
    kwargs: Optional[str] = None

    def build(self) -> List[str]:
        command = [
            'python3',
            str(SCRIPT_PATH),
            str(self.input),
            str(self.output),
            str(self.batching_config),
            self.data,
        ]

        if self.use_db:
            command.append('--use-db')

        if self.start_from is not None:
            command.extend(['--start-from', self.start_from])

        if self.task_name is not None:
            command.extend(['--task-name', self.task_name])

        if self.kwargs is not None:
            command.extend(['--kwargs', self.kwargs])

        return command
