package com.psddev.cms.db;

import com.psddev.dari.db.Record;

@Record.Embedded
public abstract class Operation extends Record {

    public abstract void evaluate(
            Variation variation, Profile profile, Object object);
}
