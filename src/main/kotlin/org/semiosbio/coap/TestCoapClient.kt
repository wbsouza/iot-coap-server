package org.semiosbio.coap

import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.coap.CoAP.ResponseCode
import org.eclipse.californium.core.coap.MediaTypeRegistry


@Throws(Exception::class)
fun main(args: Array<String>) {
	// generate the payload
	val p = Packet()
	p.thingName = "DN12345"
	p.temperature = 15.645f
	p.humidity = 47.23f
	val payload = p.toByteArray()
	println("Payload: " + payload!!.size + " bytes")
	val url = "coap://3.143.79.68:5683/data"
	val client = CoapClient(url)
	val res = client.put(payload, MediaTypeRegistry.UNDEFINED)
	if (res != null) {
		if (res.code == ResponseCode.CREATED) {
			println("success")
		} else {
			println("error")
		}
	} else {
		println("no response")
	}
	client.shutdown()
}