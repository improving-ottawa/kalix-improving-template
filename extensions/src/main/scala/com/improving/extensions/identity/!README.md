# The Identity Extension Module

## Terms / Definitions

**User** - A person or agent (e.g. a service account) which accesses an application to perform some action(s). Note that
in most cases, the term _user_ and _identity_ are used interchangeably, but that strictly speaking, a _user_ is a specific 
person or agent, while an _identity_ is who they are within the context of some application or system. 

**Identity** - An instance of a *user* within the context of an application or system with some specific set of 
properties/attributes. Note that a single user can have multiple identities (e.g. a single person with multiple GMail 
accounts).

**Credentials** - Something that a user has or knows which can be used to authenticate themself as a particular identity.
That is, something a user has or knows which can prove they are who (identity) they claim to be.

**RBAC** - Roles Based Access Control. See this [Link](https://en.wikipedia.org/wiki/Role-based_access_control) for more information.

**OpenID** - Open Identity, as per the OpenID foundation. See this [Link](https://en.wikipedia.org/wiki/OpenID) for more information.

**OIDC** - OpenID Connect, built on top of OAuth2, as per the OpenID foundation's specification.
See this [Link](https://openid.net/specs/openid-connect-core-1_0.html) for the full specification.

**IAM** - Identity and Access Management. See this [Link](https://en.wikipedia.org/wiki/Identity_management) for more information.

**JWT** - Json Web Token, a security apparatus for securing user sessions for an application. (Typically) includes identity,
roles, and permissions. See this [Link](https://en.wikipedia.org/wiki/JSON_Web_Token) for more information.

## About this Module

This module provides reusable components for implementing IAM via two different types of credentials: OIDC/OAuth2 and Passwords.

It can be integrated/used directly in a Kalix service or in any Scala (_maybe_ Java, but no guarantees) application.

Further this module is self-contained, meaning it can be used with or without any of the other `extensions` modules in
this project.

## Credentials and Identity Features

This module provides two types of credentials:
  * **Password** (`CredentialType.Password` in code): A secure OWASP compliant representation of user credentials which
    permit authentication via a password. **Note**: ONLY USABLE in cases where [TLS](https://en.wikipedia.org/wiki/Transport_Layer_Security)
    is available between server and client/UI!

  * **OIDC** (`CredentailType.OIDC` in code): A secure mechanism that defers user authentication to a trusted third-party.
    This is sometimes also called "social media login", but this is the underlying tech behind that. This credential type
    relies on an explicit trust between the third-party identity provider (the _OIDC provider_) and the application. Note
    that in addition to authentication, _identity_ information is provided/returned by the specific _OIDC provider_.

This also module provides three types of identity:
  * **Local Identity** (`LocalIdentity` in code): An identity created by a user directly within an application. When using
    this type of identity, the application must collect (i.e. via a user registration process/screen) the requisite
    properties/attributes for the identity from a user. <p></p>
    _Note_: you are free to implement your own local identity type in case you need additional properties/attributes that
    are not specified in the provided type. The provided type should be a good starting point / baseline and should
    cover most use cases.

  * **OIDC Identity** (`OIDCIdentity` in code): An identity returned/provided by an _OIDC provider_. When using this type
    of identity, keep in mind that the properties/attributes available are limited to the OIDC specification, and you *cannot*
    change/alter/modify this identity. In other words, you get what you get, and some properties/attributes required by
    your application may simply not be available or mandatory according to the _OIDC provider_ you are integrating with.

  * **User Identity** (`UserIdentity` in code): An application specific representation of a particular user's identity,
    being either a `LocalIdentity` (or derivative type) or a `OIDCIdentity`, along with the specific roles (and explicit permissions)
    assigned to that identity. <p></p>
    _Note_: You can technically use any type which extends the `IdentityBase` trait as the identity portion of `UserIdentity`. <p></p>
    _Note_: You **cannot** change/customize this type without breaking this module!

Below is a diagram showing the aforementioned relationships (excluding `IdentityBase`):
```
┌──────────────────┐       ┌─────────────────┐
│                  │       │                 │
│  Local Identity  │       │  OIDC Identity  │
│                  │       │                 │
└────────┬─────────┘       └─────────┬───────┘
         │                           │
         │                           │
         │   ┌─────────────────┐     │
         │   │                 │     │
         └──►│  User Identity  │◄────┘
             │                 │
             └─────────────────┘
                      ▲
                      │
                      │
             ┌────────┴────────┐
             │                 │
             │     Roles &     │
             │   Permissions   │
             │                 │
             └─────────────────┘    
```
_Note_: that **either** `LocalIdentity` (or an `IdentityBase` derivative) **or** `OIDCIdentity` can be plugged into
a `UserIdentity` instance!

This is a current limitation of this module and prevents (for example) having a user
which has both a traditional `username` and `password` type login while also having a `OIDC` login.

A user _could_ however have both an _OIDC_ based identity (with _OAuth2_ authentication) along with a **separate**
`LocalIdentity` with `username` and `password` based authentication. _Note_ that in this case, your application will
(without additional logic/code) treat these two identities as _two distinct users_.

Note that while _identity_ storage is a per-application concern, there is a default Kalix specific implementation available
in the `protobuf/extensions/identity.proto` file which can be used directly in a Kalix entity as state. This is used
in the Kalix demo application/template as well (see the `gateway` module for how-it-is-done details).

### A note on password based credentials

For security reasons, an identity's "_password_" is not stored in the `CredentialType.Password` data structure in plain-text.

For most, it should be obvious why this is a bad idea...

Instead, passwords are salted (with a cryptographically secure random set of bytes) and then hashed via the Argon2-ID algorithm.

See this [Link](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html#argon2id) for information about
why Argon2-ID was chosen as well as the parameters used.

For further security, passwords can also be "peppered" via a MAC algorithm and an application specific private key.

This increases security by introducing an additional piece of entropy (the private key) which is not (**should not be**)
stored along with the salt and hashed password, requiring an attacker to gain access to the underlying credentials data store,
along with the application server(s) in order to attempt any sort of rainbow/dictionary attack.

See this [Link](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html#peppering) for additional
details about "peppering".

Peppering does introduce additional application configuration complexity along with a significant performance decrease in the
application servers however, so it should only be used in environments / applications where security is paramount.

If the current template's configuration is used (e.g. 8-bytes of salt, 18 character minimum password length), most data
breach scenarios where rainbow/dictionary attacks are used will not yield any user passwords, even if peppering is not used.

If the customer/client is highly concerned about security (e.g. a government agency or large bank), peppering can be
enabled/configured for additional resistance to any password hash based attacks.

See `passwords/PepperingSettings` for configuration details and the associated test specifications for usage examples.

## Application Security Features

This module provides straight forward _RBAC_ authorization of user actions, via their `UserIdentity` instance.
Each `UserIdentity` instance is mapped to a _JWT_ after authentication, and that _JWT_ contains the _roles_ assigned
to that user along with any _explicit permissions_ granted to that user.

The user's _JWT_ should be provided either via an HTTP cookie (see `gateway` module for implementation example) or as
an HTTP `Authorization` ("Bearer" token) header with each authorized endpoint request.

The _JWT_ can then be decoded (see: `jwt/JWTUtility` in code) and the user's permissions enumerated from their roles (along 
with any explicitly granted permissions the user has with their identity).

These permissions, along with any other attributes included in the user's issued _JWT_ (again, see `jwt/JWTUtility` in code)
can be used to make authorization (access) decisions for all authorized endpoints.

Keep in mind that for this to work as described above, you must implement a set of roles (groups of permissions) either in
code or in some persistent storage (like a Kalix entity or database table, in the case of dynamic roles). Those roles must
be available via some Application Programming Interface (API), like a trait/class, such that the permissions currently
assigned to each role can be enumerated. Further, any time a new `UserIdentity` is created (either for a user or service account),
the roles for that user must be assigned (based on the context of how/where that identity is being created, e.g. an "Admin user".)

See the `kalix/` package of this module for Kalix specific utilities/helpers/traits/etc. for implementing authorized
Kalix routes. _Note_ that for Kalix, only _Action_ components can have authorization, and that any entities which require
authorized access must be front-ended/proxied by an _Action_. See the `gateway` module for an example implementation.

Updates to an identity's roles and/or explicit permissions must be synchronized with the `UserIdentity` instance, so that
the next time a user authenticates to the application, the roles/permissions can be correctly stamped into their _JWT_.

### A note on JWTs and Application security

While an attackers ability to forge/alter a JWT is effectively zero (defaults use ECDSA 256-bit encryption, which has no known
viable attack vectors and is used in the financial and military sectors), this does not mean that using JWT's alone will
protect your application from various attacks.

See this [Link](https://medium.com/@dilarauluturhan/javascript-xss-cross-site-scripting-and-csrf-cross-site-request-forgery-6f0f4baa2fb1)
for information about both attack mechanisms.

For this reason, the Kalix demo app utilizes a technique where two pieces of information are required for **every** _authorized_ 
endpoint request from the browser to the backend. One of these being the JWT, and another being a (CSRF) token.

  * `JWT` is stored in an HTTP only cookie, and never exposed to the application's Javascript (prevents XSS)
  * `CSRF-Token` is stored in secure browser storage and sent in a special HTTP header with every request

The JWT (which again, is extremely tamper resistant) is stamped with a (_**hash** of the_) CSRF token, and both the HTTP header
(containing the CSRF token) along with the cookie (containing the JWT) are validated (and the CSRF token values compared)
for every endpoint request (GET as well as POST/PUT).

An XSS attack could be able to break the `CSRF-Token` data out of the user's browser secure storage, but the attacker would
not have the (HTTP only) cookie containing the JWT.

A CSRF attack could forge a request to the application server with a valid cookie containing the JWT, but it would not have
access to the correct (hash of the) CSRF token.

A complex combined attack involving both XSS and CSRF _could_ in theory be implemented to gain access to both the CSRF token
and use it along with a hijacked browser to make requests to your application, and there is no (direct) available prevention
against this unfortunately, as this is basically the same information/access the UI uses to make valid endpoint requests.

However - the CORS (Cross-Origin Resource Sharing) policy of your application
(see this [Link](https://docs.kalix.io/operations/invoke-service.html#_enabling_cors) for Kalix), along with simple expirations
of JWT's (part of the JWT specification) are used to mitigate this type of attack (cross-origin, same-site).

Note that in the above attack scenario, the attacker would have to hijack the victim's browser in order to complete the
attack (because of the HTTP only cookie, which they never gain direct access to), as well as work out the required HTTP header
for the CSRF token, then make an *additional* CSRF attack on the same browser to your application in order
to perform whatever malicious actions.

This will typically not be possible in reasonable / typical JWT expiration timeframes (of a couple of hours).

As such, this is the suggested (and implemented) method of handling authorization in a Kalix application, although it
does require additional work on the UI/client side. See the `ui` module for an example implementation of the client-side
code.
