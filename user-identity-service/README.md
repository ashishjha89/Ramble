#### How to do clean install?
```
mvn --settings ramble_maven_settings.xml clean install
```

#### How to build all modules?
```
mvn --settings ramble_maven_settings.xml package
```

#### How to run service?
```
mvn --settings ramble_maven_settings.xml spring-boot:run
```

#### Local email client used for testing
Maildev: https://www.npmjs.com/package/maildev

To run maindev, use command: 
```
$maildev
```
Maildev web app runs on port 1080 (web ui). The SMTP server runs on port 1025.

#### Start vault server process
```
vault server --dev --dev-root-token-id="00000000-0000-0000-0000-000000000000"
```
Other steps which are needed for Vault-setup:
- Go to vault and 'enable new engine'
- Choose KV
- In Method-option, chose 'V1' from drop down
- Enable engine
- Add key-value secret: kv/ramble.email-sender (secrets are username and password).

#### Next steps
* OAuth 2
* Logout.
* Swagger docs.
* SQL support for tokens & user-info.
* Redis support for storing invalid/exposed tokens.
* Unit & integration tests.