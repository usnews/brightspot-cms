package com.psddev.cms.db.style;

import java.io.IOException;

import com.psddev.dari.util.HtmlWriter;

public interface Styleable {

    public void writeCss(HtmlWriter writer, String selector) throws IOException;
}
