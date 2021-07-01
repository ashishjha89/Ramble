package com.ramble.identity.models

import com.ramble.identity.configurations.grantedAuthorities
import com.ramble.identity.configurations.permissions
import org.springframework.security.core.GrantedAuthority

data class ApplicationUser(
        val id: String,
        val email: String,
        val password: String? = null,
        val roles: List<String> = listOf(),
        val registrationDateInSeconds: Long? = null,
        val activationDateInSeconds: Long? = null,
        val firstName: String? = null,
        val lastName: String? = null,
        val nickname: String? = null,
        val age: Int? = null,
        val gender: String? = null,
        val houseNumber: String? = null,
        val streetName: String? = null,
        val postCode: String? = null,
        val city: String? = null,
        val country: String? = null,
        val accountStatus: String? = null
) {

    private val validRoles: List<Roles> =
            Roles.values().map { it.toString() }.filter { roles.contains(it) }.map { Roles.valueOf(it) }

    val permissions: Set<String> =
            validRoles.flatMap { role -> role.permissions }.toSet()

    val grantedAuthorities: Set<GrantedAuthority> =
            validRoles.flatMap { it.grantedAuthorities }.toSet()

    val fullName: String =
            firstName?.let { "$it " } ?: "" + lastName ?: ""

    val fullAddress: String
        get() {
            val firstLine = streetName?.let { "$it " } ?: "" + houseNumber ?: ""
            val secondLine = postCode?.let { "$it " } ?: "" + city ?: ""
            val thirdLine = country?.let { ", $it" } ?: ""
            val useFirstLineSeparator = firstLine.isNotBlank() && secondLine.isNotBlank()
            return firstLine + if (useFirstLineSeparator) "," else "" + secondLine + thirdLine
        }

}