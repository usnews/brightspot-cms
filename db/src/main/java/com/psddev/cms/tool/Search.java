package com.psddev.cms.tool;

import com.psddev.cms.db.Content;
import com.psddev.cms.db.Directory;
import com.psddev.cms.tool.ToolPageContext;

import com.psddev.dari.db.CompoundPredicate;
import com.psddev.dari.db.Predicate;
import com.psddev.dari.db.PredicateParser;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.PaginatedResult;
import com.psddev.dari.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class Search {

    public static final String IS_ONLY_PATHED = "p";
    public static final String LIMIT_PARAMETER = "l";
    public static final String OFFSET_PARAMETER = "o";
    public static final String QUERY_STRING_PARAMETER = "q";
    public static final String REQUESTED_TYPES_PARAMETER = "rt";
    public static final String SELECTED_TYPE_PARAMETER = "st";
    public static final String SORT_PARAMETER = "s";
    public static final String ADDITIONAL_QUERY_PARAMETER = "aq";

    private final Set<ObjectType> requestedTypes;
    private final Set<ObjectType> validTypes;
    private final ObjectType selectedType;
    private String queryString;
    private final SearchSort sort;
    private final Query<?> query;
    private final long offset;
    private final int limit;
    private PaginatedResult<?> result;

    public Search(ToolPageContext wp, UUID... requestedTypeIds) {

        // Types that have been requested for search.
        requestedTypes = new HashSet<ObjectType>();
        if (requestedTypeIds != null) {
            for (UUID typeId : requestedTypeIds) {
                ObjectType type = ObjectType.getInstance(typeId);
                if (type != null) {
                    requestedTypes.add(type);
                }
            }
        }

        if (requestedTypes.size() == 1) {
            for (ObjectType type : requestedTypes) {
                if (Content.class.equals(type.getObjectClass())) {
                    requestedTypes.clear();
                    break;
                }
            }
        }

        // All the types that are valid based on the requested types.
        validTypes = new TreeSet<ObjectType>();
        for (ObjectType type : requestedTypes) {
            validTypes.addAll(type.findConcreteTypes());
        }

        // Type that the user selected.
        boolean isAllSearchable = true;
        selectedType = Query.findById(ObjectType.class, wp.uuidParam(SELECTED_TYPE_PARAMETER));
        if (selectedType != null) {
            isAllSearchable = Content.Static.isSearchableType(selectedType);
            query = Query.fromType(selectedType).where("_type = ?", selectedType);
        } else {
            for (ObjectType type : validTypes) {
                if (!Content.Static.isSearchableType(type)) {
                    isAllSearchable = false;
                }
            }

            if (requestedTypes.size() == 1) {
                Query<?> q = null;
                for (ObjectType type : requestedTypes) {
                    q = Query.fromType(type);
                    break;
                }
                query = q;

            } else {
                query = isAllSearchable ? Query.fromGroup(Content.SEARCHABLE_GROUP) : Query.fromAll();
                if (!validTypes.isEmpty()) {
                    query.where("typeId = ?", validTypes);
                }
            }
        }

        // Force full text search on empty query.
        queryString = wp.param(QUERY_STRING_PARAMETER, "").trim();
        if (queryString.length() == 0) {
            if (isAllSearchable) {
                query.and("* ~= '*'");
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
                for (Directory directory : Query
                        .from(Directory.class)
                        .where("path ^=[c] ?", queryString)
                        .selectAll()) {
                    paths.add(directory.getRawPath());
                }
                int lastSlashAt = queryString.lastIndexOf("/");
                if (lastSlashAt > 0) {
                    for (Directory directory : Query
                            .from(Directory.class)
                            .where("path ^=[c] ?", queryString.substring(0, lastSlashAt))
                            .selectAll()) {
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

        // Only allow permalinked objects?
        if (wp.boolParam(IS_ONLY_PATHED)) {
            query.and(Directory.Static.hasPathPredicate());
        }

        String additionalQuery = wp.param(ADDITIONAL_QUERY_PARAMETER);
        if (!ObjectUtils.isBlank(additionalQuery)) {
            query.and(additionalQuery);
        }

        query.and(wp.siteItemsPredicate());

        // Automatic sort.
        sort = wp.enumParam(SearchSort.class, SORT_PARAMETER, null);
        if (sort == null) {
            if (selectedType != null) {
                /*
                for (String field : selectedType.getLabelFields()) {
                    query.sortAscending(field);
                }
                */

            } else if (validTypes.size() == 1 && !isAllSearchable) {
                ObjectType type = validTypes.iterator().next();
                for (String field : type.getLabelFields()) {
                    query.sortAscending(type.getInternalName() + "/" + field);
                }

            } else if (ObjectUtils.isBlank(queryString)) {
                query.sortDescending(Content.UPDATE_DATE_FIELD);
            }

        // Sort option manually selected.
        } else if (SearchSort.NEWEST.equals(sort)) {
            query.sortDescending(Content.UPDATE_DATE_FIELD);
        }

        this.offset = wp.longParam(OFFSET_PARAMETER);
        this.limit = wp.intParam(LIMIT_PARAMETER, 10);
    }

    public Search(ToolPageContext wp) {
        this(wp, wp.uuidParams(REQUESTED_TYPES_PARAMETER));
    }

    public Set<ObjectType> getRequestedTypes() {
        return this.requestedTypes;
    }

    public Set<ObjectType> getValidTypes() {
        return this.validTypes;
    }

    public ObjectType getSelectedType() {
        return this.selectedType;
    }

    public String getQueryString() {
        return this.queryString;
    }

    public SearchSort getSort() {
        return this.sort;
    }

    public Query<?> getQuery() {
        return this.query;
    }

    public long getOffset() {
        return this.offset;
    }

    public int getLimit() {
        return this.limit;
    }

    public PaginatedResult<?> getResult() {
        if (this.result == null) {
            this.result = getQuery().select(getOffset(), getLimit());
        }
        return this.result;
    }
}
