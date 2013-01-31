package com.psddev.cms.db;

import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.State;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ContentSection extends ScriptSection {

    private Content content;

    /** Returns the content. */
    public Object getContent() {
        return content;
    }

    /** Sets the content. */
    public void setContent(Object content) {
        this.content = (Content) content;
    }

    @Override
    public Map<String, Object> toDefinition() {
        Map<String, Object> definition = super.toDefinition();
        Object content = getContent();

        if (content != null) {
            State contentState = State.getInstance(content);
            ObjectType contentType = contentState.getType();

            definition.put("content", contentState.getId().toString());
            definition.put("contentLabel", contentState.getLabel());

            if (contentType != null) {
                definition.put("contentTypeLabel", contentType.getLabel());
            }
        }

        return definition;
    }

    // --- Deprecated ---

    /** No replacement. */
    @Deprecated
    @ToolUi.Note("Deprecated. Please leave this blank.")
    private Set<ObjectType> contentTypes;

    /** @deprecated No replacement. */
    @Deprecated
    public Set<ObjectType> getContentTypes() {
        if (contentTypes == null) {
            contentTypes = new TreeSet<ObjectType>();
        }
        return contentTypes;
    }

    /** @deprecated No replacement. */
    @Deprecated
    public void setContentTypes(Set<ObjectType> types) {
        this.contentTypes = types;
    }
}
