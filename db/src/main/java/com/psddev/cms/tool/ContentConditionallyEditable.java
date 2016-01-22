package com.psddev.cms.tool;

import com.psddev.dari.db.State;

/**
 * Interface for displaying {@link com.psddev.cms.db.Content} edit form as read-only.
 */
public interface ContentConditionallyEditable {

    /**
     * Returns {@code true} if {@link com.psddev.cms.db.Content}
     * edit form should be displayed as read-only.
     */
    public boolean isReadOnly();

    /**
     * Returns {@code true} if the given Object
     * should be displayed as read-only
     * @param object Can't be {@code null}
     */
    public static boolean isReadOnly(Object object) {
        if (object instanceof State) {
            object = ((State) object).getOriginalObject();
        }

        return object instanceof ContentConditionallyEditable
                && ((ContentConditionallyEditable) object).isReadOnly();
    }
}
