package org.maplibre.navigation.core.utils


class RingBuffer<T>(private val maxSize: Int) {

    private val values = ArrayDeque<T>()

    init {
        require(maxSize > 0) { "Max size must be greater than 0." }
    }

    fun add(element: T): Boolean {
        val result = values.add(element)
        resize()
        return result
    }

    fun addFirst(item: T) {
        values.addFirst(item)
        resize()
    }

    fun addLast(item: T) {
        values.addLast(item)
        resize()
    }

    fun addAll(elements: Collection<T>): Boolean {
        val result =  values.addAll(elements)
        resize()
        return result
    }

    fun push(item: T) {
        values.addFirst(item)
        resize()
    }

    private fun resize() {
        while (values.size > maxSize) {
            values.removeFirst()
        }
    }
}