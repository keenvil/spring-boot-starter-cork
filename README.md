# Keenvil Cork
Cork is intended to encapsulate core Keenvil code. At this moment it offers support for:
* Authentication/Authorization,
* Multitenancy,
* Jason Web Token (JWT),
* Common Error handling,
* i18n support,
* Common code like validators and converters.

## Prerequisites - Development

You will need the following things properly installed on your computer.

* [Git](http://git-scm.com/)
* [Maven 3.9+](http://maven.apache.org)
* Java 21 (Temurin)
* Docker, only for the tests that use Testcontainers

## Installation for developmet

### Getting the code
```
$ git clone https://github.com/keenvil/spring-boot-starter-cork.git
```

### Building the code
```
$ mvn clean install
```
## Usage
### Configuration
In your Keenvil module pom file include:
```
    <dependency>
      <groupId>com.keenvil</groupId>
      <artifactId>spring-boot-starter-cork</artifactId>
      <version>${keenvil-cork-starter.version}</version>
    </dependency>
```

## Cork Multitenancy
Cork has support for multitency, that is, having Multiple Tenants running in only one module.
At this moment, Cork Multitencya provides only _shared database / separate schema_ as data base approach. This is one shared data base with one separate schema for each tenant.

### Multitenancy Usage and Configuration

#### Usage
Cork Multitenancy provides a `@EnableMultitenancy` annotation to enable it. This annotation has a required `basePackages` option to configure module base packages entities to be scanned in order to support data base multiple schemas.

```java

@EnableMultitenancy(basePackages = "com.keenvil.guard.entity")
public class GuardApiConfiguration
    extends KeenvilWebSecurityConfigurerAdapter {
}
```

#### Configuration
```
keenvil:
  cork:
    multitenancy:
      tenants-strategy: SHARED-DB-SEPARATE-SCHEMAS
      tenants:
        -
          name: primary
          type: com.zaxxer.hikari.HikariDataSource
          url: jdbc:h2:mem:guard-primary;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;INIT=RUNSCRIPT FROM 'src/main/resources/h2-schema.ddl'
          username: sa
          password:
          driver-class-name: org.h2.Driver
          default: true
          dataSourceProperties:
            connection-test-query: SELECT * FROM individuals
            minimum-idle: 1
            maximum-pool-size: 10
            pool-name: PrimaryHikariCP
            connectionTimeout: 1000
```

* `multitenancy.tenants-strategy` (String, mandatory, _SHARED-DB-SEPARATE-SCHEMAS_), Data Base strategy for multiple tenants. *This property fires Multitenancy Auto Configuration* 
* `multitenancy.tenants` (list, mandatory), list of Tenants with its information,
    * `name` (string, mandatory), tenant name which MUST match Community id value. 
    * `default` (boolean, mandatory) whether this is the default tenant or not. Only one default tenant MUST be defined.
    * `url` (string, mandatory), data base URL,
    * `username` (string, mandatory), data base username,
    * `password` (string, mandatory), data base password,
    * `driver` (string, mandatory), data base classs driver,
    *  `datasourceProperties` (properties, optional), extra data base configuration properties.
        * `connection-test-query` (string),
        * `minimum-idle` (numeric),
        * `maximum-pool-size` (numeric),
        * `pool-name` (string),
        * `connectionTimeout` (string)

## Package layout

Cork is a library: its public packages and classes are its contract with every Keenvil service. It is organized by
topic, not by layer (the layered standard applies to services).

| Package | What it holds |
|---|---|
| `com.keenvil.cork` | `CommunityIdentifierResolver` (interface used by the services), auto-configuration and `keenvil.cork.*` properties |
| `consul` | Tenant data sources and configuration read from Consul; per-tenant pool settings (`keenvil.multitenancy.pool.*`) |
| `multitenancy` | `@EnableMultitenancy`, tenant resolution (`UrlPathVariableCommunityResolver`, `RequestAttributeCommunityResolver`), Liquibase per tenant, task decorator |
| `jwt` | `@EnableJwtSecurity`, filter, `JwtService`, `JwtUser`, `@JwtLoggedUser` |
| `security` | `KeenvilWebSecurityConfigurerAdapter`, role annotations, QR |
| `error` | `KeenvilApiException` and the common controller advice |
| `totp`, `validation`, `converter`, `data`, `date`, `internationalization` | Small shared utilities |
| `mongo` | Opt-in Mongo compatibility (`keenvil.mongo.compat.enabled=true`) |
| `openapi` | Opt-in OpenAPI configuration (`keenvil.openapi.enabled=true`) |

Rules:
* Do not move or rename a public class, a `@ConfigurationProperties` prefix, or a class listed in
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` without a major version.
  Check who imports it first: `rotation-scripts/capas/cork_uso.py` (keenvil workspace) prints the usage per class.
* `src/main/java/com/mongodb/util/JSON*` keeps the package of the legacy Mongo driver on purpose.

## OpenAPI documentation (5.1.0, fixes in 5.1.1)

With `keenvil.openapi.enabled=true` and springdoc on the classpath, cork completes the service document:

* **Info**: `keenvil.openapi.title`, `keenvil.openapi.description` (markdown), `keenvil.openapi.version` (default: the
  build version, then `spring.application.version`, then `unknown`), `keenvil.openapi.contact.*` and `keenvil.openapi.servers[n].url|description`. A common section
  (authentication with `X-Authorization`, error body, pagination, dates) is appended to the description
  (`keenvil.openapi.common-section=false` turns it off).
* **Errors**: the `KeenvilApiError` schema and standard responses on every operation (400 with parameters or body,
  401/403 unless the operation is public with an empty `@SecurityRequirements`, 404 with path variables, 422 with a
  body, 500 always; `keenvil.openapi.standard-errors=false` turns it off). Operation specific errors go with
  `@ApiError(status = 409, code = "amount.changed", description = "...")`, not with swagger's `@ApiResponse`, which
  makes springdoc drop the generated 200.
* **Roles**: the roles of the operation `@PreAuthorize` (direct or through `@HasAdminRoleInCommunity` and the like)
  are appended to the description and listed in `x-roles`.
* **Internal endpoints**: `/internal/**` and `/c/{id}/internal/**` are removed (`keenvil.openapi.hide-internal=false`
  keeps them), together with the tags left without operations and the schemas no longer referenced.
* **Pagination**: a `RequestedPage` query parameter is documented as `page`, `size` and `sort`, without
  `@ParameterObject`.
* **Error examples**: `uri` uses the service context-path (`/amercement/c/1/recurso`).
* **Javadoc**: cork brings `therapi-runtime-javadoc`; a service that adds the processor gets its Javadoc as summary
  and description of operations, parameters and schemas (records included):

  ```xml
  <dependency>
    <groupId>com.github.therapi</groupId>
    <artifactId>therapi-runtime-javadoc-scribe</artifactId>
    <version>0.15.0</version>
    <scope>provided</scope>
  </dependency>
  ```

  On JDK 23+ javac only runs classpath processors with `<proc>full</proc>` in `maven-compiler-plugin`.
  If the service already declares `<annotationProcessorPaths>` (Lombok, MapStruct...), javac ignores the processors
  on the classpath: add the scribe there instead of as a `provided` dependency:

  ```xml
  <annotationProcessorPaths>
    <!-- ...the existing ones... -->
    <path>
      <groupId>com.github.therapi</groupId>
      <artifactId>therapi-runtime-javadoc-scribe</artifactId>
      <version>0.15.0</version>
    </path>
  </annotationProcessorPaths>
  ```
* **Swagger UI**: tags and operations sorted alphabetically, models collapsed, filter and request duration
  (lowest precedence defaults, the service yml overrides them).

The customizers also apply to a service that keeps its own `OpenAPI` bean. `@JwtLoggedUser JwtUser` and
`@LoggedIndividual` parameters are hidden (cork injects them); other server-injected types or annotations go in
`keenvil.openapi.ignored-parameter-types` (fully qualified names). The `/c/{id}` path variable gets a default
description, and "in the community of the URL" is added to the roles only when the path has that `{id}`.

## Releasing

The package is published to GitHub Packages by the `Publish to GitHub Packages` workflow: on every push to `main`,
or manually with `workflow_dispatch`. Bump `<version>` in `pom.xml` in the same change; a version can be published
only once.
