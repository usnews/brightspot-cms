package com.psddev.cms.db;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.psddev.cms.db.ToolUi;
import com.psddev.dari.db.Record;
import com.psddev.dari.util.ObjectUtils;

@ContentValueFilter.Embedded
public abstract class ContentValueFilter extends Record {

    public abstract Object filter(Object input) throws Exception;

    public static class Prepend extends ContentValueFilter {

        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public Object filter(Object input) {
            String text = getText();

            return ObjectUtils.isBlank(text) ? input : text + input;
        }
    }

    public static class Append extends ContentValueFilter {

        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public Object filter(Object input) {
            String text = getText();

            return ObjectUtils.isBlank(text) ? input : input + text;
        }
    }

    public static class Date extends ContentValueFilter {

        @ToolUi.NoteHtml("See <a href=\"http://joda-time.sourceforge.net/apidocs/org/joda/time/format/DateTimeFormat.html\">Joda-Time <code>DateTimeFormat</code> documention</a> for more information on how to write the pattern.")
        private String inputPattern;

        @Required
        private String outputPattern;

        public String getInputPattern() {
            return inputPattern;
        }

        public void setInputPattern(String inputPattern) {
            this.inputPattern = inputPattern;
        }

        public String getOutputPattern() {
            return outputPattern;
        }

        public void setOutputPattern(String outputPattern) {
            this.outputPattern = outputPattern;
        }

        @Override
        public Object filter(Object input) {
            String outputPattern = getOutputPattern();

            if (input instanceof String) {
                String inputString = (String) input;
                String inputPattern = getInputPattern();

                return (ObjectUtils.isBlank(inputPattern) ?
                        DateTimeFormat.forPattern(inputPattern).parseDateTime(inputString) :
                        ObjectUtils.to(DateTime.class, inputString)).
                        toString(outputPattern);

            } else {
                return new DateTime(input).toString(outputPattern);
            }
        }
    }
}
