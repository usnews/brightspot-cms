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
    <form action="<%= wp.objectUrl("", selected) %>" autocomplete="off" class="contentForm" data-widths="1500" enctype="multipart/form-data" method="post">
        <div class="main" data-widths="600">

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
                        %><li class="label"><a class="icon-magnifier" href="<%= wp.url("/misc/advancedSearch.jsp",
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

            <div class="widget">
                <h1 class="icon-page">
                    <%= state.isNew() ? "New " : "Edit " %>

                    <% if (compatibleTypes.size() < 2) {
                        %><%= wp.objectLabel(state.getType()) %><%
                    } else {
                        %><select name="newTypeId">
                            <% for (ObjectType type : compatibleTypes) { %>
                                <option<%= state.getType().equals(type) ? " selected" : "" %> value="<%= type.getId() %>"><%= wp.objectLabel(type) %></option>
                            <% } %>
                        </select><%
                    }

                    if (selected instanceof Page) {
                        %>:
                        <a href="<%= wp.returnableUrl("/content/editableSections.jsp") %>" target="contentPageSections-<%= state.getId() %>">
                            <% if (sectionContent != null) { %>
                                <%= wp.objectLabel(State.getInstance(editing).getType()) %>
                            <% } else { %>
                                Layout
                            <% } %>
                        </a>
                    <% } %>
                </h1>

                <% wp.include("/WEB-INF/objectVariation.jsp", "object", editing); %>

                <% if (sectionContent != null) { %>
                    <p><a href="<%= wp.url("", "contentId", null) %>">&larr; Back to Layout</a></p>
                <% } %>

                <% wp.include("/WEB-INF/objectForm.jsp", "object", editing); %>
            </div>

            <% renderWidgets(wp, editing, CmsTool.CONTENT_BOTTOM_WIDGET_POSITION); %>
        </div>

        <div class="aside">
            <% renderWidgets(wp, editing, CmsTool.CONTENT_RIGHT_WIDGET_POSITION); %>

            <div class="widget widget-publication">
                <h1 class="icon-tick">Publication</h1>

                <%
                if (wp.hasPermission("type/" + state.getTypeId() + "/write")) {

                    List<Workflow> workflows = Query.from(Workflow.class).select();
                    if (wp.hasPermission("type/" + state.getTypeId() + "/publish")) {
                        wp.write("<input class=\"date dateInput\" data-emptylabel=\"Now\" id=\"");
                        wp.write(wp.getId());
                        wp.write("\" name=\"publishDate\" size=\"9\" type=\"text\" value=\"");
                        wp.write(draft != null && draft.getSchedule() != null ? DateUtils.toString(draft.getSchedule().getTriggerDate(), "yyyy-MM-dd HH:mm:ss") : "");
                        wp.write("\">");
                        wp.write("<input class=\"saveButton\" name=\"action\" type=\"submit\" value=\"Publish\">");
                    }

                    wp.write("<div class=\"otherWorkflows\">");
                    if (draft != null) {
                        DraftStatus status = draft.getStatus();
                        if (status != null) {

                            wp.write("<p>Current Status: ");
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
                        }

                    } else if (!wp.hasPermission("type/" + state.getTypeId() + "/publish")) {
                        wp.write("<input name=\"action\" type=\"submit\" value=\"");
                        for (Workflow workflow : workflows) {
                            if (workflow.getSource() == null) {
                                wp.write(wp.h(workflow.getName()));
                                break;
                            }
                        }
                        wp.write("\">");
                    }
                    wp.write("</div>");

                    if (!state.isNew() || draft != null) {
                        wp.write("<input class=\"link deleteButton\" name=\"action\" type=\"submit\" value=\"Delete\">");
                    }

                } else {
                    wp.write("<div class=\"warning message\"><p>You cannot edit this ");
                    wp.write(wp.typeLabel(state));
                    wp.write("!</p></div>");
                }
                %>

                <% if (!state.isNew()) { %>
                    <a class="advancedButton icon-wrench" href="<%= wp.objectUrl("/content/advanced.jsp", editing) %>" target="contentAdvanced">&#9660;</a>
                <% } %>

                <ul class="piped extraActions">
                    <% if (wp.hasPermission("type/" + state.getTypeId() + "/write")) { %>
                        <li><input class="icon-page_save link" name="action" type="submit" value="Save Draft"></li>
                    <% } %>
                </ul>
            </div>
        </div>
    </form>
</div>

<% if (selected.getClass() == Page.class
        || Template.Static.findUsedTypes(wp.getSite()).contains(state.getType())) { %>
    <style type="text/css">
        .content-preview {
            display: none;
            position: absolute;
            overflow: hidden;
            padding: 10px 0 5px 5px;
            right: 0;
            z-index: 5;
        }
        .content-preview.loading h1:before {
            content: url(../style/icon/ajax-loader.gif);
        }
        .content-preview h1 {
            cursor: pointer;
            position: fixed;
            width: 100%;
        }
        .content-preview .widget {
            width: 970px;
        }
        .content-preview:before {
            content: '\00ab';
            font-size: 25px;
            line-height: 1;
            margin-top: 3px;
            position: fixed;
            right: 20px;
            z-index: 1;
        }
        .content-preview.expanded:before {
            content: '\00bb';
        }
        .content-preview .controls {
            margin-top: 35px;
        }
        .content-preview .controls li form {
            display: inline;
        }
    </style>

    <div class="content-preview">
        <div class="widget" style="overflow: auto;">
            <h1 class="icon-page_white_find">Preview</h1>

            <%
            String previewFormId = wp.createId();
            String previewTarget = wp.createId();
            %>

            <ul class="piped controls" style="float: left;">
                <li><a href="<%= wp.h(state.as(Directory.ObjectModification.class).getPermalink()) %>" target="_blank">Live Page</a></li>
                <li>
                    <form action="<%= wp.url("/content/sharePreview.jsp") %>" method="post" target="_blank">
                        <input name="<%= PageFilter.PREVIEW_ID_PARAMETER %>" type="hidden" value="<%= state.getId() %>">
                        <input name="<%= PageFilter.PREVIEW_OBJECT_PARAMETER %>" type="hidden">
                        <input class="link" type="submit" value="Share">
                    </form>
                </li>
            </ul>

            <form action="<%= JspUtils.getAbsolutePath(null, request, "/_preview") %>" id="<%= previewFormId %>" method="post" target="<%= previewTarget %>">
                <ul class="piped" style="float: right; margin-top: 35px; margin-right: 45px;">
                    <li>
                        <label for="<%= wp.createId() %>">Debug?</label>
                        <input id="<%= wp.getId() %>" type="checkbox" name="_debug" value="true" onchange="$(this).closest('form').submit();">
                    </li>
                </ul>
                <input name="<%= PageFilter.PREVIEW_ID_PARAMETER %>" type="hidden" value="<%= state.getId() %>">
                <input name="<%= PageFilter.PREVIEW_OBJECT_PARAMETER %>" type="hidden">
            </form>
        </div>
    </div>

    <script type="text/javascript">
    $(function() {
        var peekWidth = 150;

        // Make preview peekable.
        var $window = $(window);
        var $edit = $('.content-edit');
        var $preview = $('.content-preview');
        var $previewHeading = $preview.find('h1');
        var $previewWidget = $preview.find('.widget');

        $edit.css({
            'margin-right': peekWidth,
            'max-width': 1100
        });

        $preview.addClass('loading');
        $preview.show();

        // Make the preview expand/collapse when the heading is clicked.
        var oldPreviewWidgetWidth;

        $previewHeading.live('click', function() {
            var editOffsetLeft = $edit.offset().left;
            var bodyWidth = $(document.body).width();

            if ($preview.is('.expanded')) {
                $preview.removeClass('expanded');

                $preview.animate({
                    'left': editOffsetLeft + $edit.outerWidth() + 10
                }, 300, 'easeOutBack');

                $previewWidget.css({
                    'width': oldPreviewWidgetWidth
                });

            } else {
                $preview.addClass('expanded');

                $preview.animate({
                    'left': editOffsetLeft + 30,
                }, 300, 'easeOutBack');

                oldPreviewWidgetWidth = $previewWidget.width();
                $previewWidget.css({
                    'width': bodyWidth
                });
            }
        });

        // Make sure that the preview is correctly positioned and sized.
        var resizePreview = $.throttle(500, function() {
            var editOffset = $edit.offset();
            var bodyWidth = $(document.body).width();

            if ($preview.is('.expanded')) {
                $preview.css({
                    'left': editOffset.left + 30,
                    'top': editOffset.top
                });

                oldPreviewWidgetWidth = $previewWidget.width();
                $previewWidget.css({
                    'width': bodyWidth
                });

            } else {
                $preview.css({
                    'left': editOffset.left + $edit.outerWidth() + 10,
                    'top': editOffset.top
                });

                $previewWidget.css({
                    'width': oldPreviewWidgetWidth
                });

            }
        });

        resizePreview();
        $window.resize(resizePreview);

        // Load the preview.
        var $previewForm = $('#<%= previewFormId %>');
        var $contentForm = $('.contentForm');
        var action = $contentForm.attr('action');
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
            $preview.addClass('loading');

            // Get the correct JSON from the server.
            $.ajax({
                'data': newFormData,
                'type': 'post',
                'url': CONTEXT_PATH + 'content/state.jsp' + (questionAt > -1 ? action.substring(questionAt) : ''),
                'complete': function(request) {

                    // Make sure that the preview IFRAME exists.
                    $(':input[name=<%= PageFilter.PREVIEW_OBJECT_PARAMETER %>]').val(request.responseText);
                    var $previewTarget = $('iframe[name=<%= previewTarget %>]');

                    if ($previewTarget.length == 0) {
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
                        $preview.removeClass('loading');
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

<script type="text/javascript">
$(function() {
    var $publicationWidget = $('.widget-publication');

    // Change save button label if scheduling.
    var $dateInput = $publicationWidget.find('.dateInput');
    var $saveButton = $publicationWidget.find('.saveButton');
    var oldSaveButton = $saveButton.val();
    var oldDate = $dateInput.val();
    var changeLabel = function() {
        $saveButton.val($dateInput.val() ? (oldDate ? 'Reschedule' : 'Schedule') : oldSaveButton);
    };

    changeLabel();
    $dateInput.change(changeLabel);

    // Move the publication area to the top if within aside section.
    var $aside = $publicationWidget.closest('.aside');

    if ($aside.length > 0) {
        var asideTop = $aside.offset().top;

        var positionPublicationWidget = $.throttle(500, function() {
            var asideLeft = $aside.offset().left;
            var width = $publicationWidget.width();

            $publicationWidget.css({
                'left': asideLeft,
                'margin-bottom': $aside.find('.area').css('margin-bottom'),
                'position': 'fixed',
                'top': asideTop,
                'width': width
            });

            // Push other areas down.
            $aside.css('margin-top', $publicationWidget.outerHeight(true));
        });

        positionPublicationWidget();
        $(window).resize(positionPublicationWidget);
    }
});
</script>

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
                    wp.write("<div class=\"widget\"><h1");
                    String iconName = widget.getIconName();
                    if (!ObjectUtils.isBlank(iconName)) {
                        wp.write(" class=\"icon-");
                        wp.write(iconName);
                        wp.write("\"");
                    }
                    wp.write(">");
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
