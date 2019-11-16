# Postgres Database Restorer

This library helps you to quickly restore a Postgres database to a previous state.

It was developed as means to speed up integration tests.

## Introduction

In many integration test scenarios the database needs to be setup before it can be used in a test. For example, often migrations need to be run.

When writing isolated tests one would like to have the database in a 'clean' state for each test. This 'clean' state almost never means an 'empty' database, but one that has been setup.

The combination of these factors can lead to increasingly long running integration tests, because the database needs to be setup for each test.

This library may help you to speed up integration tests by providing a means to _quickly_ restore a Postgres database to a previous 'clean' state before each test. 

## How it Works

Postgres has a feature that allows to create a database from a template. Creating databases this way is relatively fast. 

We can create a template of a 'previous' state that we then may 'restore' to, by recreating the database from it.

For this to work one needs to disconnect connections to the database which should be restored.

This library handles the connections, dropping, and recreation from template for you.

**Please be aware that his only works if tests accessing the same database run strictly one after the other.**

## Installation

Add [JitPack](https://jitpack.io/) to your repositories:
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

Add the dependency:

```groovy
dependencies {
    testImplementation 'com.github.ayedo:PostgresDbRestore:v1.0.0'
}
```

## Usage

    val restorer = DatabaseRestorer(
        databaseName = "postgres",
        host = "localhost",
        port = 5432,
        user = "postgres",
        password = "postgres"
    )
    
    restorer.takeSnapshot()
    
    // later call this to restore to the state when you called snapshot
    restorer.restore()

## Example

An example of how you could use this library with jUnit5 can be found [here](https://github.com/ayedo/postgres-db-restore/blob/master/src/main/resources/templates/DatabaseExtension.kt).
