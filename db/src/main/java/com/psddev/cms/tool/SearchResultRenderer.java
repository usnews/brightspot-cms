package com.psddev.cms.tool;

import com.psddev.cms.db.Content;
import com.psddev.cms.db.Directory;
import com.psddev.cms.db.ImageTag;

import com.psddev.dari.db.State;
import com.psddev.dari.util.ImageEditor;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.PaginatedResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;

public class SearchResultRenderer {

    private static final String PREVIOUS_DATE_ATTRIBUTE = SearchResultRenderer.class.getName() + ".previousDate";

    protected final ToolPageContext page;
    protected final PageWriter writer;
    protected final Search search;
    protected final PaginatedResult<?> result;
    protected final boolean showTypeLabel;

    public SearchResultRenderer(ToolPageContext page, Search search) throws IOException {
        this.page = page;
        this.writer = page.getWriter();
        this.search = search;
        this.result = search.toQuery().and(page.siteItemsPredicate()).select(search.getOffset(), search.getLimit());
        this.showTypeLabel = search.getSelectedType() == null && search.findValidTypes().size() != 1;
    }

    public void render() throws IOException {
        if (ObjectUtils.isBlank(search.getQueryString())) {
            String frameName = page.createId();

            writer.start("div", "class", "frame", "name", frameName);
            writer.end();

            writer.start("form",
                    "class", "searchForm-resultSuggestionsForm",
                    "method", "post",
                    "action", page.url("/content/suggestions.jsp"),
                    "target", frameName);
                writer.tag("input",
                        "type", "hidden",
                        "name", "search",
                        "value", ObjectUtils.toJson(search.getState().getSimpleValues()));
            writer.end();
        }

        writer.start("h2").html("Result").end();

        if (result.hasItems()) {
            writer.start("div", "class", "searchForm-resultSorter");
                renderSorter();
            writer.end();

            writer.start("div", "class", "searchForm-resultPagination");
                renderPagination();
            writer.end();

            writer.start("div", "class", "searchForm-resultList");
                renderList(result.getItems());
            writer.end();

        } else {
            writer.start("div", "class", "searchForm-resultList");
                renderEmpty();
            writer.end();
        }

        String name = page.param(String.class, "name");

        if (name != null) {
            page.putUserSetting("search." + name, search.getState().getSimpleValues());
        }
    }

    public void renderSorter() throws IOException {
        writer.start("form",
                "class", "autoSubmit",
                "method", "get",
                "action", page.url(null));

            for (Map.Entry<String, List<String>> entry : StringUtils.getQueryParameterMap(page.url("", Search.SORT_PARAMETER, null)).entrySet()) {
                String name = entry.getKey();

                for (String value : entry.getValue()) {
                    writer.tag("input", "type", "hidden", "name", name, "value", value);
                }
            }

            writer.start("select", "name", Search.SORT_PARAMETER);
                for (SearchSort sort : search.findSorts()) {
                    writer.start("option",
                            "value", sort.name(),
                            "selected", sort.equals(search.getSort()) ? "selected" : null);
                        writer.html("Sort: ").html(sort);
                    writer.end();
                }
            writer.end();

        writer.end();
    }

    public void renderPagination() throws IOException {
        writer.start("ul", "class", "pagination");

            if (result.hasPrevious()) {
                writer.start("li", "class", "previous");
                    writer.start("a", "href", page.url("", Search.OFFSET_PARAMETER, result.getPreviousOffset()));
                        writer.html("Previous ");
                        writer.html(result.getLimit());
                    writer.end();
                writer.end();
            }

            writer.start("li");
                writer.html(result.getFirstItemIndex());
                writer.html(" to ");
                writer.html(result.getLastItemIndex());
                writer.html(" of ");
                writer.start("strong").html(result.getCount()).end();
            writer.end();

            if (result.hasNext()) {
                writer.start("li", "class", "next");
                    writer.start("a", "href", page.url("", Search.OFFSET_PARAMETER, result.getNextOffset()));
                        writer.html("Next ");
                        writer.html(result.getLimit());
                    writer.end();
                writer.end();
            }

        writer.end();
    }

    public void renderList(Collection<?> listItems) throws IOException {
        List<Object> items = new ArrayList<Object>(listItems);
        Map<Object, StorageItem> previews = new LinkedHashMap<Object, StorageItem>();

        for (ListIterator<Object> i = items.listIterator(); i.hasNext(); ) {
            Object item = i.next();
            State itemState = State.getInstance(item);
            StorageItem preview = itemState.getPreview();

            if (preview != null) {
                String contentType = preview.getContentType();

                if (contentType != null && contentType.startsWith("image/")) {
                    i.remove();
                    previews.put(item, preview);
                }
            }
        }

        if (!previews.isEmpty()) {
            writer.start("div", "class", "searchForm-resultListImages");
                for (Map.Entry<Object, StorageItem> entry : previews.entrySet()) {
                    renderImage(entry.getKey(), entry.getValue());
                }
            writer.end();
        }

        if (!items.isEmpty()) {
            writer.start("table", "class", "searchForm-resultListTable links table-striped pageThumbnails");
                writer.start("tbody");
                    for (Object item : items) {
                        renderRow(item);
                    }
                writer.end();
            writer.end();
        }
    }

    public void renderImage(Object item, StorageItem image) throws IOException {
        String url = null;

        if (ImageEditor.Static.getDefault() != null) {
            url = new ImageTag.Builder(image).setHeight(100).toUrl();
        }

        if (url == null) {
            url = image.getPublicUrl();
        }

        renderBeforeItem(item);

        writer.start("figure");
            writer.tag("img",
                    "alt", (showTypeLabel ? page.getTypeLabel(item) + ": " : "") + page.getObjectLabel(item),
                    "src", page.url(url));

            writer.start("figcaption");
                if (showTypeLabel) {
                    writer.typeLabel(item);
                    writer.html(": ");
                }
                writer.objectLabel(item);
            writer.end();
        writer.end();

        renderAfterItem(item);
    }

    public void renderRow(Object item) throws IOException {
        HttpServletRequest request = page.getRequest();
        String permalink = State.getInstance(item).as(Directory.ObjectModification.class).getPermalink();

        writer.start("tr",
                "data-preview-url", permalink,
                "class", State.getInstance(item).getId().equals(page.param(UUID.class, "id")) ? "selected" : null);

            if (search.getSort() == SearchSort.NEWEST) {
                Date updateDate = State.getInstance(item).as(Content.ObjectModification.class).getUpdateDate();

                if (updateDate == null) {
                    writer.start("td", "colspan", 2);
                        writer.html("N/A");
                    writer.end();

                } else {
                    DateTime jodaUpdateDate = new DateTime(updateDate);
                    String date = jodaUpdateDate.toString("MMM dd, yyyy");

                    writer.start("td", "class", "date");
                        if (!ObjectUtils.equals(date, request.getAttribute(PREVIOUS_DATE_ATTRIBUTE))) {
                            request.setAttribute(PREVIOUS_DATE_ATTRIBUTE, date);
                            writer.html(date);
                        }
                    writer.end();

                    writer.start("td", "class", "time");
                        writer.html(jodaUpdateDate.toString("hh:mm a"));
                    writer.end();
                }
            }

            if (showTypeLabel) {
                writer.start("td");
                    writer.typeLabel(item);
                writer.end();
            }

            writer.start("td");
                renderBeforeItem(item);
                writer.objectLabel(item);
                renderAfterItem(item);
            writer.end();

        writer.end();
    }

    public void renderBeforeItem(Object item) throws IOException {
        writer.start("a",
                "href", page.objectUrl("/content/edit.jsp", item, "search", page.url("")),
                "target", "_top");
    }

    public void renderAfterItem(Object item) throws IOException {
        writer.end();
    }

    public void renderEmpty() throws IOException {
        writer.start("div", "class", "message message-warning");
            writer.start("p");
                writer.html("No matching items!");
            writer.end();
        writer.end();
    }
}
