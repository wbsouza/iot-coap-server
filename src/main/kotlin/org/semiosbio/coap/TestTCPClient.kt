package org.semiosbio.coap


import java.io.OutputStream
import java.net.Socket


@Throws(Exception::class)
fun main(args: Array<String>) {
    // generate the payload
    val p = Packet()
    p.thingName = "DN12345"
    p.temperature = 15.645f
    p.humidity = 47.23f
    val payload = p.toByteArray()

    println("Payload: ${payload?.size} bytes")

    println("Connecting...")
    // String host = "localhost";
    val host = "3.143.79.68"
    Socket (host, 5683).use { sock ->
        val out: OutputStream = sock.getOutputStream()
        out.write(payload)
        out.flush()
        val res: Int = sock.getInputStream().read()
        println(res)
        if (res == 1) {
            println("got success")
        } else {
            println("got error")
        }
    }
}