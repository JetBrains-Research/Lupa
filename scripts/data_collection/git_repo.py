import os
import subprocess


class GitRepository:
    def __init__(self, repository: str, project_dir: str, store_history: bool = False):
        self.username, self.project_name = repository.split('/')
        self.project_dir = project_dir
        self.store_history = store_history

    def clone(self):
        command = ["git", "clone", f"https://github.com/{self.username}/{self.project_name}.git",
                   os.path.basename(self.project_dir)]
        if not self.store_history:
            command += ["--depth", "1"]

        p = subprocess.Popen(command, cwd=os.path.dirname(self.project_dir))
        return_code = p.wait()
        return return_code == 0

    def pull_changes(self) -> bool:
        """
        Pull changes from repository with or without history.

        :return: Whether or not repository was updated.
        """
        head_before_update = self.get_head_commit()
        if not self.store_history:
            p_fetch = subprocess.Popen(["git", "fetch", "--depth", "1"], cwd=self.project_dir, stdout=subprocess.PIPE)
            p_fetch.wait()
            p_reset = subprocess.Popen(["git", "reset", "--hard", "origin"], cwd=self.project_dir,
                                       stdout=subprocess.PIPE)
            p_reset.wait()
            p_clean = subprocess.Popen(["git", "clean", "-dfx"], cwd=self.project_dir, stdout=subprocess.PIPE)
            p_clean.wait()
        else:
            p_pull = subprocess.Popen(["git", "pull"], cwd=self.project_dir, stdout=subprocess.PIPE)
            p_pull.wait()
        head_after_update = self.get_head_commit()

        return head_before_update != head_after_update

    def get_head_commit(self) -> str:
        p = subprocess.Popen(["git", "rev-parse", "HEAD"], cwd=self.project_dir, stdout=subprocess.PIPE)
        response = p.communicate()[0].decode("utf-8")
        return response
