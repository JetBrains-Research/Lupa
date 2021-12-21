from datetime import datetime

from psycopg2 import sql

from data_collection.db_connect import DatabaseConn


class RepositoriesTable:
    def __init__(self, conn: DatabaseConn):
        self._conn = conn

    def create(self):
        if self._conn is not None:
            self._conn.execute(
                sql.SQL("""
                create table if not exists kotlin_repositories_updates (
                repository_id       serial      primary key,
                username            varchar(64)   not null,
                repo_name           varchar(255)   not null,
                last_pull_date      date        not null,
                last_analysis_date  date,
                unique (username, repo_name))"""))

    def update_date(self, username: str, repo_name: str, last_pull_date: datetime.date):
        if self._conn is not None:
            self._conn.execute(
                sql.SQL("""
                update kotlin_repositories_updates
                set last_pull_date={0}
                where username = {1} and repo_name = {2}
                """).format(sql.Literal(last_pull_date), sql.Literal(username), sql.Literal(repo_name)))

    def insert(self, username: str, repo_name: str, last_pull_date: datetime.date):
        if self._conn is not None:
            self._conn.execute(
                sql.SQL("""
                insert into kotlin_repositories_updates(username, repo_name, last_pull_date)
                    values ({0}, {1}, {2})
                """).format(sql.Literal(username), sql.Literal(repo_name), sql.Literal(last_pull_date)))

    def exists_repository(self, username: str, repo_name: str) -> bool:
        if self._conn is None:
            return False

        return bool(self._conn.execute(
            sql.SQL("""
            select last_pull_date from kotlin_repositories_updates
            where username = {0} and repo_name = {1}
            """).format(sql.Literal(username), sql.Literal(repo_name)), has_res=True))
