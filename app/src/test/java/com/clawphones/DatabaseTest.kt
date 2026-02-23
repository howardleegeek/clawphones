package com.clawphones

import org.junit.Assert.*
import org.junit.Test

class DatabaseTest {
    init {
        println("task_id=G3-02-CP - Starting tests for LocalDatabase")
    }

    @Test
    fun testInsertAndGet() {
        val db = LocalDatabase()
        val p = Person("1", "Alice", 25)
        assertTrue(db.insert(p))
        val got = db.get("1")
        assertNotNull(got)
        assertEquals(p, got)
    }

    @Test
    fun testInsertDuplicateIsRejected() {
        val db = LocalDatabase()
        val p = Person("1", "Alice", 25)
        assertTrue(db.insert(p))
        // Inserting a second item with the same id should fail
        val duplicate = Person("1", "Alicia", 26)
        assertFalse(db.insert(duplicate))
        // Original remains unchanged
        val got = db.get("1")
        assertEquals(p, got)
    }

    @Test
    fun testUpdateNonExistingIsRejected() {
        val db = LocalDatabase()
        val nonExist = Person("99", "Zed", 40)
        // Updating a non-existent record should fail
        assertFalse(db.update(nonExist))
    }

    @Test
    fun testUpdate() {
        val db = LocalDatabase()
        val p = Person("2", "Bob", 30)
        db.insert(p)
        val updated = p.copy(age = 31)
        assertTrue(db.update(updated))
        val got = db.get("2")
        assertEquals(updated, got)
    }

    @Test
    fun testDelete() {
        val db = LocalDatabase()
        val p = Person("3", "Carol", 22)
        db.insert(p)
        assertTrue(db.delete("3"))
        assertNull(db.get("3"))
    }

    @Test
    fun testSizeAndClear() {
        val db = LocalDatabase()
        db.insert(Person("a", "Ana", 20))
        db.insert(Person("b", "Ben", 21))
        assertEquals(2, db.size())
        db.clear()
        assertEquals(0, db.size())
    }
    
    @Test
    fun testGetNonExistingReturnsNull() {
        val db = LocalDatabase()
        val got = db.get("non-existent-id")
        assertNull(got)
    }
}
