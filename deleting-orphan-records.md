---
layout: default
title: Deleting Orphan Records
id: deleting-orphan-records
---


If an object class is removed from the CMS, and instances of that object remain, showing as `Unknown Type` a query can be run at `/_debug/code` to manually delete.

    Record some = (Record) Query.from(Object.class).where("id = ENTER_ID_HERE").first();
    some.delete();
    return some;

Note, disable "Live Result" before using the above query and hit 'Run' to execute the delete.

## Removing Objects from Trash

Trash trash = Query...
State.getInstance(trash.getObject()).save();