#### Local email client used for testing
Maildev: https://www.npmjs.com/package/maildev

To run maindev, use command: 
```
$maildev
```
Maildev web app runs on port 1080 (web ui). The SMTP server runs on port 1025.

#### Vault for email-sender-lib
Email-sender credential fetching logic from vault can be enabled by application.yml.

If Vault is enabled, then following steps are needed for local setup:

Start Vault-server:
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
* Email validation