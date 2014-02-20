package com.psddev.cms.db.style;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.psddev.cms.db.Content;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.StringUtils;

public abstract class AbstractBlock extends Content implements Styleable {

    @Indexed(unique = true)
    @Required
    private String name;

    private BlockData data;

    @Required
    private Position position;

    private Text text;

    private Integer opacity;
    private Integer rotate;
    private List<Decoration> decorations;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BlockData getData() {
        return data;
    }

    public void setData(BlockData data) {
        this.data = data;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public Integer getOpacity() {
        return opacity;
    }

    public void setOpacity(Integer opacity) {
        this.opacity = opacity;
    }

    public Integer getRotate() {
        return rotate;
    }

    public void setRotate(Integer rotate) {
        this.rotate = rotate;
    }

    public List<Decoration> getDecorations() {
        if (decorations == null) {
            decorations = new ArrayList<Decoration>();
        }
        return decorations;
    }

    public void setDecorations(List<Decoration> decorations) {
        this.decorations = decorations;
    }

    public String getInternalName() {
        return StringUtils.toCamelCase(getName().trim());
    }

    public void writeHtml(HtmlWriter writer, Object content) throws IOException {
        BlockData data = getData();

        if (data != null) {
            writer.writeStart("div", "class", "bsp-block bsp-block-" + getInternalName());
                for (int i = 0, size = getDecorations().size(); i < size; ++ i) {
                    writer.writeStart("div", "class", "bsp-block_d" + i);
                    writer.writeEnd();
                }

                data.writeHtml(writer, content);
            writer.writeEnd();
        }
    }

    @Override
    public void writeCss(HtmlWriter writer, String selector) throws IOException {
        getPosition().writeCss(writer, selector);

        Text text = getText();

        if (text != null) {
            text.writeCss(writer, selector);

        } else {
            writer.writeCss(selector,
                    "color", "inherit",
                    "font-family", "inherit",
                    "font-size", "inherit",
                    "font-style", "inherit",
                    "font-weight", "inherit",
                    "line-height", "inherit",
                    "letter-spacing", "inherit",
                    "text-transform", "inherit");

            writer.writeCss(selector + " a",
                    "color", "inherit");
        }

        Integer opacity = getOpacity();

        writer.writeCss(selector, "opacity", opacity == null || opacity >= 100 ? 1.0 : opacity / 100.0);

        Integer rotate = getRotate();

        if (rotate != null) {
            String transform = "rotate(" + rotate + "deg)";

            writer.writeCss(selector,
                    "-moz-transform", transform,
                    "-ms-transform", transform,
                    "-o-transform", transform,
                    "-webkit-transform", transform,
                    "transform", transform);

        } else {
            writer.writeCss(selector,
                    "-moz-transform", "none",
                    "-ms-transform", "none",
                    "-o-transform", "none",
                    "-webkit-transform", "none",
                    "transform", "none");
        }

        List<Decoration> decorations = getDecorations();

        for (int i = 0, size = decorations.size(); i < size; ++ i) {
            decorations.get(i).writeCss(writer, selector + " > .bsp-block_d" + i);
        }

        BlockData data = getData();

        if (data instanceof BlockData.Container) {
            for (Block child : ((BlockData.Container) data).getChildren()) {
                child.writeCss(writer, selector + " > .bsp-block-" + child.getInternalName());
            }
        }
    }
}
