import logging
from math import cos, sin
import sys

from utils.option import Option

logger = logging.getLogger(__name__)

if __name__ == '__main__':
    command = input(
        'Enter the command '
        '('
        f'"{Option.SIN.value}" to calculate the sine, '
        f'"{Option.COS.value}" to calculate the cosine, '
        f'"{Option.ABS.value}" to calculate the absolute value'
        '): '
    )

    try:
        option = Option(command)
    except ValueError:
        logger.error(f'Unknown command: {command}.')
        sys.exit(1)

    if option == Option.SIN:
        number = int(input('Enter a number: '))
        print(f'sin({number}) = {sin(number)}')
    elif option == Option.COS:
        number = int(input('Enter a number: '))
        print(f'cos({number}) = {cos(number)}')
    elif option == Option.ABS:
        number = int(input('Enter a number: '))
        print(f'abs({number}) = {abs(number)}')
    else:
        logger.error(f'Unknown option: {option}.')
