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
import com.psddev.cms.db.Taxon;
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

    @Deprecated
    protected final PageWriter writer;

    protected final Search search;
    protected final ObjectField sortField;
    protected final boolean showTypeLabel;
    protected final PaginatedResult<?> result;

    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("unchecked")
    public void render() throws IOException {
        page.writeStart("h2").writeHtml("Result").writeEnd();

        if (ObjectUtils.isBlank(search.getQueryString()) &&
                search.getSelectedType() != null &&
                search.getSelectedType().getGroups().contains(Taxon.class.getName())) {
            page.writeStart("div", "class", "searchTaxonomy");
                page.writeStart("ul", "class", "taxonomy");
                    for (Taxon t : Taxon.Static.getRoots((Class<Taxon>) search.getSelectedType().getObjectClass())) {
                        writeTaxon(t);
                    }
                page.writeEnd();
            page.writeEnd();

        } else {
            if (search.findSorts().size() > 1) {
                page.writeStart("div", "class", "searchSorter");
                    renderSorter();
                page.writeEnd();
            }

            page.writeStart("div", "class", "searchPagination");
                renderPagination();
            page.writeEnd();

            page.writeStart("div", "class", "searchResultList");
                if (result.hasPages()) {
                    renderList(result.getItems());
                } else {
                    renderEmpty();
                }
            page.writeEnd();
        }

        if (search.isSuggestions() && ObjectUtils.isBlank(search.getQueryString())) {
            String frameName = page.createId();

            page.writeStart("div", "class", "frame", "name", frameName);
            page.writeEnd();

            page.writeStart("form",
                    "class", "searchSuggestionsForm",
                    "method", "post",
                    "action", page.url("/content/suggestions.jsp"),
                    "target", frameName);
                page.writeTag("input",
                        "type", "hidden",
                        "name", "search",
                        "value", ObjectUtils.toJson(search.getState().getSimpleValues()));
            page.writeEnd();
        }
    }

    private void writeTaxon(Taxon taxon) throws IOException {
        page.writeStart("li");
            renderBeforeItem(taxon);
            page.writeObjectLabel(taxon);
            renderAfterItem(taxon);

            Collection<? extends Taxon> children = taxon.getChildren();

            if (children != null && !children.isEmpty()) {
                page.writeStart("ul");
                    for (Taxon c : children) {
                        writeTaxon(c);
                    }
                page.writeEnd();
            }
        page.writeEnd();
    }

    public void renderSorter() throws IOException {
        page.writeStart("form",
                "class", "autoSubmit",
                "method", "get",
                "action", page.url(null));

            for (Map.Entry<String, List<String>> entry : StringUtils.getQueryParameterMap(page.url("",
                    Search.SORT_PARAMETER, null,
                    Search.SHOW_MISSING_PARAMETER, null,
                    Search.OFFSET_PARAMETER, null)).entrySet()) {
                String name = entry.getKey();

                for (String value : entry.getValue()) {
                    page.writeTag("input", "type", "hidden", "name", name, "value", value);
                }
            }

            page.writeStart("select", "name", Search.SORT_PARAMETER);
                for (Map.Entry<String, String> entry : search.findSorts().entrySet()) {
                    String label = entry.getValue();
                    String value = entry.getKey();

                    page.writeStart("option",
                            "value", value,
                            "selected", value.equals(search.getSort()) ? "selected" : null);
                        page.writeHtml("Sort: ").writeHtml(label);
                    page.writeEnd();
                }
            page.writeEnd();

            if (sortField != null) {
                page.writeHtml(" ");

                page.writeTag("input",
                        "id", page.createId(),
                        "type", "checkbox",
                        "name", Search.SHOW_MISSING_PARAMETER,
                        "value", "true",
                        "checked", search.isShowMissing() ? "checked" : null);

                page.writeHtml(" ");

                page.writeStart("label", "for", page.getId());
                    page.writeHtml("Show Missing");
                page.writeEnd();
            }

        page.writeEnd();
    }

    public void renderPagination() throws IOException {
        page.writeStart("ul", "class", "pagination");

            if (result.hasPrevious()) {
                page.writeStart("li", "class", "previous");
                    page.writeStart("a", "href", page.url("", Search.OFFSET_PARAMETER, result.getPreviousOffset()));
                        page.writeHtml("Previous ");
                        page.writeHtml(result.getLimit());
                    page.writeEnd();
                page.writeEnd();
            }

            page.writeStart("li");
                page.writeHtml(result.getFirstItemIndex());
                page.writeHtml(" to ");
                page.writeHtml(result.getLastItemIndex());
                page.writeHtml(" of ");
                page.writeStart("strong").writeHtml(result.getCount()).writeEnd();
            page.writeEnd();

            if (result.hasNext()) {
                page.writeStart("li", "class", "next");
                    page.writeStart("a", "href", page.url("", Search.OFFSET_PARAMETER, result.getNextOffset()));
                        page.writeHtml("Next ");
                        page.writeHtml(result.getLimit());
                    page.writeEnd();
                page.writeEnd();
            }

        page.writeEnd();
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
            page.writeStart("div", "class", "searchResultImages");
                for (Map.Entry<Object, StorageItem> entry : previews.entrySet()) {
                    renderImage(entry.getKey(), entry.getValue());
                }
            page.writeEnd();
        }

        if (!items.isEmpty()) {
            page.writeStart("table", "class", "searchResultTable links table-striped pageThumbnails");
                page.writeStart("tbody");
                    for (Object item : items) {
                        renderRow(item);
                    }
                page.writeEnd();
            page.writeEnd();
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

        page.writeStart("figure");
            page.writeTag("img",
                    "alt", (showTypeLabel ? page.getTypeLabel(item) + ": " : "") + page.getObjectLabel(item),
                    "src", page.url(url));

            page.writeStart("figcaption");
                if (showTypeLabel) {
                    page.writeTypeLabel(item);
                    page.writeHtml(": ");
                }
                page.writeObjectLabel(item);
            page.writeEnd();
        page.writeEnd();

        renderAfterItem(item);
    }

    public void renderRow(Object item) throws IOException {
        HttpServletRequest request = page.getRequest();
        State itemState = State.getInstance(item);
        String permalink = itemState.as(Directory.ObjectModification.class).getPermalink();

        page.writeStart("tr",
                "data-preview-url", permalink,
                "class", State.getInstance(item).getId().equals(page.param(UUID.class, "id")) ? "selected" : null);

            if (Search.NEWEST_SORT_VALUE.equals(search.getSort())) {
                DateTime updateDateTime = page.toUserDateTime(itemState.as(Content.ObjectModification.class).getUpdateDate());

                if (updateDateTime == null) {
                    page.writeStart("td", "colspan", 2);
                        page.writeHtml("N/A");
                    page.writeEnd();

                } else {
                    String updateDate = page.formatUserDate(updateDateTime);

                    page.writeStart("td", "class", "date");
                        if (!ObjectUtils.equals(updateDate, request.getAttribute(PREVIOUS_DATE_ATTRIBUTE))) {
                            request.setAttribute(PREVIOUS_DATE_ATTRIBUTE, updateDate);
                            page.writeHtml(updateDate);
                        }
                    page.writeEnd();

                    page.writeStart("td", "class", "time");
                        page.writeHtml(page.formatUserTime(updateDateTime));
                    page.writeEnd();
                }
            }

            if (showTypeLabel) {
                page.writeStart("td");
                    page.writeTypeLabel(item);
                page.writeEnd();
            }

            page.writeStart("td", "data-preview-anchor", "");
                renderBeforeItem(item);
                page.writeObjectLabel(item);
                renderAfterItem(item);
            page.writeEnd();

            if (sortField != null) {
                Object value = itemState.get(sortField.getInternalName());

                page.writeStart("td");
                    page.writeHtml(value instanceof Recordable ?
                            ((Recordable) value).getState().getLabel() :
                            value);
                page.writeEnd();
            }

        page.writeEnd();
    }

    public void renderBeforeItem(Object item) throws IOException {
        page.writeStart("a",
                "href", page.objectUrl("/content/edit.jsp", item, "search", page.url("", Search.NAME_PARAMETER, null)),
                "data-objectId", State.getInstance(item).getId(),
                "target", "_top");
    }

    public void renderAfterItem(Object item) throws IOException {
        page.writeEnd();
    }

    public void renderEmpty() throws IOException {
        page.writeStart("div", "class", "message message-warning");
            page.writeStart("p");
                page.writeHtml("No matching items!");
            page.writeEnd();
        page.writeEnd();
    }
}
