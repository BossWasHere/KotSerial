import com.fazecast.jSerialComm.SerialPort

val hexPattern = Regex("[0-9A-F]+")

class SerialInstance(private val serialPort: SerialPort) {

    private val builder = StringBuilder(1024)

    private var readMode = SerialMode.TEXT
    private var writeMode = SerialMode.TEXT
    private var started = false

    fun printInfo() {
        println("Port open. Commands:")
        println("  \\T text write -- \\X hex write")
        println("  \\b binary read -- \\t text read -- \\x hex read")
        println("  [In text mode] \\r send CR -- \\n send LF")
        println("  \\q quit")
    }

    fun start() {
        if (started) return
        started = true

        val txThread = Thread {
            while (serialPort.isOpen) {
                txOperation()
            }
        }
        txThread.start()

        while (serialPort.isOpen) {
            rxOperation()
        }

        txThread.interrupt()
    }

    private fun rxOperation() {
        val buffer = ByteArray(1024)
        builder.clear()

        var read: Int;
        do {
            read = serialPort.readBytes(buffer, buffer.size.toLong())
            if (read > 0) {
                when (readMode) {
                    SerialMode.TEXT -> builder.append(String(buffer, 0, read))
                    SerialMode.HEX -> {
                        builder.append(buffer.take(read).joinToString(" ") { String.format("%02X", it) }).append(' ')
                    }
                    SerialMode.BINARY -> {
                        builder.append(buffer.take(read).joinToString(" ") { String.format("%8s", Integer.toBinaryString(it.toInt())).replace(' ', '0') }).append(' ')
                    }
                }
            }
        } while (read > 0);

        if (builder.isNotEmpty()) {
            print("\r")
            println(builder.trim())
            printPrompt();
        }
    }

    private fun printPrompt() {
        print("[${writeMode.name.lowercase()}]> ")
    }

    private fun txOperation() {
        printPrompt();

        try {
            when (val input = readLine()) {
                null -> {}
                "\\q" -> {
                    serialPort.closePort()
                    println("Connection closed")
                }
                //"\\B" -> writeMode = SerialMode.BINARY
                "\\T" -> writeMode = SerialMode.TEXT
                "\\X" -> writeMode = SerialMode.HEX
                "\\b" -> readMode = SerialMode.BINARY
                "\\t" -> readMode = SerialMode.TEXT
                "\\x" -> readMode = SerialMode.HEX
                else -> {

                    when (writeMode) {
                        SerialMode.HEX -> {
                            val upperInput = input.uppercase()
                            if (upperInput.matches(hexPattern)) {
                                val bytes = hexStringToByteArray(upperInput)
                                serialPort.writeBytes(bytes, bytes.size.toLong())
                            } else {
                                println("Invalid hex string")
                            }
                        }
                        SerialMode.TEXT -> {
                            val bytes = input.replace("\\r", "\r").replace("\\n", "\n").toByteArray(Charsets.UTF_8)
                            serialPort.writeBytes(bytes, bytes.size.toLong())
                        }
                        else -> {}
                    }

                }
            }
        } catch (e: InterruptedException) {
            return
        }
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val padded = if (s.length % 2 == 0) s else "0$s"
        val len = padded.length
        val array = ByteArray(len / 2)

        for (i in 0 until len step 2) {
            array[i / 2] = ((Character.digit(padded[i], 16) shl 4) + Character.digit(padded[i + 1], 16)).toByte()
        }

        return array
    }

}