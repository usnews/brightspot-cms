package com.psddev.cms.db.style;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Throwables;
import com.psddev.cms.db.ToolUi;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.ObjectUtils;

public abstract class ValueBlockData extends BlockData {

    @ToolUi.DisplayLast
    private List<ValueFilter> filters;

    @ToolUi.DisplayLast
    @ToolUi.Placeholder("Text")
    private ValueOutput output;

    public List<ValueFilter> getFilters() {
        if (filters == null) {
            filters = new ArrayList<ValueFilter>();
        }
        return filters;
    }

    public void setFilters(List<ValueFilter> filters) {
        this.filters = filters;
    }

    public ValueOutput getOutput() {
        return output;
    }

    public void setOutput(ValueOutput output) {
        this.output = output;
    }

    public abstract Object findValue(Object content) throws Exception;

    @Override
    public final boolean writeHtml(HtmlWriter writer, Object content) throws IOException {
        try {
            if (!ObjectUtils.isBlank(content)) {
                content = findValue(content);

                for (ValueFilter filter : getFilters()) {
                    content = filter.filter(content);

                    if (!ObjectUtils.isBlank(content)) {
                        break;
                    }
                }
            }

            if (!ObjectUtils.isBlank(content)) {
                ValueOutput output = getOutput();

                if (output == null) {
                    writer.writeHtml(content);

                } else {
                    output.writeHtml(writer, content);
                }

                return true;

            } else {
                return false;
            }

        } catch (Exception error) {
            Throwables.propagateIfInstanceOf(error, IOException.class);
            throw Throwables.propagate(error);
        }
    }
}
