package spam.blocker.util

import java.net.ServerSocket
import kotlin.concurrent.thread
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HttpTest {
    @Test
    fun requestFinishesWhenServerDoesNotRespond() {
        ServerSocket(0).use { server ->
            val serverThread = thread {
                server.accept().use { socket ->
                    socket.getInputStream().bufferedReader().readLine()
                    try {
                        Thread.sleep(7_000)
                    } catch (_: InterruptedException) {
                    }
                }
            }

            val startedAt = System.nanoTime()
            val result = httpRequest("http://127.0.0.1:${server.localPort}/")
            val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000

            try {
                assertNotNull(result?.exception)
                assertTrue(elapsedMs < 7_000, "Request took ${elapsedMs}ms")
            } finally {
                serverThread.interrupt()
            }
        }
    }
}
