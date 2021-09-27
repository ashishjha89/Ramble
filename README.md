### Start Ramble locally

1. Start MySql & Redis

```
$ mysql.server start
$ brew services start redis
```

Note: To stop MySql & Redis:
```
$ mysql.server stop
$ brew services stop redis
```

2. Start Zipkin Server. 
Zipkin is used for distributed tracing system. It is added in this directory (Ramble directory).
However, it is marked in gitignore.

Run Zipkin server (it will start runnin in port 9411):
```
$ java -jar zipkin-server-2.23.4-exec.jar
```

3. Start Maildev.
Maildev is used Local email client used for sending email when registering user. Ref: https://www.npmjs.com/package/maildev

To run maildev, use command:
```
$ maildev
```

4. Start the services in following order:

- service-registry
- cloud-config-server
- Api Gateway
- Application services (user-identity-service & messaging-service)
- Hystrix Dashboard