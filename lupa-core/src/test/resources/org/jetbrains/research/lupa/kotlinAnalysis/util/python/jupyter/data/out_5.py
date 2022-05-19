import numpy as np



import plt.pyplot as plt


x = np.linspace(-2, 2, 9).reshape((3,3))
print(x / 2)
print('hello')
def foo(x: int = 1):
    assert(isinstance(x, np.ndarray))
    return x * x / 2

print(foo(x))
class A:
    def __init__(self):
        self.string = "It's class A"


def create_a():
    a = A()
    return a

lots_of_A = [create_a().string for _ in range(100)]







print(lots_of_A[3:5])