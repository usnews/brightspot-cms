package com.psddev.cms.db;

import com.psddev.dari.util.HtmlWriter;

import java.io.IOException;
import java.util.Map;

@Deprecated
public abstract class Section extends Content {

    @InternalName("name")
    private String displayName;

    @Indexed(unique = true)
    private String internalName;

    @Indexed
    @InternalName("isShareable")
    private boolean shareable;

    private long cacheDuration;

    /** Returns the display name. */
    public String getDisplayName() {
        return displayName;
    }

    /** Sets the display name. */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** Returns the internal name. */
    public String getInternalName() {
        return internalName;
    }

    /** Sets the internal name. */
    public void setInternalName(String internalName) {
        this.internalName = internalName;
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

    public void writeLayoutPreview(HtmlWriter writer) throws IOException {
    }

    // --- Deprecated ---

    /** @deprecated No replacement. */
    @Deprecated
    @ToolUi.Hidden
    private boolean isCacheable;

    /** @deprecated Use {@link #getDisplayName} instead. */
    @Deprecated
    public String getName() {
        return getDisplayName();
    }

    /** @deprecated Use {@link #setDisplayName} instead. */
    @Deprecated
    public void setName(String name) {
        setDisplayName(name);
    }
}
