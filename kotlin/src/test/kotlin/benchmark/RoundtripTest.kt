package benchmark

import benchmark.model.Fixture
import benchmark.model.v2.Generators
import benchmark.serializers.OmniBufSer
import benchmark.serializers.Registry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

class RoundtripTest {
    @Test
    fun allSerializersRoundtripMessage() {
        roundtripType("message")
    }

    @Test
    fun allSerializersRoundtripAllFixtures() {
        for (type in FIXTURES) {
            roundtripType(type)
        }
    }

    @Test
    fun omnibufMetadataAndBatchRoundtrip() {
        val ser = OmniBufSer()
        assertEquals("omnibuf", ser.name())
        assertEquals("adapted", ser.streamMode())
        assertEquals("message", ser.nativeKind())
        assertFalse(ser.version().isBlank())
        assertNotEquals("unknown", ser.version())
        assertFalse(ser.supports("tensor"))
        for (type in FIXTURES) {
            assertTrue(ser.supports(type), type)
            val batch = ArrayList(Generators.instances(type, emptyMap(), 42L, 100))
            val fx = Fixture(type, batch)
            ser.prepare(fx)
            val buf = ser.serializeBytes(fx)
            val out = ser.toDomain(ser.deserializeBytes(buf))
            assertTrue(Fidelity.check(batch, out)) { "omnibuf batch $type" }
        }
    }

    @Test
    fun compressSizesGzipHello() {
        val c = Compress.sizes("hello".toByteArray(StandardCharsets.UTF_8))
        assertTrue(c[0] in 20..40, "gzip=${c[0]}")
        val empty = Compress.sizes(ByteArray(0))
        assertTrue(empty[0] == 0 && empty[1] == 0)
    }

    companion object {
        private val FIXTURES = arrayOf("message", "document", "telemetry", "strings", "event")

        private fun roundtripType(type: String) {
            val expected = Generators.makeOne(type, emptyMap(), 42L, 0)
            val fx = Fixture(type, expected)
            for (ser in Registry.all()) {
                if (!ser.supports(type)) continue
                ser.prepare(fx)
                val buf = ser.serializeBytes(fx)
                val out = ser.toDomain(ser.deserializeBytes(buf))
                assertTrue(Fidelity.check(expected, out)) {
                    "fidelity failed for ${ser.name()} on $type"
                }
            }
        }
    }
}
