# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

A collection of standalone Infinispan tutorials organized as a multi-module Maven project. Each tutorial is an independent module demonstrating a specific Infinispan feature. Current version: 16.2.0-SNAPSHOT, targeting Java 17+.

## Build Commands

```bash
# Build all tutorials
./mvnw clean package

# Build and run tests
./mvnw clean install

# Build a single tutorial
./mvnw clean package -pl infinispan-remote/cache

# Run a single tutorial's tests
./mvnw test -pl infinispan-remote/cache

# Check code formatting
./mvnw spotless:check

# Fix code formatting
./mvnw spotless:apply

# Build guides documentation (requires -Pguides profile)
./mvnw -Pguides -pl docs-maven-plugin package -DskipTests

# Deploy guides locally to infinispan.github.io checkout
./deploy-guides-local.sh [tutorials-dir] [website-dir]
```

## Code Formatting

Enforced by Spotless Maven plugin during `verify` phase:
- **Java**: 3-space indentation, import order: static, java, javax, org, com, then everything else
- **XML**: 4-space indentation
- **YAML**: 2-space indentation

See `.editorconfig` for full details.

## Architecture

### Module Categories

- **`infinispan-remote/`** — Tutorials using Hot Rod client to connect to an Infinispan Server (client-server mode)
- **`infinispan-embedded/`** — Tutorials embedding Infinispan as a library (no server needed)
- **`integrations/`** — Tutorials for Spring Boot, Quarkus, and Hibernate integrations
- **`infinispan-ai/`** — AI/vector search tutorials (LangChain4j, vector search)
- **`non-java-clients/`** — C++, C#, JavaScript Hot Rod client examples

### Shared Connection Helper

`infinispan-remote/connect-to-infinispan-server` is a shared module depended on by most remote tutorials. It provides `TutorialsConnectorHelper` which:
1. Tries to connect to a running Infinispan Server at `localhost:11222` (user: `admin`, password: `password`)
2. Falls back to starting a Testcontainers Infinispan instance if no server is found
3. On SNAPSHOT versions, uses `quay.io/infinispan-test/server:main`; on releases, uses the matching release image

### Tutorial Module Pattern

Each tutorial module follows the same structure:
- `src/main/java/` — Runnable main class demonstrating the feature
- `src/test/java/` — JUnit 5 test that exercises the same feature
- `pom.xml` — Inherits from root, declares `exec-maven-plugin` for `mvn exec:exec`
- `guide.adoc` (optional) — AsciiDoc tutorial guide published to infinispan.org

### Documentation

- `documentation/asciidoc/` — AsciiDoc source for published tutorial docs
- `docs-maven-plugin/` — Custom Maven plugin that processes `guide.adoc` files from each tutorial into the website format
- Guides are built with the `guides` Maven profile

## CI

GitHub Actions runs on pushes and PRs to `main`, `development`, and `16.0.x` branches. The CI starts an Infinispan Server container (image varies by branch), then runs `./mvnw -B clean install`.

## Branch Strategy

- `development` — uses latest Infinispan dev version (`quay.io/infinispan-test/server:main`)
- `main` — uses latest stable Infinispan release
- All contributions target `main`
