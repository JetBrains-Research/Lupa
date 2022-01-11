from first_package.main import some_function


def another_function():
    for x in range(0, 10):
        if some_function(x):
            print('True!')
