import os

from utils.run_process_utils import run_in_subprocess


class GitRepository:
    def __init__(self, repository: str, project_dir: str, store_history: bool = False):
        self.username, self.project_name = repository.split('/')
        self.project_dir = project_dir
        self.store_history = store_history

    def clone(self):
        command = ['git', 'clone', f'https://github.com/{self.username}/{self.project_name}.git',
                   os.path.basename(self.project_dir)]
        if not self.store_history:
            command += ['--depth', '1']

        return_code, _ = run_in_subprocess(command)
        return return_code == 0

    def pull_changes(self) -> bool:
        """
        Pull changes from repository with or without history.

        :return: Whether or not repository was updated.
        """
        head_before_update = self.get_head_commit()
        if not self.store_history:
            self.pull_changes_without_history()
        else:
            run_in_subprocess(['git', 'pull'], cwd=self.project_dir)
        head_after_update = self.get_head_commit()

        return head_before_update != head_after_update

    def pull_changes_without_history(self):
        run_in_subprocess(['git', 'fetch', '--depth', '1'], cwd=self.project_dir)
        run_in_subprocess(['git', 'reset', '--hard', 'origin'], cwd=self.project_dir)
        run_in_subprocess(['git', 'clean', '-dfx'], cwd=self.project_dir)

    def get_head_commit(self) -> str:
        return_code, response = run_in_subprocess(['git', 'rev-parse', 'HEAD'], cwd=self.project_dir)
        return response
