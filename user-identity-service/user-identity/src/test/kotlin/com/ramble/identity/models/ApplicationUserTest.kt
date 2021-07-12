package com.ramble.identity.models

import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals

class ApplicationUserTest {

    private val applicationUser = ApplicationUser(
            id = "id",
            email = "email",
            firstName = "firstName",
            lastName = "lastName",
            nickname = "nickname",
            age = 30,
            gender = Gender.Male,
            houseNumber = "houseNumber",
            streetName = "streetName",
            postCode = "postCode",
            city = "city",
            country = "country",
            password = "password",
            roles = listOf(Roles.User),
            accountStatus = AccountStatus.Activated,
            registrationDateInSeconds = Instant.now().epochSecond,
            activationDateInSeconds = Instant.now().epochSecond
    )

    @Test
    fun grantedAuthoritiesTest() {
        val userGrantedAuthorities = Roles.User.grantedAuthorities
        val adminGrantedAuthorities = Roles.Admin.grantedAuthorities

        val user1 = applicationUser.copy(roles = listOf(Roles.User))
        val user2 = applicationUser.copy(roles = listOf(Roles.Admin))

        assertEquals(userGrantedAuthorities, user1.grantedAuthorities)
        assertEquals(adminGrantedAuthorities, user2.grantedAuthorities)
    }

    @Test
    fun fullNameTest() {
        val userWithFirstAndLastName = applicationUser
        val userWithFirstNameOnly = applicationUser.copy(lastName = "")
        val userWithLastNameOnly = applicationUser.copy(firstName = "")
        val applicationUserWithNoName = applicationUser.copy(firstName = "", lastName = "")

        assertEquals("firstName lastName", userWithFirstAndLastName.fullName)
        assertEquals("firstName", userWithFirstNameOnly.fullName)
        assertEquals("lastName", userWithLastNameOnly.fullName)
        assertEquals("", applicationUserWithNoName.fullName)
    }

    @Test
    fun addressTest() {
        val houseNumber = "houseNumber"
        val streetName = "streetName"
        val postCode = "postCode"
        val city = "city"
        val country = "country"

        val userWithFullAddress = applicationUser.copy(
                houseNumber = houseNumber,
                streetName = streetName,
                postCode = postCode,
                city = city,
                country = country
        )

        val userWithoutAddress = applicationUser.copy(
                houseNumber = "",
                streetName = "",
                postCode = "",
                city = "",
                country = ""
        )

        "user with full address".run {
            val expectedAddress = "$streetName $houseNumber, $postCode $city, $country"
            assertEquals(expectedAddress, userWithFullAddress.fullAddress)
        }

        "user without country".run {
            val user = userWithFullAddress.copy(country = "")
            val expectedAddress = "$streetName $houseNumber, $postCode $city"
            assertEquals(expectedAddress, user.fullAddress)
        }

        "user without house-number".run {
            val user = userWithFullAddress.copy(houseNumber = "")
            val expectedAddress = "$streetName, $postCode $city, $country"
            assertEquals(expectedAddress, user.fullAddress)
        }

        "user without street-name".run {
            val user = userWithFullAddress.copy(streetName = "")
            val expectedAddress = "$houseNumber, $postCode $city, $country"
            assertEquals(expectedAddress, user.fullAddress)
        }

        "user without street-name and house-number".run {
            val user = userWithFullAddress.copy(streetName = "", houseNumber = "")
            val expectedAddress = "$postCode $city, $country"
            assertEquals(expectedAddress, user.fullAddress)
        }

        "user without postcode and city".run {
            val user = userWithFullAddress.copy(postCode = "", city = "")
            val expectedAddress = "$streetName $houseNumber, $country"
            assertEquals(expectedAddress, user.fullAddress)
        }

        "user without postcode, city and country".run {
            val user = userWithFullAddress.copy(postCode = "", city = "", country = "")
            val expectedAddress = "$streetName $houseNumber"
            assertEquals(expectedAddress, user.fullAddress)
        }

        "user without houseNumber and postcode".run {
            val user = userWithFullAddress.copy(houseNumber = "", postCode = "")
            val expectedAddress = "$streetName, $city, $country"
            assertEquals(expectedAddress, user.fullAddress)
        }

        "user without houseNumber and city".run {
            val user = userWithFullAddress.copy(houseNumber = "", city = "")
            val expectedAddress = "$streetName, $postCode, $country"
            assertEquals(expectedAddress, user.fullAddress)
        }

        "user without houseNumber, postcode and city".run {
            val user = userWithFullAddress.copy(houseNumber = "", postCode = "", city = "")
            val expectedAddress = "$streetName, $country"
            assertEquals(expectedAddress, user.fullAddress)
        }

        "user without street-name, house-number and country".run {
            val user = userWithFullAddress.copy(streetName = "", houseNumber = "", country = "")
            val expectedAddress = "$postCode $city"
            assertEquals(expectedAddress, user.fullAddress)
        }

        "user with only country".run {
            val user = userWithoutAddress.copy(country = country)
            assertEquals(country, user.fullAddress)
        }

        "user with only city".run {
            val user = userWithoutAddress.copy(city = city)
            assertEquals(city, user.fullAddress)
        }

        "user with only postcode".run {
            val user = userWithoutAddress.copy(postCode = postCode)
            assertEquals(postCode, user.fullAddress)
        }

        "user with only street-name".run {
            val user = userWithoutAddress.copy(streetName = streetName)
            assertEquals(streetName, user.fullAddress)
        }

        "user with only house-number".run {
            val user = userWithoutAddress.copy(houseNumber = houseNumber)
            assertEquals(houseNumber, user.fullAddress)
        }
    }
}