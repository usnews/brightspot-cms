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

        if (count == null && result != null && result.getFirstItemIndex() == 1 && !result.hasNext()) {
            count = (long) result.getItems().size();
        }

        PageWriter writer = page.getWriter();

        writer.writeStart("div", "class", "widget widget-sitemap");
            writer.writeStart("h1").writeHtml("Sitemap").writeEnd();

            writer.writeStart("form",
                    "class", "sitemap-filters",
                    "method", "get",
                    "action", page.url(null));

                writer.writeStart("span", "class", "sitemap-filters-itemType");
                    page.typeSelect(
                            Template.Static.findUsedTypes(page.getSite()),
                            itemType,
                            "Everything",
                            "class", "autoSubmit",
                            "name", "itemType",
                            "data-searchable", "true");
                writer.writeEnd();

                if (types.isEmpty()) {
                    writer.writeStart("span", "class", "sitemap-filters-prep");
                        writer.writeHtml("in");
                    writer.writeEnd();

                    writer.writeTag("input",
                            "type", "hidden",
                            "name", "type",
                            "value", URL_TYPE);

                } else {
                    writer.writeStart("span", "class", "sitemap-filters-prep");
                        writer.writeHtml("with");
                    writer.writeEnd();

                    writer.writeStart("span", "class", "sitemap-filters-type");
                        writer.writeStart("select",
                                "class", "autoSubmit",
                                "name", "type");

                            writer.writeStart("option",
                                    "selected", type.equals(URL_TYPE) ? "selected" : null,
                                    "value", URL_TYPE);
                                writer.writeHtml("URL");
                            writer.writeEnd();

                            for (ObjectType t : types) {
                                String id = t.getId().toString();
                                writer.writeStart("option",
                                        "selected", type.equals(id) ? "selected" : null,
                                        "value", id);
                                    writer.writeHtml(t.getDisplayName());
                                writer.writeEnd();
                            }

                        writer.writeEnd();
                    writer.writeEnd();
                }

                writer.writeStart("span", "class", "sitemap-filters-value");

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
                        writer.writeTag("input",
                                "type", "text",
                                "class", "autoSubmit objectId",
                                "data-editable", false,
                                "data-label", valueObject != null ? State.getInstance(valueObject).getLabel() : null,
                                "data-typeIds", valueType.getId(),
                                "name", valueParameter,
                                "value", value);

                    } else {
                        writer.writeStart("select",
                                "class", "autoSubmit",
                                "name", valueParameter,
                                "data-searchable", "true");

                            if (!type.equals(URL_TYPE)) {
                                writer.writeStart("option", "value", "").writeEnd();
                            }

                            for (Object v : valueQuery.selectAll()) {
                                State state = State.getInstance(v);

                                writer.writeStart("option",
                                        "value", state.getId(),
                                        "selected", v.equals(valueObject) ? "selected" : null);
                                    writer.writeHtml(state.getLabel());
                                writer.writeEnd();
                            }

                        writer.writeEnd();
                    }

                writer.writeEnd();

            writer.writeEnd();

            if (valueObject == null) {
                writer.writeStart("div", "class", "sitemap-warning sitemap-warning-value");
                    writer.writeStart("p");
                        writer.writeHtml("Please select a ");
                        writer.writeStart("strong").writeHtml(valueType.getLabel()).writeEnd();
                        writer.writeHtml(".");
                    writer.writeEnd();
                writer.writeEnd();

            } else if (!result.hasItems()) {
                writer.writeStart("div", "class", "sitemap-warning");
                    writer.writeStart("p");
                        writer.writeHtml("No ");

                        if (itemType == null) {
                            writer.writeHtml("items");

                        } else {
                            writer.writeStart("strong");
                                writer.objectLabel(itemType);
                            writer.writeEnd();
                        }

                        writer.writeHtml(" matching ");
                        writer.writeStart("strong");
                            writer.objectLabel(valueObject);
                        writer.writeEnd();
                        writer.writeHtml(".");
                    writer.writeEnd();
                writer.writeEnd();

            } else {
                writer.writeStart("ul", "class", "pagination");

                    if (result.hasPrevious()) {
                        writer.writeStart("li", "class", "first");
                            writer.writeStart("a", "href", page.url("", "offset", result.getFirstOffset())).writeHtml("First").writeEnd();
                        writer.writeEnd();

                        writer.writeStart("li", "class", "previous");
                            writer.writeStart("a", "href", page.url("", "offset", result.getPreviousOffset())).writeHtml("Previous ").writeHtml(result.getLimit()).writeEnd();
                        writer.writeEnd();
                    }

                    writer.writeStart("li");
                        writer.writeStart("form",
                                "class", "autoSubmit",
                                "method", "get",
                                "action", page.url(null));
                            writer.writeStart("select", "name", "limit");
                                for (int l : LIMITS) {
                                    writer.writeStart("option",
                                            "value", l,
                                            "selected", limit == l ? "selected" : null);
                                        writer.writeHtml("Show ");
                                        writer.writeHtml(l);
                                    writer.writeEnd();
                                }
                            writer.writeEnd();
                        writer.writeEnd();
                    writer.writeEnd();

                    if (count != null) {
                        writer.writeStart("li");
                            writer.writeHtml(result.getFirstItemIndex());
                            writer.writeHtml(" to ");
                            writer.writeHtml(result.getLastItemIndex());
                            writer.writeHtml(" of ");
                            writer.writeStart("strong").writeHtml(count).writeEnd();
                        writer.writeEnd();
                    }

                    if (result.hasNext()) {
                        writer.writeStart("li", "class", "next");
                            writer.writeStart("a", "href", page.url("", "offset", result.getNextOffset())).writeHtml("Next ").writeHtml(result.getLimit()).writeEnd();
                        writer.writeEnd();
                    }

                writer.writeEnd();

                writer.writeStart("table", "class", "links pageThumbnails table-striped");
                    writer.writeStart("tbody");

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

                            writer.writeStart("tr", "data-preview-url", permalink);

                                writer.writeStart("td");
                                    writer.typeLabel(item);
                                writer.writeEnd();

                                writer.writeStart("td");
                                    if (statusId != null) {
                                        writer.writeStart("span", "class", "contentStatusLabel contentStatusLabel-" + statusId);
                                            writer.writeHtml(itemContentData.getStatus());
                                        writer.writeEnd();
                                    }

                                    writer.writeHtml(" ");

                                    writer.writeStart("a",
                                            "target", "_top",
                                            "href", page.objectUrl("/content/edit.jsp", item));
                                        writer.objectLabel(item);
                                    writer.writeEnd();
                                writer.writeEnd();

                                if (permalink == null) {
                                    writer.writeStart("td").writeEnd();

                                } else {
                                    writer.writeStart("td", "data-preview-anchor", "");
                                        writer.writeHtml(permalink);
                                    writer.writeEnd();
                                }

                            writer.writeEnd();
                        }

                    writer.writeEnd();
                writer.writeEnd();
            }

        writer.writeEnd();
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
