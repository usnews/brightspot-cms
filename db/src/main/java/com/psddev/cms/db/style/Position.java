package com.psddev.cms.db.style;

import java.io.IOException;

import com.psddev.cms.db.ToolUi;
import com.psddev.dari.db.Record;
import com.psddev.dari.util.HtmlWriter;

@Position.Embedded
public abstract class Position extends Record implements Styleable {

    protected void writeAbsolutePositionCss(
            HtmlWriter writer,
            String selector,
            Integer left,
            Integer right,
            Integer width,
            Integer top,
            Integer bottom,
            Integer height) throws IOException {

        writer.writeCss(selector,
                "bottom", bottom != null ? bottom + "px" : "auto",
                "height", height != null ? height + "px" : "auto",
                "left", left != null ? left + "px" : "auto",
                "position", "absolute",
                "right", right != null ? right + "px" : "auto",
                "top", top != null ? top + "px" : "auto",
                "width", width != null ? width + "px" : "auto");
    }

    public static class Automatic extends Position {

        private int left;
        private int right;

        private int top;
        private int bottom;

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public int getRight() {
            return right;
        }

        public void setRight(int right) {
            this.right = right;
        }

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }

        public int getBottom() {
            return bottom;
        }

        public void setBottom(int bottom) {
            this.bottom = bottom;
        }

        @Override
        public void writeCss(HtmlWriter writer, String selector) throws IOException {
            writer.writeCss(selector,
                    "position", "relative",
                    "margin",
                            getTop() + "px " +
                            getRight() + "px " +
                            getBottom() + "px " +
                            getLeft() + "px");
        }
    }

    public static class Fit extends Position {

        private int left;
        private int right;

        private int top;
        private int bottom;

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public int getRight() {
            return right;
        }

        public void setRight(int right) {
            this.right = right;
        }

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }

        public int getBottom() {
            return bottom;
        }

        public void setBottom(int bottom) {
            this.bottom = bottom;
        }

        @Override
        public void writeCss(HtmlWriter writer, String selector) throws IOException {
            writeAbsolutePositionCss(writer, selector, getLeft(), getRight(), null, getTop(), getBottom(), null);
        }
    }

    public static class Top extends Position {

        private int left;
        private int right;

        private int top;

        @ToolUi.Placeholder("Auto")
        private Integer height;

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public int getRight() {
            return right;
        }

        public void setRight(int right) {
            this.right = right;
        }

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        @Override
        public void writeCss(HtmlWriter writer, String selector) throws IOException {
            writeAbsolutePositionCss(writer, selector, getLeft(), getRight(), null, getTop(), null, getHeight());
        }
    }

    public static class TopLeft extends Position {

        private int left;

        @ToolUi.Placeholder("Auto")
        private Integer width;

        private int top;

        @ToolUi.Placeholder("Auto")
        private Integer height;

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        @Override
        public void writeCss(HtmlWriter writer, String selector) throws IOException {
            writeAbsolutePositionCss(writer, selector, getLeft(), null, getWidth(), getTop(), null, getHeight());
        }
    }

    public static class TopRight extends Position {

        private int right;

        @ToolUi.Placeholder("Auto")
        private Integer width;

        private int top;

        @ToolUi.Placeholder("Auto")
        private Integer height;

        public int getRight() {
            return right;
        }

        public void setRight(int right) {
            this.right = right;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        @Override
        public void writeCss(HtmlWriter writer, String selector) throws IOException {
            writeAbsolutePositionCss(writer, selector, null, getRight(), getWidth(), getTop(), null, getHeight());
        }
    }

    public static class Bottom extends Position {

        private int left;
        private int right;

        private int bottom;

        @ToolUi.Placeholder("Auto")
        private Integer height;

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public int getRight() {
            return right;
        }

        public void setRight(int right) {
            this.right = right;
        }

        public int getBottom() {
            return bottom;
        }

        public void setBottom(int bottom) {
            this.bottom = bottom;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        @Override
        public void writeCss(HtmlWriter writer, String selector) throws IOException {
            writeAbsolutePositionCss(writer, selector, getLeft(), getRight(), null, null, getBottom(), getHeight());
        }
    }

    public static class BottomLeft extends Position {

        private int left;

        @ToolUi.Placeholder("Auto")
        private Integer width;

        private int bottom;

        @ToolUi.Placeholder("Auto")
        private Integer height;

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public int getBottom() {
            return bottom;
        }

        public void setBottom(int bottom) {
            this.bottom = bottom;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        @Override
        public void writeCss(HtmlWriter writer, String selector) throws IOException {
            writeAbsolutePositionCss(writer, selector, getLeft(), null, getWidth(), null, getBottom(), getHeight());
        }
    }

    public static class BottomRight extends Position {

        private int right;

        @ToolUi.Placeholder("Auto")
        private Integer width;

        private int bottom;

        @ToolUi.Placeholder("Auto")
        private Integer height;

        public int getRight() {
            return right;
        }

        public void setRight(int right) {
            this.right = right;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public int getBottom() {
            return bottom;
        }

        public void setBottom(int bottom) {
            this.bottom = bottom;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        @Override
        public void writeCss(HtmlWriter writer, String selector) throws IOException {
            writeAbsolutePositionCss(writer, selector, null, getRight(), getWidth(), null, getBottom(), getHeight());
        }
    }
}
