import os
import subprocess


class GitRepository:
    def __init__(self, repository, project_dir):
        self.username, self.project_name = repository.split('/')
        self.project_dir = project_dir

    def clone(self):
        p = subprocess.Popen(
            ["git", "clone", f"https://github.com/{self.username}/{self.project_name}.git",
             os.path.basename(self.project_dir), "--depth", "1"], cwd=os.path.dirname(self.project_dir))
        return_code = p.wait()
        return return_code == 0

    def pull_changes_without_history(self) -> bool:
        """
        Pull changes from repository without history.
        :return: Whether or not repository was updated.
        """
        head_before_update = self.get_head_commit()
        subprocess.Popen(["git", "fetch", "--depth", "1"], cwd=self.project_dir, stdout=subprocess.PIPE)
        subprocess.Popen(["git", "reset", "--hard", "origin"], cwd=self.project_dir, stdout=subprocess.PIPE)
        head_after_update = self.get_head_commit()

        return head_before_update != head_after_update

    def get_head_commit(self) -> str:
        p = subprocess.Popen(["git", "rev-parse", "HEAD"], cwd=self.project_dir, stdout=subprocess.PIPE)
        response = p.communicate()[0].decode("utf-8")
        return response
