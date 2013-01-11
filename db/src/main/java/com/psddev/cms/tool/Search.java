package com.psddev.cms.tool;

import com.psddev.cms.db.Content;
import com.psddev.cms.db.Directory;
import com.psddev.cms.tool.ToolPageContext;

import com.psddev.dari.db.CompoundPredicate;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Predicate;
import com.psddev.dari.db.PredicateParser;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.PaginatedResult;
import com.psddev.dari.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Search extends Record {

    public static final String ADDITIONAL_PREDICATE_PARAMETER = "aq";
    public static final String LIMIT_PARAMETER = "l";
    public static final String OFFSET_PARAMETER = "o";
    public static final String ONLY_PATHED_PARAMETER = "p";
    public static final String PARENT_PARAMETER = "pt";
    public static final String QUERY_STRING_PARAMETER = "q";
    public static final String SELECTED_TYPE_PARAMETER = "st";
    public static final String SORT_PARAMETER = "s";
    public static final String TYPES_PARAMETER = "rt";

    private Set<ObjectType> types;
    private ObjectType selectedType;
    private String queryString;
    private boolean onlyPathed;
    private String additionalPredicate;
    private UUID parentId;
    private SearchSort sort;
    private long offset;
    private int limit;

    public Search() {
    }

    public Search(ToolPageContext page, Iterable<UUID> typeIds) {
        this.page = page;

        if (typeIds != null) {
            for (UUID typeId : typeIds) {
                ObjectType type = ObjectType.getInstance(typeId);

                if (type != null) {
                    getTypes().add(type);
                }
            }
        }

        setSelectedType(ObjectType.getInstance(page.param(UUID.class, SELECTED_TYPE_PARAMETER)));
        setQueryString(page.paramOrDefault(String.class, QUERY_STRING_PARAMETER, "").trim());
        setOnlyPathed(page.param(boolean.class, IS_ONLY_PATHED));
        setAdditionalPredicate(page.param(String.class, ADDITIONAL_QUERY_PARAMETER));
        setParentId(page.param(UUID.class, PARENT_PARAMETER));
        setSort(page.paramOrDefault(SearchSort.class, SORT_PARAMETER, null));
        setOffset(page.param(long.class, OFFSET_PARAMETER));
        setLimit(page.paramOrDefault(int.class, LIMIT_PARAMETER, 10));
    }

    public Search(ToolPageContext page) {
        this(page, page.params(UUID.class, TYPES_PARAMETER));
    }

    public Set<ObjectType> getTypes() {
        if (types == null) {
            types = new HashSet<ObjectType>();
        }
        return types;
    }

    public void setTypes(Set<ObjectType> types) {
        this.types = types;
    }

    public ObjectType getSelectedType() {
        return selectedType;
    }

    public void setSelectedType(ObjectType selectedType) {
        this.selectedType = selectedType;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public boolean isOnlyPathed() {
        return onlyPathed;
    }

    public void setOnlyPathed(boolean onlyPathed) {
        this.onlyPathed = onlyPathed;
    }

    public String getAdditionalPredicate() {
        return additionalPredicate;
    }

    public void setAdditionalPredicate(String additionalPredicate) {
        this.additionalPredicate = additionalPredicate;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public SearchSort getSort() {
        return sort;
    }

    public void setSort(SearchSort sort) {
        this.sort = sort;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Set<ObjectType> findValidTypes() {
        Set<ObjectType> types = getTypes();
        List<ObjectType> validTypes = new ArrayList<ObjectType>();

        if (types.size() == 1) {
            ObjectType type = types.iterator().next();

            if (type != null && Content.class.equals(type.getObjectClass())) {
                types.clear();
            }
        }

        for (ObjectType type : types) {
            validTypes.addAll(type.findConcreteTypes());
        }

        Collections.sort(validTypes);
        return new LinkedHashSet<ObjectType>(validTypes);
    }

    private String findAlphabeticalSortField() {
        ObjectType type = getSelectedType();
        Set<ObjectType> validTypes = findValidTypes();

        if (type == null && validTypes.size() == 1) {
            type = validTypes.iterator().next();
        }

        if (type != null) {
            for (String field : type.getLabelFields()) {
                if (type.getIndex(field) != null) {
                    return type.getInternalName() + "/" + field;
                }
            }
        }

        return null;
    }

    public List<SearchSort> findSorts() {
        List<SearchSort> sorts = new ArrayList<SearchSort>();

        Collections.addAll(sorts, SearchSort.values());

        if (ObjectUtils.isBlank(getQueryString())) {
            sorts.remove(SearchSort.RELEVANT);
        }

        if (findAlphabeticalSortField() == null) {
            sorts.remove(SearchSort.ALPHABETICALLY);
        }

        return sorts;
    }

    public Query<?> toQuery() {
        Query<?> query = null;
        Set<ObjectType> types = getTypes();
        ObjectType selectedType = getSelectedType();
        Set<ObjectType> validTypes = findValidTypes();
        boolean isAllSearchable = true;

        if (selectedType != null) {
            isAllSearchable = Content.Static.isSearchableType(selectedType);
            query = Query.from(Object.class).where("_type = ?", selectedType);

        } else {
            for (ObjectType type : validTypes) {
                if (!Content.Static.isSearchableType(type)) {
                    isAllSearchable = false;
                }
            }

            if (types.size() == 1) {
                for (ObjectType type : types) {
                    query = Query.fromType(type);
                    break;
                }

            } else {
                query = isAllSearchable ? Query.fromGroup(Content.SEARCHABLE_GROUP) : Query.fromAll();

                if (!validTypes.isEmpty()) {
                    query.where("_type = ?", validTypes);
                }
            }
        }

        String queryString = getQueryString();

        if (queryString.length() == 0) {
            if (isAllSearchable) {
                query.and("* ~= *");
            }

        } else {

            // Strip http: or https: from the query for search by path below.
            if (queryString.length() > 8
                    && StringUtils.matches(queryString, "(?i)https?://.*")) {
                int slashAt = queryString.indexOf("/", 8);

                if (slashAt > -1) {
                    queryString = queryString.substring(slashAt);
                }
            }

            // Search by path.
            if (queryString.startsWith("/") && queryString.length() > 1) {
                List<String> paths = new ArrayList<String>();

                for (Directory directory : Query.
                        from(Directory.class).
                        where("path ^=[c] ?", queryString).
                        selectAll()) {
                    paths.add(directory.getRawPath());
                }

                int lastSlashAt = queryString.lastIndexOf("/");

                if (lastSlashAt > 0) {
                    for (Directory directory : Query.
                            from(Directory.class).
                            where("path ^=[c] ?", queryString.substring(0, lastSlashAt)).
                            selectAll()) {
                        paths.add(directory.getRawPath() + queryString.substring(lastSlashAt + 1));
                    }
                }

                query.and(Directory.PATHS_FIELD + " ^= ?", paths);

            // Full text search.
            } else if (isAllSearchable) {
                int lastSpaceAt = queryString.lastIndexOf(" ");

                if (lastSpaceAt > -1) {
                    query.and("* ~= ?", Arrays.asList(queryString, queryString.substring(0, lastSpaceAt)));

                } else {
                    query.and("* ~= ?", queryString);
                }

            } else if (selectedType != null) {
                for (String field : selectedType.getLabelFields()) {
                    query.and(selectedType.getInternalName() + "/" + field + " ^=[c] ?", queryString);
                    break;
                }

            } else {
                Predicate predicate = null;

                for (ObjectType type : validTypes) {
                    String prefix = type.getInternalName() + "/";

                    for (String field : type.getLabelFields()) {
                        predicate = CompoundPredicate.combine(
                                PredicateParser.OR_OPERATOR,
                                predicate,
                                PredicateParser.Static.parse(prefix + field + " ^=[c] ?", queryString));
                        break;
                    }
                }

                query.and(predicate);
            }
        }

        if (isOnlyPathed()) {
            query.and(Directory.Static.hasPathPredicate());
        }

        String additionalPredicate = getAdditionalPredicate();

        if (!ObjectUtils.isBlank(additionalPredicate)) {
            query.and(additionalPredicate, Query.
                    from(Object.class).
                    where("_id = ?", getParentId()).
                    first());
        }

        SearchSort sort = getSort();

        if (sort == null) {
            setSort(findSorts().iterator().next());
            sort = getSort();
        }

        if (SearchSort.NEWEST.equals(sort)) {
            query.sortDescending(Content.UPDATE_DATE_FIELD);

        } else if (SearchSort.ALPHABETICALLY.equals(sort)) {
            String sortField = findAlphabeticalSortField();

            if (sortField != null) {
                query.sortAscending(sortField);
            }
        }

        return query;
    }

    // --- Deprecated ---

    /** @deprecated Use {@link #ADDITIONA_PREDICATE_PARAMETER} instead. */
    @Deprecated
    public static final String ADDITIONAL_QUERY_PARAMETER = ADDITIONAL_PREDICATE_PARAMETER;

    /** @deprecated Use {@link #ONLY_PATHED_PARAMETER} instead. */
    @Deprecated
    public static final String IS_ONLY_PATHED = ONLY_PATHED_PARAMETER;

    /** @deprecated Use {@link #TYPES_PARAMETER} instead. */
    @Deprecated
    public static final String REQUESTED_TYPES_PARAMETER = TYPES_PARAMETER;

    /** @deprecated Use {@link #Search(ToolPageContext, Collection)} instead. */
    @Deprecated
    public Search(ToolPageContext page, UUID... typeIds) {
        this(page, typeIds != null ? Arrays.asList(typeIds) : null);
    }

    /** @deprecated Use {@link #getTypes} instead. */
    @Deprecated
    public Set<ObjectType> getRequestedTypes() {
        return getTypes();
    }

    /** @deprecated Use {@link #findValidTypes} instead. */
    public Set<ObjectType> getValidTypes() {
        return findValidTypes();
    }

    @Deprecated
    private transient Query<?> query;

    /** @deprecated Use {@link #toQuery} instead. */
    @Deprecated
    public Query<?> getQuery() {
        if (query == null) {
            query = toQuery();
        }
        return query;
    }

    @Deprecated
    private transient ToolPageContext page;

    @Deprecated
    private transient PaginatedResult<?> result;

    /** @deprecated Use {@link #toQuery} instead. */
    @Deprecated
    public PaginatedResult<?> getResult() {
        if (result == null) {
            result = getQuery().and(page.siteItemsPredicate()).select(getOffset(), getLimit());
        }
        return result;
    }
}
