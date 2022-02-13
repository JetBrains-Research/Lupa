import enum
from datetime import datetime
from typing import List, Optional, Tuple

from data_collection.db_connect import DatabaseConn

from psycopg2 import sql
from psycopg2.sql import Identifier, Literal


class RepositoriesTableNames(str, enum.Enum):
    TABLE = "kotlin_repositories_updates"
    REPO_ID = "repository_id"
    USERNAME = "username"
    REPO_NAME = "repo_name"
    LAST_PULL_DATE = "last_pull_date"
    LAST_ANALYSIS_DATE = "last_analysis_date"


class RepositoriesTable:
    def __init__(self, conn: DatabaseConn):
        self._conn = conn

    def create(self):
        if self._conn is not None:
            self._conn.execute(
                sql.SQL(f"""
                create table if not exists {RepositoriesTableNames.TABLE.value} (
                {RepositoriesTableNames.REPO_ID.value}            serial          primary key,
                {RepositoriesTableNames.USERNAME.value}           varchar(64)     not null,
                {RepositoriesTableNames.REPO_NAME.value}          varchar(255)    not null,
                {RepositoriesTableNames.LAST_PULL_DATE.value}     date            not null,
                {RepositoriesTableNames.LAST_ANALYSIS_DATE.value} date,
                unique ({RepositoriesTableNames.USERNAME.value}, {RepositoriesTableNames.REPO_NAME.value}))"""))

    def update_pull_date(self, username: str, repo_name: str, last_pull_date: datetime.date):
        if self._conn is not None:
            query = sql.SQL("""
            update {table_name}
            set {pull_date_col} = {pull_date}
            where {username_col} = {username}
            and {repo_name_col} = {repo_name}""")

            self._conn.execute(self.format_sql(query, username, repo_name, last_pull_date))

    def insert(self, username: str, repo_name: str, last_pull_date: datetime.date):
        if self._conn is not None:
            query = sql.SQL("""
            insert into {table_name}({username_col}, {repo_name_col}, {pull_date_col})
            values ({username}, {repo_name}, {pull_date})""")

            self._conn.execute(self.format_sql(query, username, repo_name, last_pull_date))

    def exists_repository(self, username: str, repo_name: str) -> bool:
        if self._conn is None:
            return False

        query = sql.SQL("""
        select {pull_date_col} from {table_name}
        where {username_col} = {username} and {repo_name_col} = {repo_name}
        """)

        return bool(self._conn.execute(
            self.format_sql(query, username, repo_name), has_res=True))

    def select_repositories_to_analyse(self) -> Optional[List[Tuple[str]]]:
        if self._conn is None:
            return None

        query = sql.SQL("""
        select {username_col}, {repo_name_col} from {table_name}
        where {pull_date_col} > {analysis_date_col}
        or {analysis_date_col} is null""")

        return self._conn.execute(self.format_sql(query), has_res=True)

    @staticmethod
    def format_sql(query: sql.SQL, username: str = None, repo_name: str = None, last_pull_date=None) -> sql.Composed:
        return query.format(table_name=Identifier(RepositoriesTableNames.TABLE.value),
                            username_col=Identifier(RepositoriesTableNames.USERNAME.value),
                            repo_name_col=Identifier(RepositoriesTableNames.REPO_NAME.value),
                            pull_date_col=Identifier(RepositoriesTableNames.LAST_PULL_DATE.value),
                            analysis_date_col=Identifier(RepositoriesTableNames.LAST_ANALYSIS_DATE.value),
                            username=Literal(username),
                            repo_name=Literal(repo_name),
                            pull_date=sql.Literal(last_pull_date))
