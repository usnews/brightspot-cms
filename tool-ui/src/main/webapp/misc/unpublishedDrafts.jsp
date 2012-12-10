<%@ page import="

com.psddev.cms.db.Content,
com.psddev.cms.db.Draft,
com.psddev.cms.db.DraftStatus,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.State,
com.psddev.dari.util.HtmlWriter,
com.psddev.dari.util.PaginatedResult
" %><%

final ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/dashboard")) {
    return;
}

new HtmlWriter(out) {{
    PaginatedResult<Draft> drafts = Query.
            from(Draft.class).
            where(Content.UPDATE_DATE_FIELD + " != missing").
            sortDescending(Content.UPDATE_DATE_FIELD).
            select(wp.longParam("offset"), wp.intParam("limit", 20));

    start("div", "class", "widget");
        start("style", "type", "text/css");
            write(".widget-unpublishedDrafts .status {");
                write("background-color: #ee6;");
                write("-moz-border-radius: 5px;");
                write("-webkit-border-radius: 5px;");
                write("border-radius: 5px;");
                write("font-size: 80%;");
                write("padding: 5px;");
                write("text-transform: uppercase;");
            write("}");
        end();

        start("h1", "class", "icon-edit").string("Drafts").end();
        start("div", "class", "widget-unpublishedDrafts");

            if (drafts.hasPrevious() || drafts.hasNext()) {
                start("ul", "class", "pagination");
                    if (drafts.hasPrevious()) {
                        start("li", "class", "first");
                            start("a", "href", wp.url("", "offset", drafts.getFirstOffset()));
                                string("Most Recent");
                            end();
                        end();
                        start("li", "class", "previous");
                            start("a", "href", wp.url("", "offset", drafts.getPreviousOffset()));
                                string("Previous ").string(drafts.getLimit());
                            end();
                        end();
                    }
                    if (drafts.hasNext()) {
                        start("li", "class", "next");
                            start("a", "href", wp.url("", "offset", drafts.getNextOffset()));
                                string("Next ").string(drafts.getLimit());
                            end();
                        end();
                    }
                end();
            }

            start("table", "class", "links table-striped").start("tbody");
                for (Draft draft : drafts.getItems()) {

                    Object object = draft.getObject();
                    if (object == null) {
                        continue;
                    }

                    State objectState = State.getInstance(object);
                    if (!objectState.isNew()) {
                        continue;
                    }

                    start("tr");
                        start("td");
                            DraftStatus status = draft.getStatus();
                            if (status != null) {
                                start("span", "class", "status");
                                    write(wp.objectLabel(status));
                                end();
                            }
                        end();

                        start("td");
                            string(objectState.getType().getLabel());
                        end();

                        start("td", "class", "main");
                            start("a", "href", wp.objectUrl("/content/edit.jsp", draft), "target", "_top");
                                write(wp.objectLabel(object));
                            end();
                        end();

                        start("td");
                            write(wp.objectLabel(draft.as(Content.ObjectModification.class).getPublishUser()));
                        end();
                    end();
                }
            end().end();
        end();
    end();
}};
%>
