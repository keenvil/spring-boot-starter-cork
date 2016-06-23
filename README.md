# My Community Commons
This README outlines the details of collaborating on
Commons artifacts.

Commons artifact is intended to be used by APIs (security, responses templates,
etc.)
## Prerequisites - Development

You will need the following things properly installed on your computer.

* [Git](http://git-scm.com/)
* [Maven](http://maven.apache.org)
* [Java](http://java.com)
* [MySQL](http://www.mysql.com/)

## Installation

* You must be logge into the my-community VPN,
* `git clone https://github.com/my-community/commons.git` this repository
* change into the new directory: `cd commons`
* `vim ~/.m2/settting.xml` and paste the following:
```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                          https://maven.apache.org/xsd/settings-1.0.0.xsd">

  <servers>
    <server>
      <id>nexus-snapshots</id>
      <username>admin</username>
      <password>tC3"XT1Q1o5}G2Q=D9M8Z1v7|kdmT9</password>
    </server>
  </servers>

</settings>
```
* `mvn clean install`

## Building

* mvn clean install

## Deploying artifacts to Nexus

* mvn deploy

### CI Configuration

Checkout Dockerfile

## Development

### Naming conventions
* TBD: add checkstyle
