## Setup Ramble

#### Start (stop) MySql

```
$ mysql.server start

$ mysql.server stop
```

#### Start (stop) Redis

```
$ brew services start redis

$ brew services stop redis
```

#### Start Zipkin Server.

Zipkin is used for distributed tracing system. It is added in this directory (Ramble directory). However, it is marked
in gitignore.

Run Zipkin server (it will start runnin in port 9411):

```
$ java -jar zipkin-server-2.23.4-exec.jar
```

#### Start Maildev.

Maildev is used Local email client used for sending email when registering user.
Ref: https://www.npmjs.com/package/maildev

Run maildev (it will start running in port 1080):

```
$ maildev
```

#### Vault for email-sender-lib

Vault is optionally needed by user-identity-service. Reading from vault can be enabled in application.yml of
user-identity-service. The Vault setting is used for sending email (to confirm registration of account).

Start local Vault-server (Optional):

```
$ vault server --dev --dev-root-token-id="00000000-0000-0000-0000-000000000000"
```

If Vault is enabled, then following steps are needed for local setup:

* Go to vault and 'enable new engine' -> Chose KV -> In Method-option, chose 'Version 1' from drop down -> Enable engine
* Add key-value secret: kv/ramble.email-sender (secrets are username and password).

#### Start the services:

* service-registry: Eureka server will run on port 8761.
* cloud-config-server: The applications runs on port 9296.
* Api Gateway: The applications runs on port 9191.
* user-identity-service & messaging-service: These can be accessed via gateway port. Individually they run on port 9001
  & 9002 respectively.
* Hystrix Dashboard: The application runs on port 9295.

#### Known limitation

- Sleuth tracing is not working across the service calls due to coroutines.
