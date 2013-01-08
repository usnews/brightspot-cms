package com.psddev.cms.tool;

import com.psddev.cms.db.Content;
import com.psddev.cms.db.Directory;
import com.psddev.cms.db.ImageTag;

import com.psddev.dari.db.SolrDatabase;
import com.psddev.dari.db.State;
import com.psddev.dari.util.DateUtils;
import com.psddev.dari.util.ImageEditor;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.PaginatedResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class SearchResultRenderer {

    private static final String PREVIOUS_DATE_ATTRIBUTE = SearchResultRenderer.class.getName() + ".previousDate";

    private final ToolPageContext wp;
    private final Search search;

    public SearchResultRenderer(ToolPageContext wp, Search search) {
        this.wp = wp;
        this.search = search;
    }

    public ToolPageContext getToolPageContext() {
        return this.wp;
    }

    public Search getSearch() {
        return this.search;
    }

    public void render() throws IOException {
        if (search.getResult().hasItems()) {
            renderPagination();
            renderList();
        } else {
            renderEmpty();
        }
    }

    protected void renderPagination() throws IOException {

        ToolPageContext wp = getToolPageContext();
        PaginatedResult<?> result = getSearch().getResult();
        wp.write("<ul class=\"pagination\">");

        if (result.hasPrevious()) {
            wp.write("<li classa=\"previous\"><a href=\"");
            wp.write(wp.url("", Search.OFFSET_PARAMETER, result.getPreviousOffset()));
            wp.write("\">Previous ");
            wp.write(result.getLimit());
            wp.write("</a></li>");
        }

        wp.write("<li class=\"label\">");
        wp.write(result.getFirstItemIndex());
        wp.write(" to ");
        wp.write(result.getLastItemIndex());
        wp.write(" of <strong>");
        wp.write(result.getCount());
        wp.write("</strong></li>");

        if (result.hasNext()) {
            wp.write("<li class=\"next\"><a href=\"");
            wp.write(wp.url("", Search.OFFSET_PARAMETER, result.getNextOffset()));
            wp.write("\">Next ");
            wp.write(result.getLimit());
            wp.write("</a></li>");
        }

        wp.write("</ul>");
    }

    protected void renderList() throws IOException {

        ToolPageContext wp = getToolPageContext();
        List<Object> items = new ArrayList<Object>(getSearch().getResult().getItems());
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
            wp.write("<div class=\"images\">");
            for (Map.Entry<Object, StorageItem> e : previews.entrySet()) {
                renderImage(e.getKey(), e.getValue());
            }
            wp.write("</div>");
        }

        if (!items.isEmpty()) {
            wp.write("<table class=\"links table-striped pageThumbnails\"><tbody>");
            for (Object item : items) {
                renderRow(item);
            }
            wp.write("</tbody></table>");
        }
    }

    protected void renderImage(Object item, StorageItem image) throws IOException {
        renderBeforeItem(item);

        ImageEditor editor = ImageEditor.Static.getDefault();
        String url = null;
        if (editor != null) {
            // 80px height
            url = new ImageTag.Builder(image).setHeight(100).toUrl();
        }
        if (url == null) {
            url = image.getPublicUrl();
        }

        wp.write("<figure>");
        wp.write("<img alt=\"");
        wp.write(wp.typeLabel(item));
        wp.write(": ");
        wp.write(wp.objectLabel(item));
        wp.write("\" src=\"");
        wp.write(wp.url(url));
        wp.write("\">");
        wp.write("<figcaption>");
        wp.write(wp.typeLabel(item));
        wp.write(": ");
        wp.write(wp.objectLabel(item));
        wp.write("</figcaption>");
        wp.write("</figure>");

        renderAfterItem(item);
    }

    protected void renderRow(Object item) throws IOException {
        ToolPageContext wp = getToolPageContext();
        Search search = getSearch();
        HttpServletRequest request = wp.getRequest();
        String permalink = State.getInstance(item).as(Directory.ObjectModification.class).getPermalink();

        wp.write("<tr data-preview-url=\"");
        wp.write(wp.h(permalink));
        wp.write("\">");

        if (search.getSort() == SearchSort.NEWEST) {
            Date updateDate = State.getInstance(item).as(Content.ObjectModification.class).getUpdateDate();
            String date = DateUtils.toString(updateDate, "MMM dd, yyyy");
            wp.write("<td>");
            if (!ObjectUtils.equals(date, request.getAttribute(PREVIOUS_DATE_ATTRIBUTE))) {
                wp.write(wp.h(date));
                request.setAttribute(PREVIOUS_DATE_ATTRIBUTE, date);
            }
            wp.write("</td>");
            wp.write("<td>");
            wp.write(wp.h(DateUtils.toString(updateDate, "hh:mm a")));
            wp.write("</td>");
        }

        if (search.getSelectedType() == null
                && search.getValidTypes().size() != 1) {
            wp.write("<td>");
            wp.write(wp.typeLabel(item));
            wp.write("</td>");
        }

        wp.write("<td class=\"main\">");
        renderBeforeItem(item);
        wp.write(wp.objectLabel(item));
        renderAfterItem(item);
        wp.write("</td>");

        wp.write("<td>");
        wp.write(wp.h(permalink));
        wp.write("</td>");

        if (search.getSort() == SearchSort.RELEVANT) {
            Float score = SolrDatabase.Static.getNormalizedScore(item);
            wp.write("<td>");
            if (score != null) {
                wp.write(String.format("%.0f%%", score * 100));
            }
            wp.write("</td>");
        }

        wp.write("</tr>");
    }

    protected void renderBeforeItem(Object item) throws IOException {
        ToolPageContext wp = getToolPageContext();
        wp.write("<a href=\"");
        wp.write(wp.objectUrl("/content/edit.jsp", item));
        wp.write("\" target=\"_top\">");
    }

    protected void renderAfterItem(Object item) throws IOException {
        ToolPageContext wp = getToolPageContext();
        wp.write("</a>");
    }

    protected void renderEmpty() throws IOException {
        ToolPageContext wp = getToolPageContext();
        wp.write("<div class=\"message warning\"><p>No matching items!</p></div>");
    }
}
