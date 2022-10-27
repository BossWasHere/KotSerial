import com.fazecast.jSerialComm.SerialPort

const val safetySleepTime = 1000
const val readTimeoutMs = 100

fun main(args: Array<String>) {

    val ports = SerialPort.getCommPorts()

    if (ports.isEmpty()) {
        println("No serial ports found")
        return
    }

    println("Available serial ports:")
    println("(0) Toggle advanced mode")
    for (port in ports.indices) {
        println("(${port + 1}) ${ports[port].systemPortName}")
    }

    var port: Int?
    var advanced = false
    do {
        print("Select a port: ")
        port = readLine()?.toIntOrNull()

        if (port == 0) {
            advanced = !advanced
            println("${if (advanced) "Enabled" else "Disabled"} advanced mode")
        }
    } while (port == null || port <= 0 || port > ports.size)
    port--

    var baud: Int?
    do {
        print("Select a baud rate: ")
        baud = readLine()?.toIntOrNull()
    } while (baud == null || baud < 0)

    val serialPort = ports[port]
    serialPort.baudRate = baud

    if (advanced) {
        var parity: Int
        println("Parity options:")
        println("(1) None [default]")
        println("(2) Even")
        println("(3) Odd")
        println("(4) Mark")
        println("(5) Space")
        do {
            print("Select parity mode: ")
            parity = readLine()?.toIntOrNull() ?: 1
        } while (parity <= 0 || parity > 5)
        serialPort.parity = --parity

        var numStopBits: Int
        println("Stop bits:")
        println("(1) One [default]")
        println("(2) One point five")
        println("(3) Two")
        do {
            print("Select #stop bits: ")
            numStopBits = readLine()?.toIntOrNull() ?: 1
        } while (numStopBits <= 0 || numStopBits > 3)
        serialPort.numStopBits = numStopBits

        print("Enter read timeout ms [default: ${readTimeoutMs}]: ")
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
            (readLine()?.toIntOrNull() ?: readTimeoutMs).coerceAtLeast(0), 0)
    } else {
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, readTimeoutMs, 0)
    }

    println("Opening port ${serialPort.systemPortName} at $baud baud...")

    while (!serialPort.openPort(safetySleepTime)) {
        println("Failed to open port")
    }

    val serialInstance = SerialInstance(serialPort)
    serialInstance.printInfo()
    serialInstance.start()
}