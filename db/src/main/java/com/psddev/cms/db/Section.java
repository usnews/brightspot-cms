package com.psddev.cms.db;

import java.util.Map;

public abstract class Section extends Content {

    private String name;

    @Indexed
    @InternalName("isShareable")
    private boolean shareable;

    private long cacheDuration;

    /** Returns the name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns {@code true} if this section is shareable across multiple
     * pages.
     */
    public boolean isShareable() {
        return shareable;
    }

    /** Sets whether this section is shareable across multiple pages. */
    public void setShareable(boolean shareable) {
        this.shareable = shareable;
    }

    public long getCacheDuration() {
        return cacheDuration;
    }

    public void setCacheDuration(long cacheDuration) {
        this.cacheDuration = cacheDuration;
    }

    public Map<String, Object> toDefinition() {
        Map<String, Object> definition = getState().getSimpleValues();
        definition.put("_type", getClass().getName());
        return definition;
    }

    // --- Deprecated ---

    /** No replacement. */
    @Deprecated
    @ToolUi.Hidden
    private boolean isCacheable;
}
