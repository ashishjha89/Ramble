package com.ramble.identity.repo.persistence.entity

import java.io.Serializable
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id

@Entity(name = "application_user_entity")
class ApplicationUserEntity(
    @Id val id: String = "",
    val email: String = "",
    val password: String = "",
    @Convert(converter = StringListConverter::class) val roles: List<String> = emptyList(),
    val accountStatus: String = "",
    val registrationDateInSeconds: Long = -1,
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String = "",
    val age: Int = -1,
    val gender: String = "Undisclosed",
    val houseNumber: String = "",
    val streetName: String = "",
    val postCode: String = "",
    val city: String = "",
    val country: String = "",
    val activationDateInSeconds: Long = -1
) : Serializable