package com.psddev.cms.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ContainerSection extends ScriptSection {

    @Embedded
    @ToolUi.Hidden
    private List<Section> children;

    /** Returns the children. */
    public List<Section> getChildren() {
        if (children == null) {
            children = new ArrayList<Section>();
        }
        return children;
    }

    /** Sets the children. */
    public void setChildren(List<Section> children) {
        this.children = children;
    }

    @Override
    public Map<String, Object> toDefinition() {
        Map<String, Object> definition = super.toDefinition();
        List<Map<String, Object>> childMaps = new ArrayList<Map<String, Object>>();

        definition.put("children", childMaps);

        for (Section child : getChildren()) {
            if (child != null) {
                childMaps.add(child.toDefinition());
            }
        }

        return definition;
    }

    // --- Deprecated ---

    @Deprecated
    @ToolUi.Note("Deprecated. Please leave this blank.")
    private String beginEngine;

    @Deprecated
    @ToolUi.Note("Deprecated. Please use Script instead.")
    private String beginScript;

    @Deprecated
    @ToolUi.Note("Deprecated. Please leave this blank.")
    private String endEngine;

    @Deprecated
    @ToolUi.Note("Deprecated. Please use Script instead.")
    private String endScript;

    /** @deprecated No replacement and no longer necessary. */
    @Deprecated
    public String getBeginEngine() {
        return beginEngine;
    }

    /** @deprecated No replacement and no longer necessary. */
    @Deprecated
    public void setBeginEngine(String beginEngine) {
        this.beginEngine = beginEngine;
    }

    /** @deprecated Use {@link #getScript} instead. */
    @Deprecated
    public String getBeginScript() {
        return beginScript;
    }

    /** @deprecated Use {@link #setScript} instead. */
    @Deprecated
    public void setBeginScript(String beginScript) {
        this.beginScript = beginScript;
    }

    /** @deprecated No replacement and no longer necessary. */
    @Deprecated
    public String getEndEngine() {
        return endEngine;
    }

    /** @deprecated No replacement and no longer necessary. */
    @Deprecated
    public void setEndEngine(String endEngine) {
        this.endEngine = endEngine;
    }

    /** @deprecated Use {@link #getScript} instead. */
    @Deprecated
    public String getEndScript() {
        return endScript;
    }

    /** @deprecated Use {@link #setScript} instead. */
    @Deprecated
    public void setEndScript(String endScript) {
        this.endScript = endScript;
    }
}
