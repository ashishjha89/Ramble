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
            firstName?.let { "$it " } ?: "" + lastName ?: ""

    val fullAddress: String = calculateAddress()

    private fun calculateAddress(): String {
        val streetNameStr = streetName?.let { "$it " } ?: ""
        val postCodeStr = postCode?.let { "$it " } ?: ""

        val firstLine = streetNameStr + houseNumber
        val secondLine = postCodeStr + city
        val thirdLine = country?.let { ", $it" } ?: ""

        val useFirstLineSeparator = firstLine.isNotBlank() && secondLine.isNotBlank()

        return firstLine +
                (if (useFirstLineSeparator) ", " else "") +
                secondLine +
                thirdLine
    }

}