package com.psddev.cms.tool.page;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;

import com.psddev.cms.db.Content;
import com.psddev.cms.db.Directory;
import com.psddev.cms.db.ImageTag;
import com.psddev.cms.db.ToolUi;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.Search;
import com.psddev.cms.tool.SearchResultRenderer;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.DatabaseEnvironment;
import com.psddev.dari.db.Metric;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.db.State;
import com.psddev.dari.util.CollectionUtils;
import com.psddev.dari.util.HtmlFormatter;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.ImageEditor;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.UrlBuilder;

@RoutingFilter.Path(application = "cms", value = "searchAdvancedFullResult")
public class SearchAdvancedFullResult extends PageServlet {

    private static final long serialVersionUID = 1L;

    private static final String FIELDS_PARAMETER = "f";
    private static final String ITEMS_PARAMETER = "i";

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        Search search = new Search(page);
        Query<?> query = search.toQuery(page.getSite());
        DatabaseEnvironment environment = Database.Static.getDefault().getEnvironment();
        ObjectType type = search.getSelectedType();
        List<ObjectField> allFields = new ArrayList<ObjectField>();

        for (String fieldName : new String[] {
                "cms.content.publishDate",
                "cms.content.publishUser",
                "cms.content.updateDate",
                "cms.content.updateUser",
                "cms.directory.paths" }) {
            allFields.add(environment.getField(fieldName));
        }

        if (type != null) {
            allFields.addAll(type.getFields());
        }

        List<String> fieldNames = page.params(String.class, FIELDS_PARAMETER);
        List<ObjectField> fields = new ArrayList<ObjectField>();

        for (ObjectField field : allFields) {
            if (fieldNames.contains(field.getInternalName())) {
                fields.add(field);
            }
        }

        Collections.sort(allFields);
        Collections.sort(fields);

        List<UUID> ids = page.params(UUID.class, ITEMS_PARAMETER);

        if (page.param(String.class, "action-download") != null) {
            HttpServletResponse response = page.getResponse();

            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=search-result-" + new DateTime(null, page.getUserDateTimeZone()).toString("yyyy-MM-dd-hh-mm-ss") + ".csv");

            page.putOverride(Recordable.class, new HtmlFormatter<Recordable>() {

                @Override
                public void format(HtmlWriter writer, Recordable object) throws IOException {
                    ToolPageContext page = (ToolPageContext) writer;

                    page.write(page.getObjectLabel(object));
                }
            });

            page.putOverride(Metric.class, new HtmlFormatter<Metric>() {

                @Override
                public void format(HtmlWriter writer, Metric object) throws IOException {
                    writer.write(new Double(object.getSum()).toString());
                }
            });

            page.putOverride(StorageItem.class, new HtmlFormatter<StorageItem>() {

                @Override
                public void format(HtmlWriter writer, StorageItem item) throws IOException {
                    writer.write(item.getPublicUrl());
                }
            });

            page.write('\ufeff');

            page.write("\"");
            writeCsvItem(page, "Type");
            page.write("\",\"");
            writeCsvItem(page, "Label");
            page.write("\"");

            for (ObjectField field : fields) {
                page.write(",\"");
                writeCsvItem(page, field.getDisplayName());
                page.write("\"");
            }

            page.write("\r\n");

            Iterator<?> queryIterator = ids.isEmpty() ?
                    query.iterable(0).iterator() :
                    Query.fromAll().where("_id = ?", ids).selectAll().iterator();

            try {
                while (queryIterator.hasNext()) {
                    Object item = queryIterator.next();
                    State itemState = State.getInstance(item);

                    page.write("\"");
                    writeCsvItem(page, page.getTypeLabel(item));
                    page.write("\",\"");
                    writeCsvItem(page, page.getObjectLabel(item));
                    page.write("\"");

                    for (ObjectField field : fields) {
                        page.write(",\"");

                        if ("cms.directory.paths".equals(field.getInternalName())) {
                            for (Iterator<Directory.Path> i = itemState.as(Directory.ObjectModification.class).getPaths().iterator(); i.hasNext(); ) {
                                Directory.Path p = i.next();
                                String path = p.getPath();

                                page.writeHtml(path);
                                page.writeHtml(" (");
                                page.writeHtml(p.getType());
                                page.writeHtml(")");

                                if (i.hasNext()) {
                                    page.write(", ");
                                }
                            }

                        } else {
                            for (Iterator<Object> i = CollectionUtils.recursiveIterable(itemState.get(field.getInternalName())).iterator(); i.hasNext(); ) {
                                Object value = i.next();
                                page.writeObject(value);
                                if (i.hasNext()) {
                                    page.write(", ");
                                }
                            }
                        }

                        page.write("\"");
                    }

                    page.write("\r\n");
                }

            } finally {
                if (queryIterator instanceof Closeable) {
                    ((Closeable) queryIterator).close();
                }
            }

            return;

        } else if (page.param(String.class, "action-trash") != null) {
            Iterator<?> queryIterator = ids.isEmpty() ?
                    query.iterable(0).iterator() :
                    Query.fromAll().where("_id = ?", ids).selectAll().iterator();

            try {
                while (queryIterator.hasNext()) {
                    page.trash(queryIterator.next());
                }

            } finally {
                if (queryIterator instanceof Closeable) {
                    ((Closeable) queryIterator).close();
                }
            }

            page.getResponse().sendRedirect(page.param(String.class, "returnUrl"));
            return;
        }

        search.setLimit(20);
        search.setSuggestions(false);

        Renderer renderer = new Renderer(page, search, allFields, fields);

        page.putOverride(Recordable.class, new HtmlFormatter<Recordable>() {

            @Override
            public void format(HtmlWriter writer, Recordable object) throws IOException {
                ToolPageContext page = (ToolPageContext) writer;

                page.writeObjectLabel(object);
            }
        });

        page.putOverride(Metric.class, new HtmlFormatter<Metric>() {

            @Override
            public void format(HtmlWriter writer, Metric object) throws IOException {
                writer.write(new Double(object.getSum()).toString());
            }
        });

        page.putOverride(Content.class, new HtmlFormatter<Content>() {

            @Override
            public void format(HtmlWriter writer, Content content) throws IOException {
                ToolPageContext page = (ToolPageContext) writer;

                page.writeStart("a",
                        "href", page.objectUrl("/content/edit.jsp", content),
                        "target", "_top");
                    page.writeObjectLabel(content);
                page.writeEnd();
            }
        });

        page.putOverride(StorageItem.class, new HtmlFormatter<StorageItem>() {

            @Override
            public void format(HtmlWriter writer, StorageItem item) throws IOException {
                ToolPageContext page = (ToolPageContext) writer;

                page.writeElement("img",
                        "height", 100,
                        "src", ImageEditor.Static.getDefault() != null ?
                                new ImageTag.Builder(item).setHeight(100).toUrl() :
                                item.getPublicUrl());
            }
        });

        renderer.render();
    }

    private void writeCsvItem(ToolPageContext page, Object item) throws IOException {
        page.write(item.toString().replaceAll("\"", "\"\""));
    }

    private static class Renderer extends SearchResultRenderer {

        private final ObjectType type;
        private final List<ObjectField> allFields;
        private final List<ObjectField> fields;

        public Renderer(
                ToolPageContext page,
                Search search,
                List<ObjectField> allFields,
                List<ObjectField> fields)
                throws IOException {

            super(page, search);

            this.type = search.getSelectedType();
            this.allFields = allFields;
            this.fields = fields;
        }

        @Override
        public void renderList(Collection<?> items) throws IOException {
            page.writeStart("form",
                    "method", "post",
                    "action", page.url(""));
                page.writeStart("select",
                        "name", FIELDS_PARAMETER,
                        "multiple", "multiple");
                    for (ObjectField field : allFields) {
                        if (field.as(ToolUi.class).isHidden()) {
                            continue;
                        }

                        page.writeStart("option",
                                "selected", fields.contains(field) ? "selected" : null,
                                "value", field.getInternalName());
                            page.writeHtml(field.getDisplayName());
                        page.writeEnd();
                    }
                page.writeEnd();

                page.writeStart("button", "style", "margin-top:5px;");
                    page.writeHtml("Display Fields");
                page.writeEnd();
            page.writeEnd();

            page.writeStart("form",
                    "class", "searchAdvancedResult",
                    "method", "post",
                    "action", page.url(""),
                    "target", "_top");

                page.writeElement("input",
                        "type", "hidden",
                        "name", "returnUrl",
                        "value", new UrlBuilder(page.getRequest()).
                                absolutePath(page.cmsUrl("/searchAdvancedFull")).
                                currentParameters());

                for (ObjectField field : fields) {
                    page.writeElement("input", "type", "hidden", "name", FIELDS_PARAMETER, "value", field.getInternalName());
                }

                page.writeStart("table", "class", "table-bordered table-striped pageThumbnails");
                    page.writeStart("thead");
                        page.writeStart("tr");
                            page.writeStart("th");
                            page.writeEnd();

                            page.writeStart("th");
                                page.writeHtml("Type");
                            page.writeEnd();

                            page.writeStart("th");
                                page.writeHtml("Label");
                            page.writeEnd();

                            for (ObjectField field : fields) {
                                page.writeStart("th");
                                    page.writeHtml(field.getDisplayName());
                                page.writeEnd();
                            }
                        page.writeEnd();
                    page.writeEnd();

                    page.writeStart("tbody");
                        for (Object item : items) {
                            renderRow(item);
                        }
                    page.writeEnd();
                page.writeEnd();

                page.writeStart("div", "class", "actions");
                    page.writeStart("button",
                            "class", "action icon icon-action-download",
                            "name", "action-download",
                            "value", true);
                        page.writeHtml("Export All");
                    page.writeEnd();

                    if (type != null) {
                        page.writeStart("button",
                                "class", "action icon icon-action-edit",
                                "formaction", page.cmsUrl("/contentEditBulk"));
                            page.writeHtml("Bulk Edit All");
                        page.writeEnd();
                    }

                    page.writeStart("a",
                            "class", "action button icon icon-object-workStream",
                            "target", "workStreamCreate",
                            "href", page.cmsUrl("/content/newWorkStream.jsp",
                                    "search", ObjectUtils.toJson(search.getState().getSimpleValues())));
                        page.writeHtml("New Work Stream");
                    page.writeEnd();

                    page.writeStart("button",
                            "class", "action action-pullRight link icon icon-action-trash",
                            "name", "action-trash",
                            "value", "true");
                        page.writeHtml("Bulk Trash All");
                    page.writeEnd();
                page.writeEnd();
            page.writeEnd();
        }

        @Override
        public void renderRow(Object item) throws IOException {
            State itemState = State.getInstance(item);
            String permalink = itemState.as(Directory.ObjectModification.class).getPermalink();

            page.writeStart("tr", "data-preview-url", permalink);
                page.writeStart("td", "style", "width: 20px;");
                    page.writeElement("input",
                            "type", "checkbox",
                            "name", ITEMS_PARAMETER,
                            "value", itemState.getId());
                page.writeEnd();

                page.writeStart("td");
                    page.writeHtml(page.getTypeLabel(item));
                page.writeEnd();

                page.writeStart("td", "data-preview-anchor", "");
                    page.writeStart("a",
                            "href", page.objectUrl("/content/edit.jsp", item),
                            "target", "_top");
                        page.writeObjectLabel(item);
                    page.writeEnd();
                page.writeEnd();

                for (ObjectField field : fields) {
                    page.writeStart("td");
                        if ("cms.directory.paths".equals(field.getInternalName())) {
                            for (Directory.Path p : itemState.as(Directory.ObjectModification.class).getPaths()) {
                                String path = p.getPath();

                                page.writeStart("div");
                                    page.writeStart("a", "href", path, "target", "_blank");
                                        page.writeHtml(path);
                                    page.writeEnd();

                                    page.writeHtml(" (");
                                    page.writeHtml(p.getType());
                                    page.writeHtml(")");
                                page.writeEnd();
                            }

                        } else {
                            for (Iterator<Object> i = CollectionUtils.recursiveIterable(itemState.get(field.getInternalName())).iterator(); i.hasNext(); ) {
                                Object value = i.next();

                                page.writeObject(value);

                                if (i.hasNext()) {
                                    page.writeHtml(", ");
                                }
                            }
                        }
                    page.writeEnd();
                }
            page.writeEnd();
        }
    }
}
