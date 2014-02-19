package com.psddev.cms.db.style;

import java.io.IOException;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.HtmlWriter;

@Decoration.Embedded
public abstract class Decoration extends Record implements Styleable {

    protected void writeOffset(HtmlWriter writer, String selector, Sides offset) throws IOException {
        String offsetTop;
        String offsetRight;
        String offsetBottom;
        String offsetLeft;

        if (offset != null) {
            offsetTop = offset.getTop() + "px";
            offsetRight = offset.getRight() + "px";
            offsetBottom = offset.getBottom() + "px";
            offsetLeft = offset.getLeft() + "px";

        } else {
            offsetTop = "0";
            offsetRight = "0";
            offsetBottom = "0";
            offsetLeft = "0";
        }

        writer.writeCss(selector,
                "bottom", offsetBottom,
                "left", offsetLeft,
                "position", "absolute",
                "right", offsetRight,
                "top", offsetTop);
    }

    protected void writeSides(HtmlWriter writer, String selector, Sides sides, String property) throws IOException {
        if (sides != null) {
            sides.writeCss(writer, selector, property);

        } else {
            writer.writeCss(selector, property, 0);
        }
    }

    protected void writeRadius(HtmlWriter writer, String selector, Corners radius) throws IOException {
        if (radius != null) {
            radius.writeCss(writer, selector, "border-radius");

        } else {
            writer.writeCss(selector, "border-radius", 0);
        }
    }
}
