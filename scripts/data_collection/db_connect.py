from configparser import ConfigParser
from pathlib import Path
from typing import List, Tuple, Optional, Any, Dict
import psycopg2


ROOT_DIR = Path(__file__).parent.parent.parent
CONFIG_FILENAME = ROOT_DIR / 'database.ini'


def config(filename: Path = CONFIG_FILENAME, section: str = 'postgresql') -> Dict:
    parser = ConfigParser()
    parser.read(filename)

    db = {}
    if parser.has_section(section):
        params = parser.items(section)
        for param in params:
            db[param[0]] = param[1]
    else:
        raise Exception('Section {0} not found in the {1} file'.format(section, filename))

    return db


class DatabaseConn:
    def __init__(self, config_filename=CONFIG_FILENAME):
        params = config(config_filename)
        self._conn = psycopg2.connect(**params)
        self._conn.autocommit = True

    def __del__(self):
        self._conn.close()

    def execute(
            self,
            query_or_stmt: str,
            has_res: bool = False) -> Optional[List[Tuple[Any]]]:
        """
        Creates a new cursor object, and executes the query/statement.  If
        `has_res` is `True`, then it returns the list of tuple results.
        :param query_or_stmt: The query or statement to run.
        :param has_res: Whether or not results should be returned. By default is `False`
        :return: If `has_res` is `True`, then a list of tuples.
        """
        cur = self._conn.cursor()
        cur.execute(query_or_stmt)
        if has_res:
            return cur.fetchall()

    def return_cursor(self):
        """
        :return: A psycopg2 cursor.
        """
        return self._conn.cursor()
