package org.semiosbio.coap

import com.google.gson.JsonObject

import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.CoapServer
import org.eclipse.californium.core.coap.CoAP.ResponseCode
import org.eclipse.californium.core.server.resources.CoapExchange
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.iotdataplane.IotDataPlaneClient
import software.amazon.awssdk.services.iotdataplane.model.PublishRequest
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.*


private class DataResource : CoapResource("data") {

    override fun handlePUT(exchange: CoapExchange) {
        try {
            val p = Packet.read(ByteArrayInputStream(exchange.requestPayload))
            println("[Incoming] " + p.toString())
            val msg = JsonObject()
            msg.addProperty("temperature", p!!.temperature)
            msg.addProperty("humidity", p!!.humidity)
            val iot = IotDataPlaneClient.builder().build()
            iot.publish(
                PublishRequest.builder() //
                    .topic("payload-drops/payloads/" + p!!.thingName) //
                    .payload(SdkBytes.fromUtf8String(msg.toString())) //
                    .qos(1) //
                    .build()
            )
            exchange.respond(ResponseCode.CREATED)
            println("[Success] $p")
        } catch (ex: IOException) {
            ex.printStackTrace()
            exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR)
        }
    }
}


fun main(args: Array<String>) {

    val server = CoapServer()
    server.add(DataResource())
    server.start()
    println("CoAP Server Started")

    ServerSocket(5684).use { healthCheckServer ->
        println("Health Check Server Started")
        while (true) {
            try {
                val sock: Socket = healthCheckServer.accept()
                sock.close()
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }
    }
}