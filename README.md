[![Code Build](https://github.com/improving-ottawa/kalix-improving-template/actions/workflows/ci.yml/badge.svg)](https://github.com/improving-ottawa/kalix-improving-template/actions/workflows/ci.yml/badge.svg)
[![Riddl Model Build](https://github.com/improving-ottawa/kalix-improving-template/actions/workflows/riddl.yml/badge.svg)](https://github.com/improving-ottawa/kalix-improving-template/actions/workflows/riddl.yml)
[![Coverage Status](https://coveralls.io/repos/github/improving-ottawa/kalix-improving-template/badge.svg?branch=main)](https://coveralls.io/github/improving-ottawa/kalix-improving-template?branch=main)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/reactific/riddl/master/LICENSE)

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-black.svg)](https://sonarcloud.io/summary/new_code?id=improving-ottawa_kalix-improving-template)

[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=improving-ottawa_kalix-improving-template&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=improving-ottawa_kalix-improving-template)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=improving-ottawa_kalix-improving-template&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=improving-ottawa_kalix-improving-template)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=improving-ottawa_kalix-improving-template&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=improving-ottawa_kalix-improving-template)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=improving-ottawa_kalix-improving-template&metric=bugs)](https://sonarcloud.io/summary/new_code?id=improving-ottawa_kalix-improving-template)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=improving-ottawa_kalix-improving-template&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=improving-ottawa_kalix-improving-template)
[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=improving-ottawa_kalix-improving-template)](https://sonarcloud.io/summary/new_code?id=improving-ottawa_kalix-improving-template)

# sbt-kalix-improving

- [Submodules](#submodules)
    - [Design](#design)
    - [Common](#common)
    - [Utils](#utils)
    - [Services](#services)
    - [Gateway](#gateway)
    - [UI](#ui)
    - [Extensions](#ext)
    - [Integration Tests](#integration-tests)
    - [ScheduledTasks](#scheduled)
- [Pre-Installation](#pre-install)
    - [protoc-gen-js](#protoc-gen)
    - [grpc-web](#grpc-web)
- [Backend](#backend)
    - [Running Locally](#be-local-run)
    - [Testing Locally](#be-local-test)
    - [Deploying to Kalix](#be-deploy)
- [Frontent/UI](#frontend)
    - [Running Locally](#fe-local)
    - [With Local Keycloak](#fe-keycloak)

# <a id="submodules"></a>  Submodules

sbt (Kalix) and typescript based multiproject as a template for frontend with microservices backend

## <a id="design"></a> Design

Domain documentation that will (eventually) generate code for services

## <a id="common"></a> Common

Includes common objects used for creating protobuf messages

## <a id="utils"></a> Utils

A library for common Scala functions & service extensions (Auth)

## <a id="services"></a> Services

This template includes 2 Kalix services to be customized as desired to create microservices
The template offers example of how to create service types, either by deploying together (bounded-context) or as a
separate deployable (service3)

- `bounded-context`
    - Service 1 (Action w/ Event Sourced Entity that is consumed by NoDataView)
    - Service 2 (just an Action)
- Service 3 (Value Entity w/ View)

## <a id="gateway"></a>  Gateway

The Kalix Gateway will serve as the interface between the client & the microservices
The Gateway contains integration tests for all services

# <a id="ui"></a> UI

The front-end client PWA (typescript)

## <a id="ext"></a>  Extensions

Email & OIDC/JWK capabilities

## <a id="integation-tests"></a>  Integration Tests

`integration-testkit` contains extensions to the Kalix testkit that allow for testing a multi-deployment system
`integration-testkit-tests` contains the tests themselves

## <a id="scheduled"></a>  Scheduled Tasks

Includes `ScheduledTaskAction`, a trait implementing `Start`, `Run`, and `RunForTest`, which allows for enabling
scheduled actions
In the example, every 2 hours,  `DoNothingService1` implements a `DoNothingAction` call to `Service1` and then reads
from `NoDataView`

# <a id="pre-install"></a> Pre-Build Dependency Installation

## <a id="protoc-gen"></a> Protoc-gen-js

```
npm install --global protoc-gen-js
```

For more info, [see link](https://github.com/yinzara/protoc-gen-js)

### <a id="grpc-web"></a> GRPC-Web Plugin Generator

After
downloading [an appropriate image](https://github.com/grpc/grpc-web#code-generator-plugin:~:text=plugin%20from%20our-,release,-page%3A),
execute

```
$ sudo mv ~/Downloads/protoc-gen-grpc-web-1.5.0-darwin-x86_64 \
/usr/local/bin/protoc-gen-grpc-web
$ chmod +x /usr/local/bin/protoc-gen-grpc-web
```

for Mac or a similar command for Lunix.

See [grpc-web docs](https://github.com/improving-ottawa/kalix-improving-template/actions/runs/6866116754/job/18671628548)
more thorough instructions

# <a id="backend"></a> Backend

## <a id="be-local-run"></a> Running services locally

To run any of the Kalix services locally, use the following command:

```shell
sbt "{service-name}/runAll -Dlogback.configurationFile=logback-local.xml -Dconfig.resource=integration-test.conf"
```

So for example, to run the `gateway` service locally, run:
> sbt "gateway/runAll -Dlogback.configurationFile=logback-local.xml -Dconfig.resource=integration-test.conf"

## <a id="be-local-test"></a> Local Testing

The integration tests can be run locally via the command `sbt "integration-test/test"`

## <a id="be-deploy"></a> How to publish and deploy the Kalix services to Kalix

In order to publish and deploy the Kalix services to the Kalix cloud, you'll need to perform the following:

1) Setup Kalix auth and Kalix / docker integration locally.

    - Follow the instructions here: https://docs.kalix.io/projects/container-registries.html

2) Create a Kalix auth token and `export` it in your shell environment.

    - Run `kalix projects tokens create --description "Some token description"` after authentication to create the token
    - Export the token via `export KALIX_TOKEN="{the token data}"` (**Note**: the environment variable name must
      match `KALIX_TOKEN` exactly!)

3) Use _sbt_ to build and publish container images for all deployable services.

    - Simply run `sbt publishContainers`

4) Use _sbt_ to deploy the built and published containers

    - Simply run `sbt deployServices`

5) Use the "health-check" to verify that the services are running.

    - Run `grpcurl -d '{}' api.off-the-top.io:443 com.ott.gateway.Gateway/HealthCheck` from your shell if you
      have `grpcurl` installed
    - Alternatively, you can _curl_ or browse: `https://api.off-the-top.io/health-check`

*Note:* there is also a compound command `publishAndDeploy` which will run steps #3 and #4 back to back for you.

# <a id="frontend"></a> Front End

## <a id="fe-local"></a> Running locally

Make sure first to run the script `proto-gen`. Then use the command `npm start` to get the app running in browser.

# <a id="fe-keycloak"></a> Local testing with Keycloak

1) Copy the local configuration template file from `gateway/src/user-local.conf.template`
   to `gateway/src/user-local.conf`
   Don't worry, it is in the `.gitignore`, so your local changes will not be pushed up to the git server!
2) Follow the instructions in `gateway/src/Test-Setup-Instructions.md`
3) Profit!

## Running the (local testing) CORS proxy

Simply run this command:
> sbt "gateway/test:runMain com.example.gateway.LocalCorsProxy"

...after the Gateway and related services are started.