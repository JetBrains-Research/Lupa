if __name__ == '__main__':
    start = int(input('Start: '))
    stop = int(input('Stop: '))
    step = int(input('Step: '))
    print(sum(list(range(start, stop + 1, step))))
