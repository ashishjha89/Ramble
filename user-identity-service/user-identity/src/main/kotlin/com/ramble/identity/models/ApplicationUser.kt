package com.ramble.identity.models

import org.springframework.security.core.GrantedAuthority

data class ApplicationUser(
    val id: String,
    val email: String,
    val password: String?,
    val roles: List<Roles>,
    val accountStatus: AccountStatus,
    val registrationDateInSeconds: Long,
    val firstName: String? = null,
    val lastName: String? = null,
    val nickname: String? = null,
    val age: Int? = null,
    val gender: Gender = Gender.Undisclosed,
    val houseNumber: String? = null,
    val streetName: String? = null,
    val postCode: String? = null,
    val city: String? = null,
    val country: String? = null,
    val activationDateInSeconds: Long? = null
) {

    val grantedAuthorities: Set<GrantedAuthority> =
        roles.flatMap { it.grantedAuthorities }.toSet()

    val fullName: String =
        when {
            firstName.isNullOrBlank() -> lastName ?: ""
            lastName.isNullOrBlank() -> firstName
            else -> "$firstName $lastName"
        }

    val fullAddress: String = calculateAddress()

    private fun calculateAddress(): String {
        val firstLine = when {
            houseNumber.isNullOrBlank() -> streetName ?: ""
            streetName.isNullOrBlank() -> houseNumber
            else -> "$streetName $houseNumber"
        }
        val secondLine = when {
            postCode.isNullOrBlank() -> city ?: ""
            city.isNullOrBlank() -> postCode
            else -> "$postCode $city"
        }
        val thirdLine = country ?: ""

        val useFirstLineSeparator = firstLine.isNotBlank() && secondLine.isNotBlank()
        val useSecondLineSeparator = thirdLine.isNotBlank() && (secondLine.isNotBlank() || firstLine.isNotBlank())

        return firstLine +
                (if (useFirstLineSeparator) ", " else "") +
                secondLine +
                (if (useSecondLineSeparator) ", " else "") +
                thirdLine
    }

}