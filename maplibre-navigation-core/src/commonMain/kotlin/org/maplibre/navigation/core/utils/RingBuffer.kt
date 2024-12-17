package org.maplibre.navigation.core.utils


class RingBuffer<T>(private val maxSize: Int) : List<T> {

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
        val result = values.addAll(elements)
        resize()
        return result
    }

    fun removeFirst(): T {
        return values.removeFirst()
    }

    fun removeLast(): T {
        return values.removeLast()
    }

    fun clear() {
        values.clear()
    }

    private fun resize() {
        while (size > maxSize) {
            values.removeFirst()
        }
    }

    override val size: Int
        get() = values.size

    override fun get(index: Int) = values[index]

    override fun isEmpty() = values.isEmpty()

    override fun iterator() = values.iterator()

    override fun listIterator() = values.listIterator()

    override fun listIterator(index: Int) = values.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int) = values.subList(fromIndex, toIndex)

    override fun lastIndexOf(element: T) = values.lastIndexOf(element)

    override fun indexOf(element: T) = values.indexOf(element)

    override fun containsAll(elements: Collection<T>) = values.containsAll(elements)

    override fun contains(element: T) = values.contains(element)
}