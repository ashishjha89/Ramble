package com.ramble.identity.repo.persistence.entity

import javax.persistence.AttributeConverter
import javax.persistence.Converter


@Converter
class StringListConverter : AttributeConverter<List<String>?, String> {

    private val splitChar = ";"

    override fun convertToDatabaseColumn(stringList: List<String>?): String? =
        if (stringList != null) java.lang.String.join(splitChar, stringList) else ""

    override fun convertToEntityAttribute(string: String?): List<String> =
        if (string.isNullOrBlank()) emptyList() else string.split(splitChar.toRegex())
}