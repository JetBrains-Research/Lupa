from psycopg2 import sql


class RepositoriesTable:
    def __init__(self, conn):
        self._conn = conn

    def create(self):
        self._conn.execute(
            sql.SQL("""
            create table if not exists kotlin_repositories_updates (
            repository_id   serial      primary key,
            username        char(50)    not null,
            repo_name       char(50)    not null,
            last_pull_date  date        not null,
            unique (username, repo_name))"""))

    def update_date(self, username, repo_name, last_pull_date):
        self._conn.execute(
            sql.SQL("""
            update kotlin_repositories_updates
            set last_pull_date={0}
            where username = {1} and repo_name = {2}
            """).format(sql.Literal(last_pull_date), sql.Literal(username), sql.Literal(repo_name)))

    def insert(self, username, repo_name, last_pull_date):
        self._conn.execute(
            sql.SQL("""
            insert into kotlin_repositories_updates(username, repo_name, last_pull_date)
                values ({0}, {1}, {2})
            """).format(sql.Literal(username), sql.Literal(repo_name), sql.Literal(last_pull_date)))

    def exists_repository(self, username, repo_name) -> bool:
        return bool(self._conn.execute(
            sql.SQL("""
            select last_pull_date from kotlin_repositories_updates
            where username = {0} and repo_name = {1}
            """).format(sql.Literal(username), sql.Literal(repo_name)), has_res=True))
