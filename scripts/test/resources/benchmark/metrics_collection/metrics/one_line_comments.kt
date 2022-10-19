// This is a factorial
fun factorial(n: Int): Int {
    /* 0! = 1 */
    if (n == 0) {
        return 1
    }
    return n * factorial(n - 1) // n! = n * (n - 1)!
}

fun main() {
    // /* o_0 */
    val n = readlnOrNull()?.toIntOrNull()
    println(n?.let { factorial(it) } ?: "Not a number.")
}
