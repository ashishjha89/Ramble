package com.ramble.identity.models

enum class Gender {
    Female,
    Male,
    Agender,
    Transgender,
    Bigender,
    Cisgender,
    Intersex,
    Undisclosed,
    Other,
}

enum class AccountStatus {
    Activated,
    Suspended,
}

enum class Roles {
    Admin,
    Active,
    Suspended,
    RegisteredOnly
}

enum class Permission {
    ReadOwnInfo,
    WriteOwnInfo,
    DeleteOwnInfo,
    ReadAll,
    WriteAll,
    DeleteAll
}