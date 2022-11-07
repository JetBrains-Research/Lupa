from typing import Dict

from cerberus import Validator
from cerberus.errors import ErrorList


def check_config(config: Dict, schema: Dict) -> ErrorList:
    v = Validator(schema)
    v.validate(config)
    return v.errors
