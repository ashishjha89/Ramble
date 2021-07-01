package com.ramble.identity.configurations

import com.ramble.identity.models.Permission
import com.ramble.identity.models.Roles
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

val Roles.permissions: Set<String>
    get() =
        when (this) {
            Roles.Active -> listOf(Permission.ReadOwnInfo, Permission.WriteOwnInfo, Permission.DeleteOwnInfo).map { it.toString() }
            Roles.Admin -> Permission.values().map { it.toString() }
            else -> listOf()
        }.toSet()

val Roles.grantedAuthorities: Set<GrantedAuthority>
    get() =
        listOf(getGrantedAuthority("ROLE_$this"))
                .toMutableList()
                .plus(permissions.toList().map { getGrantedAuthority(it) })
                .toSet()

private fun getGrantedAuthority(role: String): GrantedAuthority = SimpleGrantedAuthority(role)