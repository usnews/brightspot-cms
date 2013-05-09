package com.psddev.cms.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;

import com.psddev.cms.db.Content;
import com.psddev.cms.db.Directory;
import com.psddev.cms.db.ImageTag;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ImageEditor;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.PaginatedResult;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.StringUtils;

public class SearchResultRenderer {

    private static final String PREVIOUS_DATE_ATTRIBUTE = SearchResultRenderer.class.getName() + ".previousDate";

    protected final ToolPageContext page;
    protected final PageWriter writer;
    protected final Search search;
    protected final ObjectField sortField;
    protected final boolean showTypeLabel;
    protected final PaginatedResult<?> result;

    public SearchResultRenderer(ToolPageContext page, Search search) throws IOException {
        this.page = page;
        this.writer = page.getWriter();
        this.search = search;
        this.result = search.toQuery(page.getSite()).select(search.getOffset(), search.getLimit());

        ObjectType selectedType = search.getSelectedType();

        if (selectedType != null) {
            this.sortField = selectedType.getField(search.getSort());
            this.showTypeLabel = false;

        } else {
            this.sortField = null;
            this.showTypeLabel = search.findValidTypes().size() != 1;
        }
    }

    public void render() throws IOException {
        if (search.isSuggestions() && ObjectUtils.isBlank(search.getQueryString())) {
            String frameName = page.createId();

            writer.writeStart("div", "class", "frame", "name", frameName);
            writer.writeEnd();

            writer.writeStart("form",
                    "class", "searchSuggestionsForm",
                    "method", "post",
                    "action", page.url("/content/suggestions.jsp"),
                    "target", frameName);
                writer.writeTag("input",
                        "type", "hidden",
                        "name", "search",
                        "value", ObjectUtils.toJson(search.getState().getSimpleValues()));
            writer.writeEnd();
        }

        writer.writeStart("h2").writeHtml("Result").writeEnd();

        if (search.findSorts().size() > 1) {
            writer.writeStart("div", "class", "searchSorter");
                renderSorter();
            writer.writeEnd();
        }

        writer.writeStart("div", "class", "searchPagination");
            renderPagination();
        writer.writeEnd();

        writer.writeStart("div", "class", "searchResultList");
            if (result.hasItems()) {
                renderList(result.getItems());
            } else {
                renderEmpty();
            }
        writer.writeEnd();
    }

    public void renderSorter() throws IOException {
        writer.writeStart("form",
                "class", "autoSubmit",
                "method", "get",
                "action", page.url(null));

            for (Map.Entry<String, List<String>> entry : StringUtils.getQueryParameterMap(page.url("",
                    Search.SORT_PARAMETER, null,
                    Search.SHOW_MISSING_PARAMETER, null,
                    Search.OFFSET_PARAMETER, null)).entrySet()) {
                String name = entry.getKey();

                for (String value : entry.getValue()) {
                    writer.writeTag("input", "type", "hidden", "name", name, "value", value);
                }
            }

            writer.writeStart("select", "name", Search.SORT_PARAMETER);
                for (Map.Entry<String, String> entry : search.findSorts().entrySet()) {
                    String label = entry.getValue();
                    String value = entry.getKey();

                    writer.writeStart("option",
                            "value", value,
                            "selected", value.equals(search.getSort()) ? "selected" : null);
                        writer.writeHtml("Sort: ").writeHtml(label);
                    writer.writeEnd();
                }
            writer.writeEnd();

            if (sortField != null) {
                writer.writeHtml(" ");

                writer.writeTag("input",
                        "id", page.createId(),
                        "type", "checkbox",
                        "name", Search.SHOW_MISSING_PARAMETER,
                        "value", "true",
                        "checked", search.isShowMissing() ? "checked" : null);

                writer.writeHtml(" ");

                writer.writeStart("label", "for", page.getId());
                    writer.writeHtml("Show Missing");
                writer.writeEnd();
            }

        writer.writeEnd();
    }

    public void renderPagination() throws IOException {
        writer.writeStart("ul", "class", "pagination");

            if (result.hasPrevious()) {
                writer.writeStart("li", "class", "previous");
                    writer.writeStart("a", "href", page.url("", Search.OFFSET_PARAMETER, result.getPreviousOffset()));
                        writer.writeHtml("Previous ");
                        writer.writeHtml(result.getLimit());
                    writer.writeEnd();
                writer.writeEnd();
            }

            writer.writeStart("li");
                writer.writeHtml(result.getFirstItemIndex());
                writer.writeHtml(" to ");
                writer.writeHtml(result.getLastItemIndex());
                writer.writeHtml(" of ");
                writer.writeStart("strong").writeHtml(result.getCount()).writeEnd();
            writer.writeEnd();

            if (result.hasNext()) {
                writer.writeStart("li", "class", "next");
                    writer.writeStart("a", "href", page.url("", Search.OFFSET_PARAMETER, result.getNextOffset()));
                        writer.writeHtml("Next ");
                        writer.writeHtml(result.getLimit());
                    writer.writeEnd();
                writer.writeEnd();
            }

        writer.writeEnd();
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
            writer.writeStart("div", "class", "searchResultImages");
                for (Map.Entry<Object, StorageItem> entry : previews.entrySet()) {
                    renderImage(entry.getKey(), entry.getValue());
                }
            writer.writeEnd();
        }

        if (!items.isEmpty()) {
            writer.writeStart("table", "class", "searchResultTable links table-striped pageThumbnails");
                writer.writeStart("tbody");
                    for (Object item : items) {
                        renderRow(item);
                    }
                writer.writeEnd();
            writer.writeEnd();
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

        writer.writeStart("figure");
            writer.writeTag("img",
                    "alt", (showTypeLabel ? page.getTypeLabel(item) + ": " : "") + page.getObjectLabel(item),
                    "src", page.url(url));

            writer.writeStart("figcaption");
                if (showTypeLabel) {
                    writer.typeLabel(item);
                    writer.writeHtml(": ");
                }
                writer.objectLabel(item);
            writer.writeEnd();
        writer.writeEnd();

        renderAfterItem(item);
    }

    public void renderRow(Object item) throws IOException {
        HttpServletRequest request = page.getRequest();
        State itemState = State.getInstance(item);
        String permalink = itemState.as(Directory.ObjectModification.class).getPermalink();

        writer.writeStart("tr",
                "data-preview-url", permalink,
                "class", State.getInstance(item).getId().equals(page.param(UUID.class, "id")) ? "selected" : null);

            if (Search.NEWEST_SORT_VALUE.equals(search.getSort())) {
                DateTime updateDateTime = page.toUserDateTime(itemState.as(Content.ObjectModification.class).getUpdateDate());

                if (updateDateTime == null) {
                    writer.writeStart("td", "colspan", 2);
                        writer.writeHtml("N/A");
                    writer.writeEnd();

                } else {
                    String updateDate = page.formatUserDate(updateDateTime);

                    writer.writeStart("td", "class", "date");
                        if (!ObjectUtils.equals(updateDate, request.getAttribute(PREVIOUS_DATE_ATTRIBUTE))) {
                            request.setAttribute(PREVIOUS_DATE_ATTRIBUTE, updateDate);
                            writer.writeHtml(updateDate);
                        }
                    writer.writeEnd();

                    writer.writeStart("td", "class", "time");
                        writer.writeHtml(page.formatUserTime(updateDateTime));
                    writer.writeEnd();
                }
            }

            if (showTypeLabel) {
                writer.writeStart("td");
                    writer.typeLabel(item);
                writer.writeEnd();
            }

            writer.writeStart("td", "data-preview-anchor", "");
                renderBeforeItem(item);
                writer.objectLabel(item);
                renderAfterItem(item);
            writer.writeEnd();

            if (sortField != null) {
                Object value = itemState.get(sortField.getInternalName());

                writer.writeStart("td");
                    writer.writeHtml(value instanceof Recordable ?
                            ((Recordable) value).getState().getLabel() :
                            value);
                writer.writeEnd();
            }

        writer.writeEnd();
    }

    public void renderBeforeItem(Object item) throws IOException {
        writer.writeStart("a",
                "href", page.objectUrl("/content/edit.jsp", item, "search", page.url("", Search.NAME_PARAMETER, null)),
                "data-objectId", State.getInstance(item).getId(),
                "target", "_top");
    }

    public void renderAfterItem(Object item) throws IOException {
        writer.writeEnd();
    }

    public void renderEmpty() throws IOException {
        writer.writeStart("div", "class", "message message-warning");
            writer.writeStart("p");
                writer.writeHtml("No matching items!");
            writer.writeEnd();
        writer.writeEnd();
    }
}
