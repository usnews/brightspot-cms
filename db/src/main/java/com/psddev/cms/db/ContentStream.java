package com.psddev.cms.db;

import java.util.ArrayList;
import java.util.List;

import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;

@ContentStream.Embedded
public abstract class ContentStream extends Record {

    public abstract List<?> findContents(int offset, int limit);

    @SuppressWarnings("rawtypes")
    public static class Search extends PinContentStream {

        @Embedded
        @Required
        private Query query;

        public Query getQuery() {
            return query;
        }

        public void setQuery(Query query) {
            this.query = query;
        }

        @Override
        protected List<?> doFindContents(int offset, int limit) {
            return getQuery().select(offset, limit).getItems();
        }
    }

    public static class Static extends ContentStream {

        private List<Content> contents;

        public List<Content> getContents() {
            if (contents == null) {
                contents = new ArrayList<Content>();
            }
            return contents;
        }

        public void setContents(List<Content> contents) {
            this.contents = contents;
        }

        @Override
        public List<Content> findContents(int offset, int limit) {
            return getContents();
        }
    }
}
