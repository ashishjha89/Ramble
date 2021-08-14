package com.ramble.identity.repo.persistence.entity

import org.junit.Test
import kotlin.test.assertEquals

class StringListConverterTest {

    private val stringListConverter = StringListConverter()

    @Test
    fun convertToDatabaseColumnTest() {
        assertEquals("user;admin", stringListConverter.convertToDatabaseColumn(listOf("user", "admin")))
        assertEquals("user", stringListConverter.convertToDatabaseColumn(listOf("user")))
        assertEquals("", stringListConverter.convertToDatabaseColumn(emptyList()))
    }

    @Test
    fun convertToEntityAttributeTest() {
        assertEquals(listOf("user", "admin"), stringListConverter.convertToEntityAttribute("user;admin"))
        assertEquals(listOf("user"), stringListConverter.convertToEntityAttribute("user"))
        assertEquals(emptyList(), stringListConverter.convertToEntityAttribute(""))
    }
}