# sbt-kalix-improving

sbt and typescript based multiproject as a template for frontend with microservices backend

## Design

Domain documentation that will (eventually) generate code for services

## Common

Includes common objects used for creating protobuf messages

## Utils

A library for common Scala functions

## Services

This template includes 2 Kalix services - service1 & service2 - to be customized as desired to create microservices

## Gateway

The Kalix Gateway will serve as the interface between the client & the microservices
The Gateway contains integration tests for all services

## UI

The front-end client PWA (typescript)

## Pre-Build Dependency Installation

# Protoc-gen-js

```
npm install --global protoc-gen-js
```

For more info, [see link](https://github.com/yinzara/protoc-gen-js)

# GRPC-Web Plugin Generator

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


