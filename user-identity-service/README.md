#### Features available
* Registration & Login
    - Username + password
* Authentication & Authorization
    - Access-token, refresh-token and registration-token (using jwt)
* Get User-Info (/me api)

#### Local email client used for testing
Maildev: https://www.npmjs.com/package/maildev

To run maildev, use command: 
```
$ maildev
```
Maildev web app runs on port 1080 (web ui). The SMTP server runs on port 1025.

#### Vault for email-sender-lib
For this POC with maildev email client, Vault is not needed to be enabled.

However, email-sender credential fetching from vault can still be enabled in application.yml.

If Vault is enabled, then following steps are needed for local setup:

#### Local Vault-server
Start Vault-server:
```
$ vault server --dev --dev-root-token-id="00000000-0000-0000-0000-000000000000"
```
Other steps which are needed for Vault-setup:
- Go to vault and 'enable new engine' -> Chose KV -> In Method-option, chose 'Version 1' from drop down -> Enable engine
- Add key-value secret: kv/ramble.email-sender (secrets are username and password).

#### Next steps
* Refresh token
    - Unit test for AuthTokenService, AuthTokenRepo
* Logout
* Swagger docs
* SQL support for tokens & user-info
* Redis support for storing invalid/exposed tokens
* Integration tests
* Email validation
* Cloud Vault: for DB keys, signing keys, email-credentials.