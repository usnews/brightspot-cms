---
layout: documentation
title: Configuration
id: configuration
section: documentation
---

## Configuration

There are a number of configuration properties that control Dari at runtime.
Optional values will have a reasonable default value.  These settings can be
configured in `settings.properties` or for a servlet container like Tomcat, in
`context.xml`.

### Debug Tools

To enable debug tools add the following to your web.xml:

    <filter>
        <filter-name>ApplicationFilter</filter-name>
        <filter-class>com.psddev.dari.db.ApplicationFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>ApplicationFilter</filter-name>
        <url-pattern>/*</url-pattern>
        <dispatcher>ERROR</dispatcher>
        <dispatcher>FORWARD</dispatcher>
        <dispatcher>INCLUDE</dispatcher>
        <dispatcher>REQUEST</dispatcher>
    </filter-mapping>

### Database Configuration

All database specific configuration parameters are prefixed with
**`dari/database/{databaseName}/`**.

**Key:** `dari/defaultDatabase` **Type:** `java.lang.String`

> The name of the default database.

**Key:** `dari/database/{databaseName}/class` **Type:** `java.lang.String`

> The classname of a `com.psddev.dari.db.Database` implementation.

**Key:** `dari/database/{databaseName}/readTimeout` **Type:** `java.lang.Double`

> Sets the read timeout for this database.
> *The default is 3 seconds.*

**Key:** `dari/databaseWriteRetryLimit` **Type:** `java.lang.Integer`

> The number of times to retry a transient failure.
> *The default value is 10.*

**Key:** `dari/databaseWriteRetryInitialPause` **Type:** `java.lang.Integer`

> The initial amount of time in milliseconds to wait before retrying a
> transient failure.
> *The default value is 10ms.*

**Key:** `dari/databaseWriteRetryFinalPause` **Type:** `java.lang.Integer`

> The maximum amount of time in milliseconds to wait before retrying a transient
> failure.
> *The default value is 1000ms.*

**Key:** `dari/databaseWriteRetryPauseJitter` **Type:** `java.lang.Double`

> The amount of time to adjust the pause between retries so that multiple threads retrying
> at the same time will stagger. This helps break deadlocks in certain
> databases like MySQL.
> *The default value is 0.5.*

> The pause value is calculated as `initialPause + (finalPause - initialPause) > * i / (limit - 1)`. 
> This is then jittered + or - `pauseJitter` percent.

> For example, if `dari/databaseWriteRetryLimit` is 10, `dari/databaseWriteRetryFinalPause` is 1000ms
> and `dari/databaseWriteRetryPauseJitter` is 0.5 then on the first
> retry Dari will wait between 5ms and 15ms. On the second try Dari will
> wait between 60ms and 180ms continuing until 10th and final try which will wait
> between 500ms and 1500ms.

#### SQL Database Configuration

**Key:** `dari/database/{name}/class` **Type:** `java.lang.String`

> This should be `com.psddev.dari.db.SqlDatabase` for all SQL databases.

**Key:** `dari/isCompressSqlData` **Type:** `java.lang.Boolean`

> Enable or disable compression of Dari object data in the database. Dari uses the Snappy
> compression library for compression. To use this you must include
> Snappy in your pom.xml file as follows:
>
        <dependency>
            <groupId>org.iq80.snappy</groupId>
            <artifactId>snappy</artifactId>
            <version>0.2</version>
        </dependency>

> *The default is false.* We recommend only enabling compression if you
> know your dataset is large (over 50GB).

**Key:** `dari/database/{databaseName}/jdbcUrl` **Type:** `java.lang.String`

**Key:** `dari/database/{databaseName}/readJdbcUrl` **Type:** `java.lang.String` *(Optional)*

> The database jdbc url. All writes will go the database configured by
> `jdbcUrl`. To have reads to go to a slave configure `readJbdcUrl`.

**Key:** `dari/database/{databaseName}/jdbcUser` **Type:** `java.lang.String`

**Key:** `dari/database/{databaseName}/readJdbcUser` **Type:** `java.lang.String` *(Optional)*

> The database user name.

**Key:** `dari/database/{databaseName}/jdbcPassword` **Type:** `java.lang.String`

**Key:** `dari/database/{databaseName}/readJdbcPassword` **Type:** `java.lang.String` *(Optional)*

> The database password.

**Key:** `dari/database/{databaseName}/dataSource` **Type:** `Resource`

**Key:** `dari/database/{databaseName}/readDataSource` **Type:** `Resource` *(Optional)*

> The database resource. All writes will go the database configured by
> `dataSource`. To have reads to go to a slave configure `readDataSource`.

<div class="alert alert-info">
    <strong>Helpful Tip:</strong>
    To use Tomcat connection pooling define a JNDI Resource in
    <code>context.xml</code> with the name
    <code>dari/database/{databaseName}/dataSource</code>.
</div>

#### Aggregate Database Configuration

Aggregate database is an implemention of `com.psddev.dari.db.AbstractDatabase`
provided by Dari that allows objects to be written to and read from multiple
database backends. Typically this is used to reads and writes to both MySQL and
Solr. This allows normal reads to go to MySQL, while full-text search will use
Solr.

**Key:** `dari/database/{databaseName}/defaultDelegate` **Type:** `java.lang.String`

> This is the name of the primary database. It will be written to first and should
> be considered the source of record for all objects. This is usually
> one of the SQL backends.

#### Example Configuration

This is an example configuration that reads from a MySQL slave and writes to a
MySQL master. Solr is configured to read and write to the same host.

    # Aggregate Database Configuration
    dari/defaultDatabase = production
    dari/database/production/defaultDelegate = sql
    dari/database/production/class = com.psddev.dari.db.AggregateDatabase
    dari/database/production/delegate/sql/class = com.psddev.dari.db.SqlDatabase

    # Master Configuration
    dari/database/production/delegate/sql/jdbcUser = username
    dari/database/production/delegate/sql/jdbcPassword = password
    dari/database/production/delegate/sql/jdbcUrl = jdbc:msyql://master.mycompany.com:3306/dari

    # Slave Configuration
    dari/database/production/delegate/sql/readJdbcUser = username
    dari/database/production/delegate/sql/readJdbcPassword = password
    dari/database/production/delegate/sql/readJdbcUrl = jdbc:msyql://slave.mycompany.com:3306/dari

    # Solr Configuration
    dari/database/production/delegate/solr/class = com.psddev.dari.db.SolrDatabase
    dari/database/production/delegate/solr/serverUrl = http://solr.mycompany.com/solr

### Solr Database Configuration

**Key:** `dari/database/{databaseName}/class` **Type:** `java.lang.String`

> This should be `com.psddev.dar.db.SolrDatabase` for Solr databases.

**Key:** `dari/database/{databaseName}/serverUrl` **Type:** `java.lang.String`

> The URL to the master Solr server.

**Key:** `dari/database/{databaseName}/readServerUrl` **Type:** `java.lang.String` *(Optional)*

> The URL to slave Solr server.

**Key:** `dari/database/{databaseName}/commitWithin` **Type:** `java.lang.Integer`

> The maximum amount of time in seconds to wait before committing to Solr.

**Key:** `dari/database/{databaseName}/saveData` **Type:** `java.lang.Boolean`

> Disable saving of Dari record data (JSON Blob) to Solr. Disabling this
> will reduce the size of the Solr index at the cost of extra reads to
> the MySQL database. Only enable this if you have another database
> configured as the primary.

**Key:** `dari/subQueryResolveLimit` **Type:** `java.lang.Integer`

> Since Solr does not currently support joins Dari will execute subqueries
> separately. This limits the size of the results used to prevent
> generating too large of a query.

### Debug Filter Configuration

**Key:** `PRODUCTION` **Type:** `java.lang.Boolean`

> This key enables or disables *production* mode. When production mode
> is enabled a `debugUsername` and `debugPassword` are required to use any
> debug tools.

> This also suppresses JSP error messages in the browser. JSP errors
> will still show up in logs.

> This value defaults to false.

**Key:** `dari/debugUsername` **Type:** `java.lang.String`

> The debug interface user name.

**Key:** `dari/debugPassword` **Type:** `java.lang.String`

> The debug interface password.

**Key:** `dari/debugRealm` **Type:** `java.lang.String`

> The debug interface realm.

### Miscellenous Configuration

**Key:** `dari/cookieSecret` **Type:** `java.lang.String`

> This is used by the `com.psddev.dari.util.JspUtils` class to implement
> secure signed cookies. It should a be reasonably long and random
> string of characters.

**Key:** `dari/isCachingFilterEnabled` **Type:** `java.lang.Boolean`

### Storage Item Configuration

The `com.psddev.dari.util.StorageItem` class provides a mechanism for storing
file-based data without worrying about the underlying storage.  The follow
configuration determines where items are stored.

Multiple storage locations can be configured at a time by namespacing it
like `dari/storage/{storageName}/`.

**Key:** `dari/defaultStorage` **Type:** `java.lang.String`

> The name of the default storage configuration item. This will be used by
> `com.psddev.dari.util.StorageItem.Static.create()`.

#### Local StorageItem

StorageItem implementation that stores files on local disk.

**Key:** `dari/storage/{storageName}/class` **Type:** `java.lang.String`

> This should be `com.psddev.dari.util.LocalStorageItem` for local
> filesystem storage.

**Key:** `dari/storage/{storageName}/rootPath` **Type:** `java.lang.String`

> Path to location to store files on the local filesystem.

**Key:** `dari/storage/{storageName}/baseUrl` **Type:** `java.lang.String`

> URL to the document root defined by `rootPath`.

#### Amazon S3 StorageItem

StorageItem implementation that stores files on Amazon S3.

**Key:** `dari/storage/{storageName}/class` **Type:** `java.lang.String`

> This should be `com.psddev.dari.util.AmazonStorageItem.java` for
> Amazon S3 storage.

**Key:** `dari/storage/{storageName}/access` **Type:** `java.lang.String`

> This is your AWS Access Key ID (a 20-character,
> alphanumeric string). For example: AKIAIOSFODNN7EXAMPLE

**Key:** `dari/storage/{storageName}/secret` **Type:** `java.lang.String`

> This is your AWS Secret Access Key (a 40-character string). For example:
> wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY

**Key:** `dari/storage/{storageName}/bucket` **Type:** `java.lang.String`

> The name of the S3 bucket to store objects in.

**Key:** `dari/storage/{storageName}/baseUrl` **Type:** `java.lang.String`

> URL to the bucket root defined by `bucket`.

#### Brightcove StorageItem

**Key:** `dari/storage/{storageName}/class` **Type:** `java.lang.String`

> This should be `com.psddev.dari.util.BrightcoveStorageItem.java.java` for
> Brightcove video storage.

**Key:** `dari/storage/{storageName}/encoding` **Type:** `java.lang.String`

**Key:** `dari/storage/{storageName}/readServiceUrl` **Type:** `java.lang.String`

**Key:** `dari/storage/{storageName}/writeServiceUrl` **Type:** `java.lang.String`

**Key:** `dari/storage/{storageName}/readToken` **Type:** `java.lang.String`

**Key:** `dari/storage/{storageName}/readUrlToken` **Type:** `java.lang.String`

**Key:** `dari/storage/{storageName}/writeToken` **Type:** `java.lang.String`

**Key:** `dari/storage/{storageName}/previewPlayerKey` **Type:** `java.lang.String`

**Key:** `dari/storage/{storageName}/previewPlayerId` **Type:** `java.lang.String`

