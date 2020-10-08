package com.psychedelic9.bt4glasses.util

object ListUtils {
    fun <T>removeListDuplicate1(list: ArrayList<T>){
        val set: LinkedHashSet<T> = LinkedHashSet(list.size)
        set.addAll(list)
        list.clear()
        list.addAll(set)
    }
}