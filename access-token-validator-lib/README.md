#### Including library detail
Include @EnableRedisRepositories @EntityScan in the application class. For example:
```
@EnableRedisRepositories(basePackages = ["com.ramble.accesstoken"])
@EntityScan(value = ["com.ramble.accesstoken.*"])
```

#### Install library in local system
```
$ maven clean install
```
The library will be saved to:
/Users/{your-home-directory}/.m2/repository/com/ramble/accesstoken/access-token-validator-lib/1.0/access-token-validator-lib-1.0.jar
