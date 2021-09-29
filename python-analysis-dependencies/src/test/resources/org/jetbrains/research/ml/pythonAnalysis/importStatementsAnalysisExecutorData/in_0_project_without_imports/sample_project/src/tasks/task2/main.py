def factorial(number: int) -> int:
    if number == 0 or number == 1:
        return 1

    return number * factorial(number - 1)


def fibonacci(number: int) -> int:
    if number == 0 or number == 1:
        return number

    return fibonacci(number - 1) + fibonacci(number - 2)


if __name__ == '__main__':
    print(factorial(10))
    print(fibonacci(10))
