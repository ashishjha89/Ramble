### Adding this library to your project:

Add following in your pom file:
```
<dependency>
   <groupId>com.ramble.accesstoken</groupId>
   <artifactId>access-token-validator-lib</artifactId>
   <version>1.0</version>
</dependency>
```

Include @EnableRedisRepositories @EntityScan in the application class.
For example:
```
@SpringBootApplication(scanBasePackages = ["com.ramble"])
@EnableRedisRepositories(basePackages = ["com.ramble.accesstoken"])
@EntityScan(value = ["com.ramble.accesstoken.*"])
```

### Install library in local system
```
$ mvn clean install
```
The library will be saved to:
/Users/{your-home-directory}/.m2/repository/com/ramble/accesstoken/access-token-validator-lib/1.0/access-token-validator-lib-1.0.jar


### Access token information
* JWT Token
* Issued date-time
* Expired date-time
* UserId
* EmailId
* ClientId
* Roles

### Access token validation
* Check if token is not expired
* Check token has all the information (clientId, userId, emailId, riles)
* Check token is not present in 'DisabledClientTokens' Redis cache.

### Disable access token
* Request is made for a new access-token to be added to 'disabled token' list.
* Get all existing disabled tokens for the clientId.
* Filter out those tokens which has still valid format (i.e. present for same clientId and not yet expired).
* Create a new 'disabled token list' with the (a) new disabled-token to add (b) above filtered token.
* Add the above list in 'DisabledClientTokens' for the 'clientId'.