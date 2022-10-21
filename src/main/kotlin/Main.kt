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
    for (port in ports.indices) {
        println("(${port + 1}) ${ports[port].systemPortName}")
    }

    var port: Int?
    do {
        print("Select a port: ")
        port = readLine()?.toIntOrNull()
    } while (port == null || port <= 0 || port > ports.size)
    port--

    var baud: Int?
    do {
        print("Select a baud rate: ")
        baud = readLine()?.toIntOrNull()
    } while (baud == null || baud < 0)

    val serialPort = ports[port]
    serialPort.baudRate = baud

    println("Opening port ${serialPort.systemPortName} at $baud baud...")

    while (!serialPort.openPort(safetySleepTime)) {
        println("Failed to open port")
    }
    serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, readTimeoutMs, 0)

    val serialInstance = SerialInstance(serialPort)
    serialInstance.sendInfo()
    serialInstance.start()
}