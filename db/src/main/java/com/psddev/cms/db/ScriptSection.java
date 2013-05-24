package com.psddev.cms.db;

@Deprecated
public class ScriptSection extends Section {

    @InternalName("script")
    private String rendererPath;

    public String getRendererPath() {
        return rendererPath;
    }

    public void setRendererPath(String rendererPath) {
        this.rendererPath = rendererPath;
    }

    // --- Deprecated ---

    @Deprecated
    @ToolUi.Note("Deprecated. Please leave this blank.")
    private String engine;

    /** @deprecated No replacement and no longer necessary. */
    @Deprecated
    public String getEngine() {
        return engine;
    }

    /** @deprecated No replacement and no longer necessary. */
    @Deprecated
    public void setEngine(String engine) {
        this.engine = engine;
    }

    /** @deprecated Use {@link #getRendererPath} instead. */
    @Deprecated
    public String getScript() {
        return getRendererPath();
    }

    /** @deprecated Use {@link #setRendererPath} instead. */
    @Deprecated
    public void setScript(String script) {
        setRendererPath(script);
    }
}
