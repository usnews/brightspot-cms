package com.psddev.cms.db.style;

import java.io.IOException;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.HtmlWriter;

@ValueOutput.Embedded
public abstract class ValueOutput extends Record {

    public abstract void writeHtml(HtmlWriter writer, Object value) throws IOException;

    public static class Html extends ValueOutput {

        @Override
        public void writeHtml(HtmlWriter writer, Object value) throws IOException {
            writer.writeRaw(value);
        }
    }

    public static class Image extends ValueOutput {

        @Override
        public void writeHtml(HtmlWriter writer, Object value) throws IOException {
            writer.writeElement("img", "src", value);
        }
    }
}
