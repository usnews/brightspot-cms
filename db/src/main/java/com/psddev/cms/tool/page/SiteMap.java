package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.Content;
import com.psddev.cms.db.Directory;
import com.psddev.cms.db.Template;
import com.psddev.cms.db.ToolUi;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.PageWriter;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.AggregateQueryResult;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.PaginatedResult;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StringUtils;

@RoutingFilter.Path(application = "cms", value = "/misc/siteMap.jsp")
@SuppressWarnings("serial")
public class SiteMap extends PageServlet {

    private static final int[] LIMITS = { 10, 20, 50 };
    private static final String URL_TYPE = "url";

    @Override
    protected String getPermissionId() {
        return "area/dashboard";
    }

    @Override
    protected void doService(final ToolPageContext page) throws IOException, ServletException {
        String type = page.pageParam(String.class, "type", null);
        final ObjectType itemType = Query.findById(ObjectType.class, page.pageParam(UUID.class, "itemType", null));
        long offset = page.param(long.class, "offset");
        int limit = page.pageParam(Integer.class, "limit", 20);

        List<ObjectType> types = new ArrayList<ObjectType>();
        boolean typeFound = false;

        for (ObjectType t : Database.Static.getDefault().getEnvironment().getTypes()) {
            if (t.as(ToolUi.class).isGlobalFilter()) {
                types.add(t);
                if (type != null && type.equals(t.getId().toString())) {
                    typeFound = true;
                }
            }
        }

        Collections.sort(types);

        if (!typeFound) {
            type = URL_TYPE;
        }

        String valueParameter = type + ".value";
        final String value = page.pageParam(String.class, valueParameter, null);
        Object valueObject = Query.from(Object.class).where("_id = ?", value).first();
        PaginatedResult<?> result = null;
        Long count = null;

        if (type.equals(URL_TYPE)) {
            if (!(valueObject instanceof Directory)) {
                valueObject = Query.from(Directory.class).where("path = /").first();
            }

            if (valueObject != null) {
                result = new AggregateQueryResult<Object>(offset, limit, new DirectoryQueryIterator(Query.
                        from(Directory.class).
                        where("path startsWith ?", ((Directory) valueObject).getPath()).
                        sortAscending("path")) {

                    @Override
                    protected Query<?> createQuery(Directory directory) {
                        return (itemType != null ? Query.fromType(itemType) : Query.fromAll()).
                                and(page.siteItemsPredicate()).
                                and(directory.itemsPredicate(page.getSite())).
                                sortAscending(Directory.PATHS_FIELD);
                    }
                });

                count = null;
            }

        } else if (valueObject != null) {
            Query<?> query = (itemType != null ? Query.fromType(itemType) : Query.fromAll()).
                    and(page.siteItemsPredicate()).
                    and("* matches ?", value).
                    and("cms.directory.paths != missing");

            if (query.hasMoreThan(250)) {
                result = new AggregateQueryResult<Object>(offset, limit, new DirectoryQueryIterator(Query.
                        from(Directory.class).
                        where("path startsWith /").
                        sortAscending("path")) {

                    @Override
                    protected Query<?> createQuery(Directory directory) {
                        return (itemType != null ? Query.fromType(itemType) : Query.fromAll()).
                                and(page.siteItemsPredicate()).
                                and(directory.itemsPredicate(page.getSite())).
                                and("* matches ?", value).
                                and("cms.directory.paths != missing");
                    }
                });

                count = query.count();

            } else {
                @SuppressWarnings("unchecked")
                List<Object> items = (List<Object>) query.selectAll();

                Collections.sort(items, new Comparator<Object>() {
                    @Override
                    public int compare(Object x, Object y) {
                        return ObjectUtils.compare(
                                State.getInstance(x).as(Directory.ObjectModification.class).getPermalink(),
                                State.getInstance(y).as(Directory.ObjectModification.class).getPermalink(),
                                true);
                    }
                });

                result = new PaginatedResult<Object>(offset, limit, items);
                count = (long) items.size();
            }
        }

        PageWriter writer = page.getWriter();

        writer.start("div", "class", "widget widget-sitemap");
            writer.start("h1").html("Sitemap").end();

            writer.start("form",
                    "class", "sitemap-filters",
                    "method", "get",
                    "action", page.url(null));

                writer.start("span", "class", "sitemap-filters-itemType");
                    page.typeSelect(
                            Template.Static.findUsedTypes(page.getSite()),
                            itemType,
                            "Everything",
                            "class", "autoSubmit",
                            "name", "itemType",
                            "data-searchable", "true");
                writer.end();

                writer.start("span", "class", "sitemap-filters-type");
                    writer.start("span", "class", "sitemap-filters-with");
                        writer.html("with");
                    writer.end();

                    writer.start("select",
                            "class", "autoSubmit",
                            "name", "type");

                        writer.start("option",
                                "selected", type.equals(URL_TYPE) ? "selected" : null,
                                "value", URL_TYPE);
                            writer.html("URL");
                        writer.end();

                        for (ObjectType t : types) {
                            String id = t.getId().toString();
                            writer.start("option",
                                    "selected", type.equals(id) ? "selected" : null,
                                    "value", id);
                                writer.html(t.getDisplayName());
                            writer.end();
                        }

                    writer.end();
                writer.end();

                writer.start("span", "class", "sitemap-filters-value");

                    Query<?> valueQuery;
                    ObjectType valueType;

                    if (type.equals(URL_TYPE)) {
                        valueType = ObjectType.getInstance(Directory.class);
                        valueQuery = Query.from(Directory.class).sortAscending("path");

                    } else {
                        valueType = ObjectType.getInstance(ObjectUtils.to(UUID.class, type));
                        valueQuery = Query.fromType(valueType);

                        for (String fieldName : valueType.getLabelFields()) {
                            ObjectField field = valueType.getField(fieldName);

                            if (field != null && valueType.getIndex(fieldName) != null) {
                                valueQuery.sortAscending(valueType.getInternalName() + "/" + fieldName);
                            }
                        }
                    }

                    if (valueQuery.hasMoreThan(250)) {
                        writer.tag("input",
                                "type", "text",
                                "class", "autoSubmit objectId",
                                "data-editable", false,
                                "data-label", valueObject != null ? State.getInstance(valueObject).getLabel() : null,
                                "data-typeIds", valueType.getId(),
                                "name", valueParameter,
                                "value", value);

                    } else {
                        writer.start("select",
                                "class", "autoSubmit",
                                "name", valueParameter,
                                "data-searchable", "true");

                            if (!type.equals(URL_TYPE)) {
                                writer.start("option", "value", "").end();
                            }

                            for (Object v : valueQuery.selectAll()) {
                                State state = State.getInstance(v);

                                writer.start("option",
                                        "value", state.getId(),
                                        "selected", v.equals(valueObject) ? "selected" : null);
                                    writer.html(state.getLabel());
                                writer.end();
                            }

                        writer.end();
                    }

                writer.end();

            writer.end();

            if (valueObject == null) {
                writer.start("div", "class", "sitemap-warning sitemap-warning-value");
                    writer.start("p");
                        writer.html("Please select a ");
                        writer.start("strong").html(valueType.getLabel()).end();
                        writer.html(".");
                    writer.end();
                writer.end();

            } else if (!result.hasItems()) {
                writer.start("div", "class", "sitemap-warning");
                    writer.start("p");
                        writer.html("No ");

                        if (itemType == null) {
                            writer.html("items");

                        } else {
                            writer.start("strong");
                                writer.objectLabel(itemType);
                            writer.end();
                        }

                        writer.html(" matching ");
                        writer.start("strong");
                            writer.objectLabel(valueObject);
                        writer.end();
                        writer.html(".");
                    writer.end();
                writer.end();

            } else {
                writer.start("ul", "class", "pagination");

                    if (result.hasPrevious()) {
                        writer.start("li", "class", "first");
                            writer.start("a", "href", page.url("", "offset", result.getFirstOffset())).html("First").end();
                        writer.end();

                        writer.start("li", "class", "previous");
                            writer.start("a", "href", page.url("", "offset", result.getPreviousOffset())).html("Previous ").html(result.getLimit()).end();
                        writer.end();
                    }

                    writer.start("li");
                        writer.start("form",
                                "class", "autoSubmit",
                                "method", "get",
                                "action", page.url(null));
                            writer.start("select", "name", "limit");
                                for (int l : LIMITS) {
                                    writer.start("option",
                                            "value", l,
                                            "selected", limit == l ? "selected" : null);
                                        writer.html("Show ");
                                        writer.html(l);
                                    writer.end();
                                }
                            writer.end();
                        writer.end();
                    writer.end();

                    if (count != null) {
                        writer.start("li");
                            writer.html(result.getFirstItemIndex());
                            writer.html(" to ");
                            writer.html(result.getLastItemIndex());
                            writer.html(" of ");
                            writer.start("strong").html(count).end();
                        writer.end();
                    }

                    if (result.hasNext()) {
                        writer.start("li", "class", "next");
                            writer.start("a", "href", page.url("", "offset", result.getNextOffset())).html("Next ").html(result.getLimit()).end();
                        writer.end();
                    }

                writer.end();

                writer.start("table", "class", "links pageThumbnails table-striped");
                    writer.start("tbody");

                        for (Object item : result.getItems()) {
                            State itemState = State.getInstance(item);
                            String permalink = null;
                            Content.ObjectModification itemContentData = itemState.as(Content.ObjectModification.class);
                            String statusId = itemContentData.getStatusId();

                            if (type.equals(URL_TYPE)) {
                                for (Directory.Path pathObject : itemState.as(Directory.ObjectModification.class).getPaths()) {
                                    String p = pathObject.getPath();

                                    if (StringUtils.getPathInfo(p, ((Directory) valueObject).getPath()) != null) {
                                        permalink = p;
                                        break;
                                    }
                                }

                            } else {
                                permalink = itemState.as(Directory.ObjectModification.class).getPermalink();
                            }

                            writer.start("tr", "data-preview-url", permalink);

                                writer.start("td");
                                    writer.typeLabel(item);
                                writer.end();

                                writer.start("td");
                                    if (statusId != null) {
                                        writer.start("span", "class", "contentStatusLabel contentStatusLabel-" + statusId);
                                            writer.html(itemContentData.getStatus());
                                        writer.end();
                                    }

                                    writer.html(" ");

                                    writer.start("a",
                                            "target", "_top",
                                            "href", page.objectUrl("/content/edit.jsp", item));
                                        writer.objectLabel(item);
                                    writer.end();
                                writer.end();

                                if (permalink == null) {
                                    writer.start("td").end();

                                } else {
                                    writer.start("td", "data-preview-anchor", "");
                                        writer.html(permalink);
                                    writer.end();
                                }

                            writer.end();
                        }

                    writer.end();
                writer.end();
            }

        writer.end();
    }

    private static abstract class DirectoryQueryIterator implements Iterator<Query<?>> {

        private final Iterator<Directory> directoryIterator;

        public DirectoryQueryIterator(Query<Directory> directoryQuery) {
            this.directoryIterator = directoryQuery.iterable(0).iterator();
        }

        protected abstract Query<?> createQuery(Directory directory);

        @Override
        public boolean hasNext() {
            return directoryIterator.hasNext();
        }

        @Override
        public Query<?> next() {
            if (hasNext()) {
                return createQuery(directoryIterator.next());
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
