from enum import Enum, unique


@unique
class Option(Enum):
    SIN = 'sin'
    COS = 'cos'
    ABS = 'abs'
