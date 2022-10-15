# This is a factorial
def factorial(n: int) -> int:
    """Calculate factorial: n!"""
    if n == 0:
        return 1  # Return 1 because 0! = 1
    """ n! = n * (n - 1)! """
    return n * factorial(n - 1)


if __name__ == '__main__':
    n = int(input())  # convert string to int
    '''This is a very strange comment'''
    print(factorial(n))
