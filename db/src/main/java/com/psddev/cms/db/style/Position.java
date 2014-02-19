package com.psddev.cms.db.style;

import java.io.IOException;

import com.psddev.dari.util.HtmlWriter;

public enum Position {

    FIT("Fit") {

        @Override
        public void writeCss(HtmlWriter writer, String selector) throws IOException {
            writer.writeCss(selector,
                    "height", "100%",
                    "position", "relative",
                    "width", "100%");
        }
    },

    TOP("Top") {

        @Override
        public void writeCss(HtmlWriter writer, String selector) throws IOException {
            writer.writeCss(selector,
                    "bottom", "auto",
                    "left", 0,
                    "position", "absolute",
                    "right", 0,
                    "top", 0);
        }
    },

    TOP_LEFT("Top Left") {

        @Override
        public void writeCss(HtmlWriter writer, String selector) throws IOException {
            writer.writeCss(selector,
                    "bottom", "auto",
                    "left", 0,
                    "position", "absolute",
                    "right", "auto",
                    "top", 0);
        }
    },

    TOP_RIGHT("Top Right") {

        @Override
        public void writeCss(HtmlWriter writer, String selector) throws IOException {
            writer.writeCss(selector,
                    "bottom", "auto",
                    "left", "auto",
                    "position", "absolute",
                    "right", 0,
                    "top", 0);
        }
    },

    BOTTOM("Bottom") {

        @Override
        public void writeCss(HtmlWriter writer, String selector) throws IOException {
            writer.writeCss(selector,
                    "bottom", 0,
                    "left", 0,
                    "position", "absolute",
                    "right", 0,
                    "top", "auto");
        }
    },

    BOTTOM_LEFT("Bottom Left") {

        @Override
        public void writeCss(HtmlWriter writer, String selector) throws IOException {
            writer.writeCss(selector,
                    "bottom", 0,
                    "left", 0,
                    "position", "absolute",
                    "right", "auto",
                    "top", "auto");
        }
    },

    BOTTOM_RIGHT("Bottom Right") {

        @Override
        public void writeCss(HtmlWriter writer, String selector) throws IOException {
            writer.writeCss(selector,
                    "bottom", 0,
                    "left", "auto",
                    "position", "absolute",
                    "right", 0,
                    "top", "auto");
        }
    };

    private final String label;

    private Position(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public abstract void writeCss(HtmlWriter writer, String selector) throws IOException;
}
