package com.psddev.cms.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.psddev.dari.db.ForwardingDatabase;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.PaginatedResult;

public class PreviewDatabase extends ForwardingDatabase {

    private final Map<UUID, Map<String, Object>> changesById = new HashMap<UUID, Map<String, Object>>();

    public void addChanges(Schedule schedule) {
        for (Draft draft : Query.
                from(Draft.class).
                where("schedule = ?", schedule).
                selectAll()) {
            changesById.put(draft.getObjectId(), draft.getObjectChanges());
        }
    }

    // --- ForwardingDatabase support ---

    private <T> T applyChanges(T object) {
        if (object != null) {
            State state = State.getInstance(object);
            Map<String, Object> changes = changesById.get(state.getId());

            if (changes != null) {
                state.putAll(changes);
            }
        }

        return object;
    }

    @Override
    public <T> List<T> readAll(Query<T> query) {
        List<T> all = super.readAll(query);

        for (T item : all) {
            applyChanges(item);
        }

        return all;
    }

    @Override
    public <T> T readFirst(Query<T> query) {
        return applyChanges(super.readFirst(query));
    }

    @Override
    public <T> Iterable<T> readIterable(Query<T> query, int fetchSize) {
        return new FilteringIterable<T>(super.readIterable(query, fetchSize));
    }

    private class FilteringIterable<E> implements Iterable<E> {

        private final Iterable<E> delegate;

        public FilteringIterable(Iterable<E> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Iterator<E> iterator() {
            return new FilteringIterator<E>(delegate.iterator());
        }
    }

    private class FilteringIterator<E> implements Iterator<E> {

        private final Iterator<E> delegate;

        public FilteringIterator(Iterator<E> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public E next() {
            return applyChanges(delegate.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public <T> PaginatedResult<T> readPartial(Query<T> query, long offset, int limit) {
        PaginatedResult<T> result = super.readPartial(query, offset, limit);

        for (T item : result.getItems()) {
            applyChanges(item);
        }

        return result;
    }

    // --- Deprecated ---

    @Deprecated
    @Override
    public <T> List<T> readList(Query<T> query) {
        return readAll(query);
    }
}
