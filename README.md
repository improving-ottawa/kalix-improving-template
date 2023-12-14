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
    - [Extensions](#ext)
    - [Integration Tests](#integration-tests)
    - [ScheduledTasks](#scheduled)

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

## Gateway

The Kalix Gateway will serve as the interface between the client & the microservices
The Gateway contains integration tests for all services

## Scheduled Tasks

Includes `ScheduledTaskAction`, a trait implementing `Start`, `Run`, and `RunForTest`, which allows for enabling
scheduled actions
In the example, every 2 hours,  `DoNothingService1` implements a `DoNothingAction` call to `Service1` and then reads
from `NoDataView`

# UI

The front-end client PWA (typescript)

## Pre-Build Dependency Installation

### Protoc-gen-js

```
npm install --global protoc-gen-js
```

For more info, [see link](https://github.com/yinzara/protoc-gen-js)

### GRPC-Web Plugin Generator

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

# Backend

## Running services locally

To run any of the Kalix services locally, use the following command:

```shell
sbt "{service-name}/runAll -Dlogback.configurationFile=logback-local.xml -Dconfig.resource=integration-test.conf"
```

So for example, to run the `gateway` service locally, run:
> sbt "gateway/runAll -Dlogback.configurationFile=logback-local.xml -Dconfig.resource=integration-test.conf"

# Local Testing

The integration tests can be run locally via the command `sbt "integration-test/test"`

## How to publish and deploy the Kalix services to Kalix

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

# Front End

## Running locally

Make sure first to run the script `proto-gen`. Then use the command `npm start` to get the app running in browser.