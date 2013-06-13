package com.psddev.cms.db;

import java.io.IOException;
import java.util.UUID;

import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;

/**
 * Action taken by a user in the tool.
 */
@Record.Embedded
public abstract class ToolUserAction extends Record {

    private long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Writes the HTML suitable for display to the user in the given
     * {@code page}.
     *
     * @param page Can't be {@code null}.
     */
    public abstract void writeDisplayHtml(ToolPageContext page) throws IOException;

    /**
     * User editing a piece of content in the tool.
     */
    public static class ContentEdit extends ToolUserAction {

        private UUID contentId;

        public UUID getContentId() {
            return contentId;
        }

        public void setContentId(UUID contentId) {
            this.contentId = contentId;
        }

        @Override
        public void writeDisplayHtml(ToolPageContext page) throws IOException {
            UUID contentId = getContentId();
            Object content = Query.
                    from(Object.class).
                    where("_id = ?", contentId).
                    first();

            page.writeHtml("editing ");
            page.writeStart("a",
                    "target", "_blank",
                    "href", page.cmsUrl("/content/edit.jsp", "id", contentId));
                page.writeTypeObjectLabel(content);
            page.writeEnd();
        }
    }
}
