In order to run the `OIDCIdentityTest` locally, follow these setup steps:

1) Start the Keycloak Docker container (on the default port) via:
  > docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:23.0.1 start-dev

2) Login to your local Keycloak instance at: [Keycloak Admin](http://localhost:8080/admin)
3) Create a new Client (more or less) following these steps: https://www.keycloak.org/getting-started/getting-started-docker#_secure_the_first_application
  - For the `Client ID`, enter "test-client"
  - For `Client Authentication`, make sure this is enabled
  - For the `Valid redirect URIs`, make sure you add the OIDC callback URI: `http://localhost:9000/oidc/callback`
  - Get the `Client Secret` from the "Clients" -> "test-client" -> "Credentials" tab
4) Update the `OIDCIdentityTest` configuration ("companion object") `keycloakProvider` settings for `clientSecret`
5) The test should now be ready to run! Simply run the test's `main` method (either with __sbt__ or via IntelliJ Idea) and look for the following:
  > Service started, to begin test, navigate to: {some http link}
6) Open that link with any modern browser