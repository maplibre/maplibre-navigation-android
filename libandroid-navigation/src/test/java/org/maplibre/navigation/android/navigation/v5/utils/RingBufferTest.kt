package org.maplibre.navigation.android.navigation.v5.utils

import com.google.common.collect.Lists
import org.junit.Assert
import org.junit.Test
import org.maplibre.navigation.android.navigation.v5.BaseTest

class RingBufferTest : BaseTest() {
    @Test
    fun testBounds() {
        val buffer = RingBuffer<Int>(1)
        buffer.add(1)
        buffer.addFirst(2)
        buffer.addLast(3)
        buffer.addAll(Lists.newArrayList(4))
        buffer.push(5)
        buffer.add(6)

        Assert.assertEquals(1, buffer.size)
    }

    @Test
    fun testLifoOrder() {
        val buffer = RingBuffer<Int>(1)
        buffer.add(1)
        buffer.add(2)

        Assert.assertEquals(1, buffer.size)
        Assert.assertEquals(2.0, buffer.pop().toDouble(), DELTA)
    }

    @Test
    @Throws(Exception::class)
    fun testFifo() {
        val buffer = RingBuffer<Int>(2)
        buffer.add(1)
        buffer.add(2)

        Assert.assertEquals(2, buffer.size)
        Assert.assertEquals(1.0, buffer.pop().toDouble(), DELTA)
    }

    @Test
    fun testPeek() {
        val buffer = RingBuffer<Int>(2)
        buffer.add(1)
        buffer.add(2)
        buffer.add(3)
        Assert.assertEquals(2, buffer.size)
        Assert.assertEquals(2.0, buffer.peekFirst()!!.toDouble(), DELTA)
        Assert.assertEquals(3.0, buffer.peekLast()!!.toDouble(), DELTA)
    }
}