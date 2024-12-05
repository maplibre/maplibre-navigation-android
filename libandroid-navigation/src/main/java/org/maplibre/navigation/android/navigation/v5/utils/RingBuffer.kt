package org.maplibre.navigation.android.navigation.v5.utils

import java.util.ArrayDeque

class RingBuffer<T>(private val maxSize: Int) : ArrayDeque<T>() {

    init {
        require(maxSize > 0) { "Max size must be greater than 0." }
    }

    override fun add(element: T): Boolean {
        val result = super.add(element)
        resize()
        return result
    }

    override fun addFirst(item: T) {
        super.addFirst(item)
        resize()
    }

    override fun addLast(item: T) {
        super.addLast(item)
        resize()
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val result = super.addAll(elements)
        resize()
        return result
    }

    override fun push(item: T) {
        super.push(item)
        resize()
    }

    private fun resize() {
        while (size > maxSize) {
            pop()
        }
    }
}