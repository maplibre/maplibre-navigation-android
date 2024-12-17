package org.maplibre.navigation.core.utils

import org.maplibre.navigation.core.BaseTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RingBufferTest : BaseTest() {

    @Test
    fun testBounds() {
        val buffer = RingBuffer<Int>(1)
        buffer.add(1)
        buffer.addFirst(2)
        buffer.addLast(3)
        buffer.addAll(ArrayList(4))
        buffer.addFirst(5)
        buffer.add(6)

        assertEquals(1, buffer.size)
    }

    @Test
    fun testLifoOrder() {
        val buffer = RingBuffer<Int>(1)
        buffer.add(1)
        buffer.add(2)

        assertEquals(1, buffer.size)
        assertEquals(2.0, buffer.removeLast().toDouble(), DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun testFifo() {
        val buffer = RingBuffer<Int>(2)
        buffer.add(1)
        buffer.add(2)

        assertEquals(2, buffer.size)
        assertEquals(1.0, buffer.removeFirst().toDouble(), DELTA)
    }

    @Test
    fun testPeek() {
        val buffer = RingBuffer<Int>(2)
        buffer.add(1)
        buffer.add(2)
        buffer.add(3)
        assertEquals(2, buffer.size)
        assertEquals(2.0, buffer.removeFirst().toDouble(), DELTA)
        assertEquals(3.0, buffer.removeLast().toDouble(), DELTA)
    }
}