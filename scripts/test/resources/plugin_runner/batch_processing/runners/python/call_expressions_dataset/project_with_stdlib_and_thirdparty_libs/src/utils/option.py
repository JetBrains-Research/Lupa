from enum import Enum, unique
from typing import List


@unique
class Option(Enum):
    SIN = 'sin'
    COS = 'cos'
    ABS = 'abs'

    @classmethod
    def values(cls) -> List[str]:
        return [option.value for option in cls]
