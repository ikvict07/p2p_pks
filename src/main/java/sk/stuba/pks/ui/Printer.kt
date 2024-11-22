package sk.stuba.pks.ui

object Printer {
    private const val RESET = "\u001B[0m"
    private const val YELLOW = "\u001B[33m"
    private const val CYAN = "\u001B[36m"
    private const val ONE_LINE_LEN = 80

    fun printMessage(message: String, to: String) {
        val header = "Port \"$to\" received the message"
        val headerCentered = header.center(ONE_LINE_LEN - 2)

        println("$YELLOW${"-".repeat(ONE_LINE_LEN)}$RESET")
        println("|${YELLOW}$headerCentered${RESET}|")
        message.chunked(ONE_LINE_LEN).forEach {
            println("|${CYAN}${it.center(ONE_LINE_LEN - 2)}${RESET}|")
        }
        println("$YELLOW${"-".repeat(ONE_LINE_LEN)}$RESET")

    }


    fun printFile(file: String, to: String) {
        val header = "Port \"$to\" received the file"
        val headerCentered = header.center(ONE_LINE_LEN - 2)
        println("$YELLOW${"-".repeat(ONE_LINE_LEN)}$RESET")
        println("|${YELLOW}$headerCentered${RESET}|")
        println("|${CYAN}${file.center(ONE_LINE_LEN - 2)}${RESET}|")
        println("$YELLOW${"-".repeat(ONE_LINE_LEN)}$RESET")
    }

    private fun String.center(width: Int): String {
        if (this.length >= width) return this
        val leftPadding = (width - this.length) / 2
        val rightPadding = width - this.length - leftPadding
        return " ".repeat(leftPadding) + this + " ".repeat(rightPadding)
    }

}
