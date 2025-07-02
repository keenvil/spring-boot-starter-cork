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
* [Maven 3.9.6](http://maven.apache.org)
* [Java - 21](http://java.com)
* [MySQL - 5.7.17](http://www.mysql.com/)

## Installation for developmet

### Getting the code
```
$ git clone https://github.com/keenvil/commons.git
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

@EnableMultitenancy(basePackages = "com.keenvil.guard.domain")
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


