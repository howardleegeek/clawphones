package com.clawphones

data class Person(val id: String, val name: String, val age: Int)

class LocalDatabase {
    private val store = mutableMapOf<String, Person>()

    fun insert(person: Person): Boolean {
        if (store.containsKey(person.id)) return false
        store[person.id] = person
        return true
    }

    fun get(id: String): Person? = store[id]

    fun update(person: Person): Boolean {
        if (!store.containsKey(person.id)) return false
        store[person.id] = person
        return true
    }

    fun delete(id: String): Boolean = store.remove(id) != null

    fun clear() { store.clear() }

    fun size(): Int = store.size
}
