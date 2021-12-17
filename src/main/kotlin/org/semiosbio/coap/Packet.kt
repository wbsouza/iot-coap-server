package org.semiosbio.coap

import kotlinx.serialization.Serializable
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import kotlinx.serialization.*
import kotlinx.serialization.json.*


@Serializable
class Packet(var thingName: String? = null, var temperature: Float = 0.0f, var humidity: Float = 0.0f) {

    @Throws(IOException::class)
    fun toByteArray(): ByteArray? {
        return try {
            val baos = ByteArrayOutputStream()
            baos.write(MAGIC_BYTES)
            // thing name as length-encoded string
            val thingNameBytes = thingName!!.toByteArray(StandardCharsets.US_ASCII)
            val thingNameLength = thingNameBytes.size
            require(!(thingNameLength < 1 || thingNameLength > 128)) { "Invalid thing name. Must be 1 to 128 chars" }
            baos.write(thingNameLength and 0xFF)
            baos.write(thingNameBytes)
            baos.write(ByteBuffer.allocate(4).putFloat(temperature).array())
            baos.write(ByteBuffer.allocate(4).putFloat(humidity).array())
            baos.toByteArray()
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
    }

    override fun toString(): String {
        return Json.encodeToString(this)
    }


    companion object {

        private val MAGIC_BYTES = byteArrayOf(0x7E, 0x42)

        private fun readBytes(inst: InputStream, numBytes: Int): ByteArray? {
            var r = 0
            var len = 0
            val buffer = ByteArray(numBytes)
            while (len < numBytes && inst.read(buffer, len, numBytes - len).also { r = it } != -1) {
                len += r
            }
            if (len != numBytes) {
                throw IOException("Failed to read desired number of bytes")
            }
            return buffer
        }


        @Throws(IOException::class)
        fun read(inst: InputStream): Packet? {
            val b1 = inst.read()
            if (b1 == -1) {
                throw IOException("no data")
            }
            if (b1.toByte() != this. MAGIC_BYTES[0]) {
                throw IOException("Invalid stream, invalid magic byte 0")
            }
            if (inst.read().toByte() != MAGIC_BYTES[1]) {
                throw IOException("Invalid stream, invalid magic byte 1")
            }

            // read the thing name
            val nameLength = inst.read()
            val nameBytes = readBytes(inst, nameLength)
            val thingName = String(nameBytes!!, StandardCharsets.US_ASCII)

            // read temp float
            val tempBytes = readBytes(inst, 4)
            val temp = ByteBuffer.wrap(tempBytes).float

            // read humidity float
            val humidBytes = readBytes(inst, 4)
            val humid = ByteBuffer.wrap(humidBytes).float

            // return the packet
            val p = Packet()
            p.thingName = thingName
            p.temperature = temp
            p.humidity = humid
            return p
        }
    }

}