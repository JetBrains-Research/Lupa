def is_palindrome(line: str) -> bool:
    return line == line[::-1]


if __name__ == '__main__':
    print(is_palindrome('tenet'))
