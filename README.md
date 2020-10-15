# Advocado

Full-stack authentication system set up for a fictive web store called
'Advocado'. It is written in [Clojure][8], and [ClojureScript][9]. This
solution includes among other things:

- Login system authentication system with hashed passwords stored in a PostgreSQL database
- Token based authentication on frontend, stored in `localStorage` for persistence.
- Sign up system, allowing new users to register an account.
- Forgot password system allowing users to reset their password via an email.
- Basic front-end UI using [Material UI](https://material-ui.com/)
- Client side routing, and a backend API skeleton.
- Front-end state management using [re-frame](https://github.com/Day8/re-frame)
- Front-end and backend input validation using [clojure.spec](https://clojure.org/about/spec)

This solution was initially generated using [Luminus][4] version `3.67`

## Prerequisites

- You will need [Leiningen][1] 2.0 or above installed.
- You'll need a [PostgreSQL][2] database
- A [Mailgun][7] account for email dispatching

### Environment variables

This app uses [cprop][5] to manage environment variables, to setup a
development environment create an `dev-config.edn`:

```edn
{:dev true
 :port 3000
 ;; when :nrepl-port is set the application starts the nREPL server on load
 :nrepl-port 7000

 ; set your dev database connection URL here
 :database-url "postgresql://localhost/YOUR_DB?user=YOUR-USER"

 ;; Token signing
 :privkey "auth_privkey.pem"
 :pubkey "auth_pubkey.pem"
 :passphrase "YOUR PASSPHRASE"

 ;; mailgun
 :mailgun-api-key "YOUR MAILGUN API KEY"
 :mailgun-base-url "https://api.mailgun.net/v3/YOUR_DOMAIN_NAME"
 }
```
**Resources**:

- [tolitius/cprop: likes properties, environments, configs, profiles..](https://github.com/tolitius/cprop)
- [Environment Variables | Luminus - a Clojure web framework](https://luminusweb.com/docs/environment.html)

### Database access

Once a PostgreSQL database is setup on your target system, and configured in
either `dev-config.edn`, or what ever other method you prefer [[1][5],[2][6]],
the database can be migrated using this command:

    lein run migrate

This will setup the needed tables and create a test user with the credentials:

    user: ola
    pass: admin

You probably want to change this later, the SQL migration query is located in
`20200813140733-add-admin-user.up.sql`, and to hash a new password you can do this:

    user=> (hashers/derive "PASSWORD" {:alg :pbkdf2+sha256})

**Resources:**

- [Database Access | Luminus - a Clojure web framework](https://luminusweb.com/docs/database.html)
- [Database Migrations | Luminus - a Clojure web framework](https://luminusweb.com/docs/migrations.html)
- [PostgreSQL - ArchWiki](https://wiki.archlinux.org/index.php/PostgreSQL)

### Certificates

For the token you need to generate some certificates. These should be located
in the `./resources` directory, and can be called whatever you want, just make
sure to update your `config.edn` with the filenames and passphrase.

```sh
openssl genrsa -aes128 -out auth_privkey.pem 2048
```
```sh
openssl rsa -pubout -in auth_privkey.pem -out auth_pubkey.pem
```

### Emails

This app uses [Mailgun][7] to dispatch a "reset password" email, you'll need an
account there. Additionally this app uses an email template that is called
`forgot-pass.html`, the content of this template is of little relevance except
for a link that contains this template variable:

```html
<a href="{{url}}">RESET YOUR PASSWORD</a>
```

When dispatching an email this app attaches `X-Mailgun-Variables` to the
request parameters, which is the tokenized url to reset a password. See
`email.clj` in the `src` directory.

All this can of course be configured, refer to the mailgun documentation for more.

- [User Manual — Mailgun API documentation](https://documentation.mailgun.com/en/latest/user_manual.html#templates)

## Developing

Once everything is setup you can start a development environment like this:

    lein repl
    user=> (start)
    user=> (start-fw)

Open <http://localhost:3000>

This starts the app and starts [figwheel][12] that sets up hot reloading in front-end.

## Running

To start a web server for the application, run:

    lein run

## Deployment

See [luminus deployment](https://luminusweb.com/docs/deployment.html) for various deployment options.

## License

Copyright © 2020 Daniel Berg

Distributed under the [MIT License](http://opensource.org/licenses/MIT).

[1]: https://github.com/technomancy/leiningen
[2]: https://www.postgresql.org/
[3]: https://wiki.archlinux.org/index.php/PostgreSQL
[4]: https://luminusweb.com/
[5]: https://github.com/tolitius/cprop
[6]: https://luminusweb.com/docs/environment.html
[7]: https://www.mailgun.com/
[8]: https://clojure.org/
[9]: https://clojurescript.org/
[12]: https://github.com/bhauman/lein-figwheel

