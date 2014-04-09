package com.psddev.cms.db;

import com.psddev.dari.db.State;

import java.util.Map;

/**
 * @deprecated No replacement.
 */
@Deprecated
@Operation.DisplayName("Use Embedded Variation Data")
public class EmbeddedDataOperation extends Operation {

    @Override
    public void evaluate(
            Variation variation, Profile profile, Object object) {
        State state = State.getInstance(object);
        Map<String, Object> variationData = (Map<String, Object>)
                state.getValue("variations/" + variation.getId());
        if (variationData != null) {
            state.getValues().putAll(variationData);
        }
    }
}
