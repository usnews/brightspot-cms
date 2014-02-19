package com.psddev.cms.db.style;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.psddev.cms.db.ToolUi;
import com.psddev.dari.db.Record;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.IoUtils;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;

@Text.Embedded
public class Text extends Record implements Styleable {

    @ToolUi.ColorPicker
    @ToolUi.Placeholder("Inherit")
    private String textColor;

    @ToolUi.ColorPicker
    @ToolUi.Placeholder("Inherit")
    private String linkColor;

    @ToolUi.Placeholder("Inherit")
    private String font;

    @ToolUi.Placeholder("Inherit")
    private Integer size;

    private boolean bold;
    private boolean italic;

    @ToolUi.Placeholder("Inherit")
    private Integer lineHeight;

    @ToolUi.Placeholder("Inherit")
    private Integer letterSpacing;

    @ToolUi.Placeholder("Left")
    private TextAlign align;

    @ToolUi.Placeholder("None")
    private TextTransform transform;

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getLinkColor() {
        return linkColor;
    }

    public void setLinkColor(String linkColor) {
        this.linkColor = linkColor;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public Integer getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(Integer lineHeight) {
        this.lineHeight = lineHeight;
    }

    public Integer getLetterSpacing() {
        return letterSpacing;
    }

    public void setLetterSpacing(Integer letterSpacing) {
        this.letterSpacing = letterSpacing;
    }

    public TextAlign getAlign() {
        return align;
    }

    public void setAlign(TextAlign align) {
        this.align = align;
    }

    public TextTransform getTransform() {
        return transform;
    }

    public void setTransform(TextTransform transform) {
        this.transform = transform;
    }

    @Override
    public void writeCss(HtmlWriter writer, String selector) throws IOException {
        String textColor = getTextColor();
        String linkColor = getLinkColor();
        String font = getFont();
        Integer size = getSize();
        Integer lineHeight = getLineHeight();
        Integer letterSpacing = getLetterSpacing();
        TextAlign align = getAlign();
        TextTransform transform = getTransform();

        if (font != null) {
            Matcher fontMatcher = Pattern.compile(".*https?://fonts.googleapis.com/css.*family=([^:]+).*").matcher(font);

            if (fontMatcher.matches()) {
                writer.writeRaw(IoUtils.toString(new URL(font)));

                font = StringUtils.decodeUri(fontMatcher.group(1));
            }
        }

        writer.writeCss(selector,
                "color", !ObjectUtils.isBlank(textColor) ? textColor : "inherit",
                "font-family", font != null ? "'" + font + "'" : "inherit",
                "font-size", size != null ? size + "px" : "inherit",
                "font-style", isItalic() ? "italic" : "inherit",
                "font-weight", isBold() ? "bold" : "inherit",
                "line-height", lineHeight != null ? lineHeight + "px" : (size != null ? 1.5 : "inherit"),
                "letter-spacing", letterSpacing != null ? letterSpacing + "px" : "inherit",
                "text-align", align != null ? align.getCssPropertyValue() : "left",
                "text-transform", transform != null ? transform.name() : "inherit");

        writer.writeCss(selector + " a",
                "color", !ObjectUtils.isBlank(linkColor) ? linkColor : "inherit");
    }
}
