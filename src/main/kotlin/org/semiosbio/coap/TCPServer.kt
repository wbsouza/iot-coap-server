package org.semiosbio.coap

import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.iotdataplane.IotDataPlaneClient
import software.amazon.awssdk.services.iotdataplane.model.PublishRequest
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

import com.google.gson.JsonObject

class Payload(var thingName: String, var temp: Float)

class ConnectionHandler(accept: Socket) : Runnable {

    val sock: Socket? = null

    @Throws(IOException::class)
    fun sendSuccess() {
        sock!!.getOutputStream().write(1)
        sock.getOutputStream().flush()
    }

    fun trySendError() {
        try {
            sock!!.getOutputStream().write(0)
            sock.getOutputStream().flush()
        } catch (e: IOException) {
            println("Error sending error ($e)")
        }
    }

    @Synchronized
    fun close() {
        try {
            if (!sock!!.isClosed) {
                sock.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun run() {
        try {
            val p = Packet.read(sock!!.getInputStream())
            println("Got packet: " + p.toString())
            val msg = JsonObject()
            msg.addProperty("temperature", p!!.temperature)
            msg.addProperty("humidity", p.humidity)
            val iot = IotDataPlaneClient.builder().build()
            iot.publish(
                PublishRequest.builder() //
                    .topic("payload-drops/payloads/" + p.thingName) //
                    .payload(SdkBytes.fromUtf8String(msg.toString())) //
                    .qos(1) //
                    .build()
            )
            sendSuccess()
        } catch (ex: IOException) {
            if ("no data" == ex.message) {
                // just close the socket - probably a health check
                close()
            } else {
                ex.printStackTrace()
                trySendError()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            trySendError()
        } finally {
            close()
        }
    }

}


fun main(args: Array<String>) {
    val executor = Executors.newFixedThreadPool(150)
    val serverSocket = ServerSocket(5683)
    println("Listening for connections...")
    while (true) {
        ConnectionHandler(serverSocket.accept())
    }
}


