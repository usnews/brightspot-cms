package com.psddev.cms.tool.page;

import com.psddev.cms.db.Directory;
import com.psddev.cms.db.Template;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.PageWriter;
import com.psddev.cms.tool.ToolPageContext;

import com.psddev.dari.db.AggregateQueryResult;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.PaginatedResult;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

@RoutingFilter.Path(application = "cms", value = "/misc/siteMap.jsp")
@SuppressWarnings("serial")
public class SiteMap extends PageServlet {

    private static final int[] LIMITS = { 10, 20, 50 };

    @Override
    protected String getPermissionId() {
        return "area/dashboard";
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        String path = page.pageParam(String.class, "path", "/");
        ObjectType type = Query.findById(ObjectType.class, page.pageParam(UUID.class, "typeId", null));
        long offset = page.param(long.class, "offset");
        int limit = page.pageParam(Integer.class, "limit", 20);
        List<Query<?>> queries = new ArrayList<Query<?>>();

        for (Directory d : Query.
                from(Directory.class).
                where("path startsWith ?", path).
                sortAscending("path").
                selectAll()) {
            queries.add((type != null ? Query.fromType(type) : Query.fromAll()).
                    and(page.siteItemsPredicate()).
                    and(d.itemsPredicate(page.getSite())).
                    sortAscending(Directory.PATHS_FIELD));
        }

        PaginatedResult<?> items = new AggregateQueryResult<Object>(offset, limit, queries.toArray(new Query<?>[queries.size()]));
        PageWriter writer = page.getWriter();

        writer.start("div", "class", "widget");
            writer.start("h1", "class", "icon-sitemap").html("Site Map").end();

            writer.start("form", "action", page.url(null), "method", "get");

                writer.start("select",
                        "class", "autoSubmit",
                        "name", "path",
                        "data-searchable", "true");
                    for (Directory d : Query.
                            from(Directory.class).
                            sortAscending("path").
                            selectAll()) {
                        String p = d.getPath();

                        writer.start("option",
                                "value", p,
                                "selected", p.equals(path) ? "selected" : null);
                            writer.html(p);
                        writer.end();
                    }

                writer.end();

                writer.html(" ");

                writer.start("select",
                        "class", "autoSubmit",
                        "name", "typeId",
                        "data-searchable", "true");
                    writer.start("option",
                            "value", "",
                            "selected", type == null ? "selected" : null);
                        writer.html("All Types");
                    writer.end();

                    for (ObjectType t : Template.Static.findUsedTypes(page.getSite())) {
                        writer.start("option",
                                "value", t.getId(),
                                "selected", t.equals(type) ? "selected" : null);
                            writer.html(t.getLabel());
                        writer.end();
                    }
                writer.end();

            writer.end();

            if (!items.hasItems()) {
                writer.start("div", "class", "message message-warning");
                    writer.start("p");
                        writer.html("No items");

                        if (type != null) {
                            writer.html(" of ");
                            writer.start("strong").html(type.getLabel()).end();
                            writer.html(" type ");
                        }

                        writer.html(" in ");
                        writer.start("strong").html(path).end();
                        writer.html(" directory.");
                    writer.end();
                writer.end();

            } else {
                writer.start("ul", "class", "pagination");

                    if (items.hasPrevious()) {
                        writer.start("li", "class", "first");
                            writer.start("a", "href", page.url("", "offset", items.getFirstOffset())).html("First").end();
                        writer.end();

                        writer.start("li", "class", "previous");
                            writer.start("a", "href", page.url("", "offset", items.getPreviousOffset())).html("Previous ").html(items.getLimit()).end();
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

                    if (items.hasNext()) {
                        writer.start("li", "class", "next");
                            writer.start("a", "href", page.url("", "offset", items.getNextOffset())).html("Next ").html(items.getLimit()).end();
                        writer.end();
                    }

                writer.end();

                writer.start("table", "class", "links pageThumbnails table-striped");
                    writer.start("tbody");

                        for (Object item : items.getItems()) {
                            State itemState = State.getInstance(item);
                            String permalink = null;

                            for (Directory.Path pathObject : itemState.as(Directory.ObjectModification.class).getPaths()) {
                                String p = pathObject.getPath();

                                if (StringUtils.getPathInfo(p, path) != null) {
                                    permalink = p;
                                    break;
                                }
                            }

                            writer.start("tr", "data-preview-url", permalink);

                                writer.start("td");
                                    writer.typeLabel(item);
                                writer.end();

                                writer.start("td");
                                    writer.start("a",
                                            "target", "_top",
                                            "href", page.objectUrl("/content/edit.jsp", item));
                                        writer.objectLabel(item);
                                    writer.end();
                                writer.end();

                                if (permalink == null) {
                                    writer.start("td").end();

                                } else {
                                    writer.start("td");
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
}
