<%@ page import="

com.psddev.cms.db.Site,
com.psddev.cms.db.ToolUi,
com.psddev.cms.tool.PageWriter,
com.psddev.cms.tool.Search,
com.psddev.cms.tool.SearchResultRenderer,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Database,
com.psddev.dari.db.ObjectField,
com.psddev.dari.db.ObjectFieldComparator,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.PredicateParser,
com.psddev.dari.db.Query,
com.psddev.dari.db.SolrDatabase,
com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.PaginatedResult,

java.util.ArrayList,
java.util.Collections,
java.util.Comparator,
java.util.HashMap,
java.util.List,
java.util.Map,
java.util.UUID,

org.apache.solr.client.solrj.SolrQuery,
org.apache.solr.common.params.CommonParams
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

try {
    Map<String, Object> searchData = (Map<String, Object>) ObjectUtils.fromJson(wp.param(String.class, "search"));
    Search search = new Search(wp);

    search.getState().setValues(searchData);

    Map<String, Object> objectData = (Map<String, Object>) ObjectUtils.fromJson(wp.param(String.class, "object"));
    Object object = Database.Static.getDefault().getEnvironment().createObject(ObjectUtils.to(UUID.class, objectData.get("_type")), ObjectUtils.to(UUID.class, objectData.get("_id")));
    State objectState = State.getInstance(object);

    objectState.setValues(objectData);

    ObjectType objectType = objectState.getType();

    if (objectType == null) {
        return;
    }

    String fieldName = wp.param(String.class, "field");
    ObjectField field = objectType.getField(fieldName);

    if (field == null) {
        return;
    }

    Map<Object, Float> suggestions = new HashMap<Object, Float>();
    StringBuilder filter = new StringBuilder();

    for (ObjectType t : field.as(ToolUi.class).findDisplayTypes()) {
        filter.append(SolrDatabase.Static.escapeValue(t.getId()));
        filter.append(" || ");
    }

    Site site = wp.getUser().getCurrentSite();

    for (Object item : findSimilar(object, filter, 10)) {
        Float score = SolrDatabase.Static.getNormalizedScore(item);

        if (score != null && score > 0.7) {
            if (site != null &&
                    !PredicateParser.Static.evaluate(item, site.itemsPredicate())) {
                continue;
            }

            suggestions.put(item, score);
        }
    }

    filter.setLength(0);

    String fieldClass = field.getJavaDeclaringClassName();

    for (ObjectType type : objectState.getDatabase().getEnvironment().getTypes()) {
        ObjectField f = type.getField(fieldName);

        if (f != null && ObjectUtils.equals(fieldClass, f.getJavaDeclaringClassName())) {
            filter.append(SolrDatabase.Static.escapeValue(type.getId()));
            filter.append(" || ");
        }
    }

    Map<Object, Integer> similar = new HashMap<Object, Integer>();

    for (Object item : findSimilar(object, filter, 20)) {
        Float score = SolrDatabase.Static.getNormalizedScore(item);

        if (score > 0.7) {
            if (site != null &&
                    !PredicateParser.Static.evaluate(item, site.itemsPredicate())) {
                continue;
            }

            Object value = State.getInstance(item).get(fieldName);

            if (value == null) {
                continue;
            }

            if (value instanceof Map) {
                value = ((Map<?, ?>) value).values();
            }

            if (value instanceof Iterable) {
                for (Object v : (Iterable<?>) value) {
                    incrementCount(similar, v);
                }

            } else {
                incrementCount(similar, value);
            }
        }
    }

    for (Map.Entry<Object, Integer> entry : similar.entrySet()) {
        Integer count = entry.getValue();

        if (count > 1) {
            suggestions.put(entry.getKey(), ((float) count) / similar.size());
        }
    }

    if (suggestions.isEmpty()) {
        return;
    }

    Object fieldValue = State.getInstance(object).get(fieldName);

    if (fieldValue != null) {
        if (fieldValue instanceof Map) {
            fieldValue = ((Map<?, ?>) fieldValue).values();
        }

        if (fieldValue instanceof Iterable) {
            for (Object v : (Iterable<?>) fieldValue) {
                suggestions.remove(v);
            }

        } else {
            suggestions.remove(fieldValue);
        }
    }

    if (suggestions.isEmpty()) {
        return;
    }

    List<Map.Entry<Object, Float>> suggestionsList = new ArrayList<Map.Entry<Object, Float>>(suggestions.entrySet());

    Collections.sort(suggestionsList, new Comparator<Map.Entry<Object, Float>>() {
        public int compare(Map.Entry<Object, Float> x, Map.Entry<Object, Float> y) {
            float xv = x.getValue();
            float yv = y.getValue();

            return xv == yv ? 0 : xv < yv ? 1 : -1;
        }
    });

    List<Object> sortedSuggestions = new ArrayList<Object>();

    for (Map.Entry<Object, Float> entry : suggestionsList) {
        sortedSuggestions.add(entry.getKey());
    }

    if (sortedSuggestions.size() > 10) {
        sortedSuggestions = sortedSuggestions.subList(0, 10);
    }

    PageWriter writer = wp.getWriter();

    writer.start("div", "class", "searchSuggestions");
        writer.start("h2").html("Suggestions").end();
        new SearchResultRenderer(wp, search).renderList(sortedSuggestions);
    writer.end();

} catch (Exception error) {
}
%><%!

private static List<?> findSimilar(Object object, StringBuilder filter, int rows) {
    if (filter.length() < 1) {
        return Collections.emptyList();
    }

    filter.setLength(filter.length() - 4);
    filter.insert(0, "typeId:(");
    filter.append(")");

    SolrDatabase solr = Database.Static.getFirst(SolrDatabase.class);
    SolrQuery solrQuery = solr.buildSimilarQuery(object);

    solrQuery.add(CommonParams.FQ, filter.toString());
    solrQuery.setStart(0);
    solrQuery.setRows(rows);

    return solr.queryPartialWithOptions(solrQuery, null).getItems();
}

private static void incrementCount(Map<Object, Integer> map, Object object) {
    Integer count = map.get(object);

    map.put(object, count != null ? count + 1 : 1);
}
%>
