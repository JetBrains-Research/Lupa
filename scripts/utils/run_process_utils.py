import logging
import subprocess
from pathlib import Path
from typing import List, Optional, Tuple, Union


PROJECT_ROOT = Path(__file__).parents[2]


def run_in_subprocess(
    command: List[str],
    cwd: Optional[Union[str, Path]] = PROJECT_ROOT,
    stdout_file=subprocess.PIPE,
    stderr_file=subprocess.PIPE,
) -> Tuple[int, str]:
    process = subprocess.run(command, cwd=cwd, stdout=stdout_file, stderr=stderr_file)

    stdout = process.stdout.decode() if process.stdout else None
    stderr = process.stderr.decode() if process.stderr else None

    if stdout:
        logging.debug("%s's stdout:\n%s" % (command[0], stdout))
    if stderr:
        logging.debug("%s's stderr:\n%s" % (command[0], stderr))

    return process.returncode, stdout
