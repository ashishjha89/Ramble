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
