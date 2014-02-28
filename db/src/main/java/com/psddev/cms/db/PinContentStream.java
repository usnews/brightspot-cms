package com.psddev.cms.db;

import java.util.ArrayList;
import java.util.List;

import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.db.State;

public abstract class PinContentStream extends ContentStream {

    @ToolUi.DisplayLast
    private List<Pin> pins;

    public List<Pin> getPins() {
        if (pins == null) {
            pins = new ArrayList<Pin>();
        }
        return pins;
    }

    public void setPins(List<Pin> pins) {
        this.pins = pins;
    }

    protected abstract List<?> doFindContents(int offset, int limit);

    public final List<?> findContents(int offset, int limit) {
        List<Object> contents = new ArrayList<Object>(doFindContents(offset, limit));

        for (Pin pin : getPins()) {
            int position = pin.getPosition() - offset;

            if (0 <= position && position < contents.size()) {
                contents.add(position, pin.getContent());
            }
        }

        if (contents.size() > limit) {
            contents = contents.subList(0, limit);
        }

        return contents;
    }

    @Embedded
    public static class Pin extends Record {

        private int position;

        @Types({ Record.class })
        private Recordable content;

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public Object getContent() {
            return content;
        }

        public void setContent(Object content) {
            this.content = (Recordable) content;
        }

        @Override
        public String getLabel() {
            StringBuilder label = new StringBuilder();
            Object content = getContent();

            label.append("Position: ");
            label.append(getPosition());

            if (content != null) {
                State contentState = State.getInstance(content);
                ObjectType contentType = contentState.getType();

                label.append(" \u2192 ");
                label.append(contentType != null ? contentType.getLabel() : "Unknown Type");
                label.append(": ");
                label.append(contentState.getLabel());
            }

            return label.toString();
        }
    }
}
