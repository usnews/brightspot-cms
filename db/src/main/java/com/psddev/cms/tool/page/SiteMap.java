package com.psddev.cms.tool.page;

import com.psddev.cms.db.Directory;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.PageWriter;
import com.psddev.cms.tool.ToolPageContext;

import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.PaginatedResult;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StringUtils;

import java.io.IOException;
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
        Directory selected = Query.findById(Directory.class, page.pageParam(UUID.class, "directoryId", null));
        int limit = page.pageParam(Integer.class, "limit", 20);

        if (selected == null) {
            selected = Query.from(Directory.class).first();
        }

        PaginatedResult<?> items = null;
        
        if (selected != null) {
            items = Query.
                    fromAll().
                    where(selected.itemsPredicate(null)).
                    and(page.siteItemsPredicate()).
                    sortAscending(Directory.PATHS_FIELD).
                    select(page.param(long.class, "offset"), limit);
        }

        PageWriter writer = page.getWriter();

        writer.start("div", "class", "widget");
            writer.start("h1", "class", "icon-sitemap").html("Site Map").end();

            writer.start("form", "action", page.url(null), "class", "autoSubmit", "method", "get");
                writer.start("select", "name", "directoryId");

                    writer.start("option", "value", "").html("- DIRECTORY -").end();

                    for (Directory directory : Query.
                            from(Directory.class).
                            sortAscending("path").
                            selectAll()) {
                        writer.start("option",
                                "value", directory.getId(),
                                "selected", directory.equals(selected) ? "selected" : null);
                            writer.html(directory.getPath());
                        writer.end();
                    }

                writer.end();
            writer.end();

            if (selected != null) {
                if (!items.hasItems()) {
                    writer.start("div", "class", "message message-warning");
                        writer.start("p");
                            writer.html("No items in ");
                            writer.start("strong").html(selected.getPath()).end();
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

                            String selectedPath = selected.getPath();

                            for (Object item : items.getItems()) {
                                State itemState = State.getInstance(item);
                                String permalink = null;

                                for (Directory.Path pathObject : itemState.as(Directory.ObjectModification.class).getPaths()) {
                                    String path = pathObject.getPath();

                                    if (StringUtils.getPathInfo(path, selectedPath) != null) {
                                        permalink = path;
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
                                            writer.html(permalink.substring(selectedPath.length()));
                                        writer.end();
                                    }

                                writer.end();
                            }

                        writer.end();
                    writer.end();
                }
            }

        writer.end();
    }
}
