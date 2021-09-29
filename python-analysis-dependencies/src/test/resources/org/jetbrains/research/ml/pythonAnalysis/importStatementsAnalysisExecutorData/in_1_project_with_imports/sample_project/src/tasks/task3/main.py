from ...common.common import common_function
from .non_existent_package import SomeClass

if __name__ == '__main__':
    common_function()
    some_instance = SomeClass()
    print(some_instance)
