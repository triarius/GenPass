package com.example.android.genpass

/**
 * Created by narthana on 30/06/17.
 */

sealed class LinkedList<out T>
object EmptyLinkedList: LinkedList<Nothing>()
data class NonEmptyLinkedList<T>(var head: T, var tail: LinkedList<T>): LinkedList<T>()

fun <T> linkedListOf(vararg elems: T, tail: LinkedList<T> = EmptyLinkedList): LinkedList<T> {
    fun rec(list: List<T>): LinkedList<T> =
        if (list.isNotEmpty()) NonEmptyLinkedList<T>(list[0], rec(list.subList(1, list.size)))
        else EmptyLinkedList
    return rec(elems.toList())
}

// TODO: replace the following with a folding implementation

fun <T> LinkedList<T>.filter(predicate: (T) -> Boolean): LinkedList<T> {
    fun rec(p: LinkedList<T>): LinkedList<T> = when (p) {
        is NonEmptyLinkedList<T> -> if (predicate(p.head)) p.apply { tail = rec(tail) }
                                    else rec(p.tail)
        is EmptyLinkedList -> p
    }
    return rec(this)
}

fun <T, R> LinkedList<T>.map(f: (T) -> R): LinkedList<R> {
    fun rec(p: LinkedList<T>): LinkedList<R> = when (p) {
        is NonEmptyLinkedList<T> -> NonEmptyLinkedList<R>(f(p.head), rec(p.tail))
        is EmptyLinkedList -> EmptyLinkedList
    }
    return rec(this)
}

fun LinkedList<Int>.sum(): Int {
    tailrec fun rec(p: LinkedList<Int>, acc: Int): Int = when (p) {
        is EmptyLinkedList -> acc
        is NonEmptyLinkedList<Int> -> rec(p.tail, acc + p.head)
    }
    return rec(this, 0)
}