package com.example.android.genpass

/**
 * Created by narthana on 30/06/17.
 */
sealed class LinkedList<T> {
    operator fun plus(other: LinkedList<T>): LinkedList<T> {
        when (this) {
            is NonEmptyLinkedList<T> ->
                if (other is NonEmptyLinkedList<T>) {
                    last.value.tail = other
                    last = other.last
                }
            is EmptyLinkedList<T> -> return other
        }
        return this
    }
}
class EmptyLinkedList<T>: LinkedList<T>()
data class NonEmptyLinkedList<S>(var head: S, var tail: LinkedList<S>): LinkedList<S>() {
    var last: Lazy<NonEmptyLinkedList<S>> = lazy {
        fun findLast(p: LinkedList<S>): NonEmptyLinkedList<S> = when (p) {
            is NonEmptyLinkedList<S> -> if (p.tail is EmptyLinkedList) p else findLast(p.tail)
            is EmptyLinkedList -> throw IllegalStateException()
        }
        findLast(this)
    }

    operator fun get(n: Int): S? {
        if (n == 0) return head
        if (tail is NonEmptyLinkedList<S>) return (tail as NonEmptyLinkedList<S>)[n - 1]
        return null
    }

    operator fun set(n: Int, value: S) {
        if (n == 0) head = value
        else if (tail is NonEmptyLinkedList<S>) (tail as NonEmptyLinkedList<S>)[n - 1] = value
    }

    override fun toString(): String {
        val sb = StringBuilder().append("[").append(head)
        tailrec fun rec(p: LinkedList<S>): Unit = when (p) {
            is NonEmptyLinkedList<S> -> {
                sb.append(" ").append(p.head.toString())
                rec(p.tail)
            }
            is EmptyLinkedList<S> -> Unit
        }
        rec(this.tail)
        return sb.append("]").toString()
    }
}

fun<T> linkedListOf(vararg elems: T): LinkedList<T> {
    fun rec(list: List<T>): LinkedList<T> =
        if (list.isNotEmpty()) NonEmptyLinkedList<T>(list[0], rec(list.subList(1, list.size)))
        else EmptyLinkedList<T>()

    return rec(elems.toList())
}

fun<T> LinkedList<T>.filter(predicate: (T) -> Boolean): LinkedList<T> {
    fun rec(p: LinkedList<T>): LinkedList<T> = when (p) {
        is NonEmptyLinkedList<T> -> if (predicate(p.head)) p.apply { tail = rec(tail) }
                                    else rec(p.tail)
        is EmptyLinkedList -> p
    }
    return rec(this)
}

fun<T, R> LinkedList<T>.map(f: (T) -> R): LinkedList<R> {
    fun rec(p: LinkedList<T>): LinkedList<R> = when (p) {
        is NonEmptyLinkedList<T> -> NonEmptyLinkedList<R>(f(p.head), rec(p.tail))
        is EmptyLinkedList<T> -> EmptyLinkedList<R>()
    }
    return rec(this)
}

fun LinkedList<Int>.sum(): Int {
    tailrec fun rec(p: LinkedList<Int>, acc: Int): Int = when (p) {
        is EmptyLinkedList<Int> -> acc
        is NonEmptyLinkedList<Int> -> rec(p.tail, acc + p.head)
    }
    return rec(this, 0)
}

