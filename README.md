### Start Ramble locally

#### Start (stop) MySql & Redis

```
$ mysql.server start
$ brew services start redis

$ mysql.server stop
$ brew services stop redis
```

#### Start Zipkin Server. 
Zipkin is used for distributed tracing system. It is added in this directory (Ramble directory).
However, it is marked in gitignore.

Run Zipkin server (it will start runnin in port 9411):
```
$ java -jar zipkin-server-2.23.4-exec.jar
```

#### Start Maildev.
Maildev is used Local email client used for sending email when registering user. Ref: https://www.npmjs.com/package/maildev

Run maildev (it will start running in port 1080):
```
$ maildev
```

#### Start the services:

- service-registry: Eureka server will run on port 8761.
- cloud-config-server: The applications runs on port 9296.
- Api Gateway: The applications runs on port 9191.
- user-identity-service & messaging-service: These can be accessed via gateway port. Individually they run on port 9001 & 9002 respectively. 
- Hystrix Dashboard: The application runs on port 9295.

#### Known limitation

- Sleuth tracing is not working across the service calls due to coroutines.
