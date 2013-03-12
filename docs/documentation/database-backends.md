---
layout: default
title: Database Backends
id: databaseBackends
section: documentation
---
<div markdown="1" class="span12">

Dari is designed in such a way that it can have multiple storage
backends. It can, and usually does, use several of them at once. For
example, a standard configuration is to store data in MySQL as the
source of record and also store data in Solr for full text search
capabilities.

Dari will automatically handle saving to all configured storage backends
when the `save()` method is called on a record.

### Supported databases

* MySQL
* PostgreSQL
* Oracle
* Solr
* MongoDB

### SQL-based Storage Backends

MySQL, PostgreSQL and Oracle are implemented using the same underlying
table structure and indexing strategy.

This storage backend is implemented using seven tables. The primary table,
`Record`, stores the raw information about the objects that are saved. Objects
are serialized to JSON format when they are saved and stored in the `data`
column of the `Record` table. The rest of the tables are used to implement
field indexes.

Dari creates unique ids for every object by generating a UUID. The `id` and
`typeId` fields in the `Record` table store these UUIDs in the most appropriate
datatype for the underlying database. For MySQL and Oracle this is `binary(16)`. On
PostgreSQL the native `UUID` is used.

#### Creating a SQL Database

Due to the subtle differences between database vendors MySQL, PostgreSQL and
Oracle each have their own DDL that is used to setup a Dari database. They can
be found in the following locations:

**MySQL**:
[etc/mysql/schema-11.sql](https://github.com/perfectsense/dari/blob/master/etc/mysql/schema-11.sql)

**PostgreSQL**:
[etc/postgress/schema-11.sql](https://github.com/perfectsense/dari/blob/master/etc/postgres/schema-11.sql)

**Oracle**:
[etc/oracle/1-dari.sql](https://github.com/perfectsense/dari/blob/master/etc/oracle/1-dari.sql),
[etc/oracle/2-schema.sql](https://github.com/perfectsense/dari/blob/master/etc/oracle/2-schema.sql),
[etc/oracle/3-grants.sql](https://github.com/perfectsense/dari/blob/master/etc/oracle/3-grants.sql)

#### Indexing

Since records are stored as a JSON text blob Dari cannot use traditional SQL
indexes. To implement indexing Dari uses several additional tables to store
indexes.

There are four primary index tables: `RecordString`, `RecordNumber`,
`RecordUuid` and `RecordLocation2`.

When fields of an object are indexed the field's value along with its object id 
will be stored in the appropriate index table.

#### Tables

`Record`

&nbsp;&nbsp;&nbsp;&nbsp;This table is the primary storage table. All objects
are stored in this table as serialized JSON blobs.

`RecordLocation2`

&nbsp;&nbsp;&nbsp;&nbsp;This table stores spatial indexes.
Supported on MySQL and PostgreSQL only.

`RecordNumber2`

&nbsp;&nbsp;&nbsp;&nbsp;This table stores number and timestamp indexes.

`RecordString3`

&nbsp;&nbsp;&nbsp;&nbsp;This table stores string and enumeration
indexes.

`RecordUpdate`

&nbsp;&nbsp;&nbsp;&nbsp;This table tracks when object were last updated.

`RecordUuid2`

&nbsp;&nbsp;&nbsp;&nbsp;This table stores relationship indexes.

`Symbol`

&nbsp;&nbsp;&nbsp;&nbsp;This table stores symbols such as index names.
It is referenced by the other index tables.
