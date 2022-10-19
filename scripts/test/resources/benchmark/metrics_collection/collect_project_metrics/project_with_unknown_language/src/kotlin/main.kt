fun factorial(n: Int): Int {
    if (n == 0) {
        return 1
    }
    return n * factorial(n - 1)
}

fun main() {
    val n = readlnOrNull()?.toIntOrNull()
    n?.let { println(factorial(it)) } ?: println("Not a number.")
}
