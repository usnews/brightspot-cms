package com.psddev.cms.db.style;

import java.io.IOException;

import com.psddev.cms.db.ContentValue;
import com.psddev.dari.db.Record;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.ObjectUtils;

@Action.Embedded
public class Action extends Record {

    private boolean newWindow;

    @Required
    private ContentValue url;

    public boolean isNewWindow() {
        return newWindow;
    }

    public void setNewWindow(boolean newWindow) {
        this.newWindow = newWindow;
    }

    public ContentValue getUrl() {
        return url;
    }

    public void setUrl(ContentValue url) {
        this.url = url;
    }

    public boolean writeStart(HtmlWriter writer, Object content) throws IOException {
        ContentValue url = getUrl();
        Object urlValue = null;

        if (url != null) {
            urlValue = url.findValue(content);
        }

        if (ObjectUtils.isBlank(urlValue)) {
            return false;
        }

        writer.writeStart("a",
                "href", urlValue,
                "target", isNewWindow() ? "_blank" : null);

        return true;
    }
}
