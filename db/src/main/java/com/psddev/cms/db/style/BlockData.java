package com.psddev.cms.db.style;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.psddev.cms.db.ContentValue;
import com.psddev.cms.db.ToolUi;
import com.psddev.dari.db.Record;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.ObjectUtils;

@BlockData.Embedded
public abstract class BlockData extends Record {

    public abstract boolean writeHtml(HtmlWriter writer, Object content) throws IOException;

    public static class Cascade extends BlockData {

        private List<BlockData> data;

        public List<BlockData> getData() {
            if (data == null) {
                data = new ArrayList<BlockData>();
            }
            return data;
        }

        public void setData(List<BlockData> data) {
            this.data = data;
        }

        @Override
        public boolean writeHtml(HtmlWriter writer, Object content) throws IOException {
            for (BlockData d : getData()) {
                if (d.writeHtml(writer, content)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static class Container extends BlockData {

        private List<Block> children;

        public List<Block> getChildren() {
            if (children == null) {
                children = new ArrayList<Block>();
            }
            return children;
        }

        public void setChildren(List<Block> children) {
            this.children = children;
        }

        @Override
        public boolean writeHtml(HtmlWriter writer, Object content) throws IOException {
            for (Block child : getChildren()) {
                child.writeHtml(writer, content);
            }

            return true;
        }
    }

    public static class Value extends BlockData {

        @Required
        private ContentValue value;

        @ToolUi.Placeholder("Text")
        private ValueOutput output;

        private Action action;

        public ContentValue getValue() {
            return value;
        }

        public void setValue(ContentValue value) {
            this.value = value;
        }

        public ValueOutput getOutput() {
            return output;
        }

        public void setOutput(ValueOutput output) {
            this.output = output;
        }

        public Action getAction() {
            return action;
        }

        public void setAction(Action action) {
            this.action = action;
        }

        @Override
        public boolean writeHtml(HtmlWriter writer, Object content) throws IOException {
            ContentValue value = getValue();

            if (value != null) {
                Object valueValue = value.findValue(content);

                if (!ObjectUtils.isBlank(valueValue)) {
                    Action action = getAction();

                    if (action != null && action.writeStart(writer, content)) {
                        writer.writeHtml(valueValue);
                        writer.writeEnd();

                    } else {
                        writer.writeHtml(valueValue);
                    }
                }
            }

            return false;
        }
    }
}
