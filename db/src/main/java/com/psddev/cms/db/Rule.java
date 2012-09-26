package com.psddev.cms.db;

import com.psddev.dari.db.Record;

@Record.Embedded
public abstract class Rule extends Record {

    public abstract boolean evaluate(
            Variation variation, Profile profile, Object object);
}
