<%@ page import="

com.psddev.cms.db.Content,
com.psddev.cms.db.ContentSection,
com.psddev.cms.db.Directory,
com.psddev.cms.db.Draft,
com.psddev.cms.db.DraftStatus,
com.psddev.cms.db.Page,
com.psddev.cms.db.PageFilter,
com.psddev.cms.db.Section,
com.psddev.cms.db.Site,
com.psddev.cms.db.Template,
com.psddev.cms.db.ToolSearch,
com.psddev.cms.db.ToolUi,
com.psddev.cms.db.Workflow,
com.psddev.cms.tool.CmsTool,
com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.tool.Widget,

com.psddev.dari.db.ObjectType,
com.psddev.dari.db.Query,
com.psddev.dari.db.State,
com.psddev.dari.util.DateUtils,
com.psddev.dari.util.HtmlWriter,
com.psddev.dari.util.JspUtils,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.PaginatedResult,

java.io.StringWriter,
java.util.ArrayList,
java.util.List,
java.util.ListIterator,
java.util.Set,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/dashboard")) {
    return;
}

Object selected = wp.findOrReserve();
State state = State.getInstance(selected);

if (selected != null) {
    Site site = wp.getSite();
    if (!(site == null || Site.Static.isObjectAccessible(site, selected))) {
        wp.redirect("/");
        return;
    }
}

Template template = null;
if (selected != null) {
    template = state.as(Template.ObjectModification.class).getDefault();
}
if (template == null) {
    template = Query.findById(
            Template.class, wp.uuidParam("templateId"));
    if (template != null) {
        Set<ObjectType> types = template.getContentTypes();
        if (types != null && types.size() == 1) {
            for (ObjectType type : types) {
                selected = wp.findOrReserve(type.getId());
                state = State.getInstance(selected);
            }
        }
    }
    if (selected != null) {
        state.as(Template.ObjectModification.class).setDefault(template);
    } else {
        wp.redirect("/");
        return;
    }
}

UUID newTypeId = wp.uuidParam("newTypeId");
if (newTypeId != null) {
    state.setTypeId(newTypeId);
}

Object editing = selected;
Object sectionContent = null;
if (selected instanceof Page) {
    sectionContent = Query.findById(Object.class, wp.uuidParam("contentId"));
    if (sectionContent != null) {
        editing = sectionContent;
    }
}

if (wp.include("/WEB-INF/objectDelete.jsp", "object", editing)
        || wp.include("/WEB-INF/objectDraft.jsp", "object", editing)
        || wp.include("/WEB-INF/objectPublish.jsp", "object", editing)) {
    return;
}

Object copy = Query.findById(Object.class, wp.uuidParam("copyId"));
if (copy != null) {
    State editingState = State.getInstance(editing);
    editingState.setValues(State.getInstance(copy).getSimpleValues());
    editingState.setId(null);
}

// Directory directory = Query.findById(Directory.class, wp.uuidParam("directoryId"));
Draft draft = wp.getOverlaidDraft(editing);
Set<ObjectType> compatibleTypes = ToolUi.getCompatibleTypes(State.getInstance(editing).getType());

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<div class="content-edit">
    <form action="<%= wp.objectUrl("", selected) %>" autocomplete="off" class="contentForm" enctype="multipart/form-data" method="post">
        <div class="contentForm-main">

            <%
            ToolSearch search = Query.from(ToolSearch.class).where("_id = ?", wp.uuidParam("searchId")).first();
            if (search != null) {
                String sortFieldName = search.getSortField().getInternalName();
                Object previous = search.toPreviousQuery(state).first();
                Object next = search.toNextQuery(state).first();

                if (previous != null || next != null) {
                    %><ul class="pagination" style="margin-top: -5px;"><%
                        if (previous != null) {
                            %><li class="previous"><a href="<%= wp.url("",
                                    "id", State.getInstance(previous).getId())
                                    %>"><%= wp.objectLabel(previous) %></a></li><%
                        }
                        %><li class="label"><a class="action-search" href="<%= wp.url("/misc/advancedSearch.jsp",
                                "id", search.getId())
                                %>">Search Result</a></li><%
                        if (next != null) {
                            %><li class="next"><a href="<%= wp.url("",
                                    "id", State.getInstance(next).getId())
                                    %>"><%= wp.objectLabel(next) %></a></li><%
                        }
                    %></ul><%
                }
            }
            %>

            <% wp.include("/WEB-INF/objectMessage.jsp", "object", editing); %>

            <div class="widget widget-content">
                <h1><%
                    wp.write(state.isNew() ? "New " : "Edit ");

                    if (compatibleTypes.size() < 2) {
                        wp.write(wp.objectLabel(state.getType()));
                    } else {
                        wp.write("<select name=\"newTypeId\">");
                            for (ObjectType type : compatibleTypes) {
                                wp.write("<option");
                                wp.write(state.getType().equals(type) ? " selected" : "");
                                wp.write(" value=\"");
                                wp.write(type.getId());
                                wp.write("\">");
                                wp.write(wp.objectLabel(type));
                                wp.write("</option>");
                            }
                        wp.write("</select>");
                    }

                    if (selected instanceof Page) {
                        wp.write("<a href=\"");
                        wp.write(wp.returnableUrl("/content/editableSections.jsp"));
                        wp.write("\" target=\"contentPageSections-");
                        wp.write(state.getId());
                        wp.write("\">");
                            if (sectionContent != null) {
                                wp.write(wp.objectLabel(State.getInstance(editing).getType()));
                            } else {
                                wp.write("Layout");
                            }
                        wp.write("</a>");
                    }
                %></h1>

                <% wp.include("/WEB-INF/objectVariation.jsp", "object", editing); %>

                <% if (sectionContent != null) { %>
                    <p><a class="action-back" href="<%= wp.url("", "contentId", null) %>">Back to Layout</a></p>
                <% } %>

                <% wp.include("/WEB-INF/objectForm.jsp", "object", editing); %>
            </div>

            <% renderWidgets(wp, editing, CmsTool.CONTENT_BOTTOM_WIDGET_POSITION); %>
        </div>

        <div class="contentForm-aside">
            <% renderWidgets(wp, editing, CmsTool.CONTENT_RIGHT_WIDGET_POSITION); %>

            <div class="widget widget-publication">
                <h1>Publication</h1>

                <%
                if (wp.hasPermission("type/" + state.getTypeId() + "/write")) {
                    List<Workflow> workflows = Query.from(Workflow.class).select();

                    if (draft != null) {
                        DraftStatus status = draft.getStatus();
                        if (status != null) {

                            wp.write("<div class=\"otherWorkflows\">");
                            wp.write("<p>Status: ");
                            wp.write(wp.objectLabel(status));
                            wp.write("</p>");

                            for (Workflow workflow : workflows) {
                                if (status.equals(workflow.getSource())
                                        && wp.hasPermission("type/" + state.getTypeId() + "/" + workflow.getPermissionId())) {
                                    wp.write("<input name=\"action\" type=\"submit\" value=\"");
                                    wp.write(wp.objectLabel(workflow));
                                    wp.write("\"> ");
                                }
                            }

                            wp.write("</div>");
                        }

                    } else if (!wp.hasPermission("type/" + state.getTypeId() + "/publish")) {
                        wp.write("<div class=\"otherWorkflows\">");
                        wp.write("<input name=\"action\" type=\"submit\" value=\"");
                        for (Workflow workflow : workflows) {
                            if (workflow.getSource() == null) {
                                wp.write(wp.h(workflow.getName()));
                                break;
                            }
                        }
                        wp.write("\">");
                        wp.write("</div>");
                    }

                    if (wp.hasPermission("type/" + state.getTypeId() + "/publish")) {
                        wp.write("<input class=\"date dateInput\" data-emptylabel=\"Now\" id=\"");
                        wp.write(wp.getId());
                        wp.write("\" name=\"publishDate\" size=\"9\" type=\"text\" value=\"");
                        wp.write(draft != null && draft.getSchedule() != null ? DateUtils.toString(draft.getSchedule().getTriggerDate(), "yyyy-MM-dd HH:mm:ss") : "");
                        wp.write("\">");
                        wp.write("<input class=\"action-save\" name=\"action\" type=\"submit\" value=\"Publish\">");
                    }

                    if (!state.isNew() || draft != null) {
                        wp.write("<button class=\"action-delete\" name=\"action\" value=\"Delete\" onclick=\"return confirm('Are you sure you want to delete?');\">Delete</button>");
                    }

                } else {
                    wp.write("<div class=\"warning message\"><p>You cannot edit this ");
                    wp.write(wp.typeLabel(state));
                    wp.write("!</p></div>");
                }
                %>

                <% if (!state.isNew()) { %>
                    <a class="action-tools" href="<%= wp.objectUrl("/content/advanced.jsp", editing) %>" target="contentAdvanced">Advanced</a>
                <% } %>

                <ul class="extraActions">
                    <% if (wp.hasPermission("type/" + state.getTypeId() + "/write")) { %>
                        <li><button class="action-draft" name="action" value="Save Draft">Save Draft</button></li>
                    <% } %>
                    <% if (wp.getCmsTool().isPreviewPopup() && (
                            selected.getClass() == Page.class
                            || Template.Static.findUsedTypes(wp.getSite()).contains(state.getType()))) { %>
                        <li><a class="action-preview" href="<%= wp.objectUrl("/content/preview.jsp", selected) %>" target="contentPreview-<%= state.getId() %>">Preview</a></li>
                    <% } %>
                </ul>
            </div>
        </div>
    </form>
</div>

<% if (!wp.getCmsTool().isPreviewPopup() &&
        (selected.getClass() == Page.class
        || Template.Static.findUsedTypes(wp.getSite()).contains(state.getType()))) { %>
    <div class="content-preview">
        <div class="widget widget-preview">
            <h1>Preview</h1>

            <%
            String previewFormId = wp.createId();
            String previewTarget = wp.createId();
            String modeId = wp.createId();
            %>

            <ul class="widget-preview-controls">
                <li><a class="action-live" href="<%= wp.h(state.as(Directory.ObjectModification.class).getPermalink()) %>" target="_blank">Live Page</a></li>
                <li>
                    <form action="<%= wp.url("/content/sharePreview.jsp") %>" method="post" target="_blank">
                        <input name="<%= PageFilter.PREVIEW_ID_PARAMETER %>" type="hidden" value="<%= state.getId() %>">
                        <input name="<%= PageFilter.PREVIEW_OBJECT_PARAMETER %>" type="hidden">
                        <button class="action-share">Share</button>
                    </form>
                </li>
            </ul>

            <form action="<%= JspUtils.getAbsolutePath(null, request, "/_preview") %>" id="<%= previewFormId %>" method="post" target="<%= previewTarget %>" style="float: right; margin-top: 35px; margin-right: 45px;">
                <input type="hidden" id="<%= modeId %>" name="_" value="true">
                <label for="<%= wp.createId() %>">Mode:</label>
                <select id="<%= wp.getId() %>" onchange="
                        var $select = $(this);
                        $('#<%= modeId %>').attr('name', $select.val());
                        $select.closest('form').submit();">
                    <option value="_">Default</option>
                    <option value="_prod">Production</option>
                    <option value="_debug">Debug</option>
                    <option value="_wireframe">Wireframe</option>
                </select>
                <input name="<%= PageFilter.PREVIEW_ID_PARAMETER %>" type="hidden" value="<%= state.getId() %>">
                <input name="<%= PageFilter.PREVIEW_OBJECT_PARAMETER %>" type="hidden">
            </form>
        </div>
    </div>

    <script type="text/javascript">

    // Make preview peekable.
    $(function() {
        var $win = $(window),
                $edit = $('.content-edit'),
                $preview = $('.content-preview'),
                $previewWidget = $preview.find('.widget-preview'),
                $previewHeading = $preview.find('h1');

        $edit.css({
            'margin-right': $preview.width(),
            'max-width': 1100
        });

        $previewWidget.addClass('loading');
        $preview.show();

        // Preview should be roughly the same width as the window.
        $win.resize($.throttle(500, function() {
            var offset = $edit.offset();

            offset.left += $previewWidget.is('.widget-expanded') ? 30 : $edit.outerWidth() + 10;
            $preview.css(offset);
            $previewWidget.css('width', $win.outerWidth(true) - 30);
        }));

        $win.resize();

        // Make the preview expand/collapse when the heading is clicked.
        $previewHeading.click(function() {
            var editLeft = $edit.offset().left;

            if ($previewWidget.is('.widget-expanded')) {
                $previewWidget.removeClass('widget-expanded');
                $preview.animate({ 'left': editLeft + $edit.outerWidth() + 10 }, 300, 'easeOutBack');
                $preview.css('width', '');

            } else {
                $previewWidget.addClass('widget-expanded');
                $preview.animate({ 'left': editLeft + 30 }, 300, 'easeOutBack');
                $preview.css('width', '100%');
            }
        });

        // Load the preview.
        var $previewForm = $('#<%= previewFormId %>');
        var $contentForm = $('.contentForm');
        var action = location.href;
        var questionAt = action.indexOf('?');
        var oldFormData;

        var loadPreview = $.throttle(2000, function() {
            var newFormData = $contentForm.serialize();

            // If the form inputs haven't changed, try again later.
            if (oldFormData === newFormData) {
                setTimeout(loadPreview, 100);
                return;
            }

            oldFormData = newFormData;
            $previewWidget.addClass('loading');

            // Get the correct JSON from the server.
            $.ajax({
                'data': newFormData,
                'type': 'post',
                'url': CONTEXT_PATH + 'content/state.jsp?id=<%= state.getId() %>&' + (questionAt > -1 ? action.substring(questionAt + 1) : ''),
                'complete': function(request) {

                    // Make sure that the preview IFRAME exists.
                    $(':input[name=<%= PageFilter.PREVIEW_OBJECT_PARAMETER %>]').val(request.responseText);
                    var $previewTarget = $('iframe[name=<%= previewTarget %>]');

                    if ($previewTarget.length === 0) {
                        $previewTarget = $('<iframe/>', {
                            'name': '<%= previewTarget %>',
                            'css': {
                                'border-style': 'none',
                                'height': '1000px',
                                'margin': 0,
                                'overflow': 'hidden',
                                'padding': 0,
                                'width': '100%'
                            }
                        });
                        $previewForm.after($previewTarget);
                    }

                    // Resize IFRAME so that there isn't a scrollbar.
                    var setHeightTimer;
                    var setHeight = function() {
                        if ($previewTarget[0]) {
                            var $body = $($previewTarget[0].contentWindow.document.body);
                            $body.css('overflow', 'hidden');
                            $previewTarget.height(Math.max($edit.outerHeight(true), $body.outerHeight(true)));

                        } else if (setHeightTimer) {
                            clearInterval(setHeightTimer);
                            setHeightTimer = null;
                        }
                    };

                    setHeightTimer = setInterval(setHeight, 100);

                    $previewTarget.load(function() {
                        $previewWidget.removeClass('loading');
                        setHeight();
                        if (setHeightTimer) {
                            clearInterval(setHeightTimer);
                            setHeightTimer = null;
                        }
                    });

                    // Really load the preview.
                    $previewForm.submit();
                    setTimeout(loadPreview, 100);
                }
            });
        });

        loadPreview();
    });
    </script>
<% } %>

<% wp.include("/WEB-INF/footer.jsp"); %><%!

// Renders all the content widgets for the given position.
private static void renderWidgets(ToolPageContext wp, Object object, String position) throws Exception {

    State state = State.getInstance(object);
    List<Widget> widgets = null;
    for (List<Widget> item : wp.getTool().findWidgets(position)) {
        widgets = item;
        break;
    }

    if (!ObjectUtils.isBlank(widgets)) {
        wp.write("<div class=\"contentWidgets contentWidgets-");
        wp.write(wp.h(position));
        wp.write("\">");

        for (Widget widget : widgets) {
            if (wp.hasPermission(widget.getPermissionId())) {

                wp.write("<input type=\"hidden\" name=\"");
                wp.write(wp.h(state.getId()));
                wp.write("/_widget\" value=\"");
                wp.write(wp.h(widget.getId()));
                wp.write("\">");

                String display;
                try {
                    display = widget.display(wp, object);
                } catch (Exception ex) {
                    StringWriter sw = new StringWriter();
                    HtmlWriter hw = new HtmlWriter(sw);
                    hw.putAllStandardDefaults();
                    hw.start("pre", "class", "error message").object(ex).end();
                    display = sw.toString();
                }

                if (!ObjectUtils.isBlank(display)) {
                    wp.write("<div class=\"widget widget-");
                    wp.write(wp.h(widget.getInternalName()));
                    wp.write("\"><h1>");
                    wp.write(wp.objectLabel(widget));
                    wp.write("</h1>");
                    wp.write(display);
                    wp.write("</div>");
                }
            }
        }
        wp.write("</div>");
    }
}
%>
