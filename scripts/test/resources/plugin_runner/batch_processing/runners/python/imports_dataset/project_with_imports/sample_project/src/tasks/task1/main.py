from math import sin, cos, pow

from src.common.common import common_function


def quick_math() -> float:
    common_function()
    return pow(sin(1), 2) + pow(cos(1), 2)


if __name__ == '__main__':
    print(quick_math())
