<%@ page import="

com.psddev.cms.db.Content,
com.psddev.cms.db.ContentSection,
com.psddev.cms.db.Directory,
com.psddev.cms.db.Draft,
com.psddev.cms.db.DraftStatus,
com.psddev.cms.db.Guide,
com.psddev.cms.db.GuidePage,
com.psddev.cms.db.History,
com.psddev.cms.db.Page,
com.psddev.cms.db.PageFilter,
com.psddev.cms.db.Schedule,
com.psddev.cms.db.Section,
com.psddev.cms.db.Site,
com.psddev.cms.db.Template,
com.psddev.cms.db.ToolSearch,
com.psddev.cms.db.ToolUi,
com.psddev.cms.db.ToolUser,
com.psddev.cms.db.Variation,
com.psddev.cms.db.Workflow,
com.psddev.cms.db.WorkStream,
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
com.psddev.dari.util.StringUtils,

java.io.StringWriter,
java.util.ArrayList,
java.util.LinkedHashSet,
java.util.List,
java.util.ListIterator,
java.util.Map,
java.util.Set,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requireUser()) {
    return;
}

Object selected = wp.findOrReserve();
if (selected == null) {
    wp.redirect("/");
    return;
}

State state = State.getInstance(selected);

if (selected != null) {
    Site site = wp.getSite();
    if (!(site == null || Site.Static.isObjectAccessible(site, selected))) {
        wp.redirect("/");
        return;
    }
}

UUID variationId = wp.param(UUID.class, ToolPageContext.VARIATION_ID_PARAMETER);

if (variationId == null) {
    Site site = wp.getSite();

    if (site != null) {
        Variation defaultVariation = site.getDefaultVariation();

        if (defaultVariation != null) {
            wp.redirect("", "variationId", defaultVariation.getId());
            return;
        }
    }
}

Template template = null;
if (selected != null) {
    template = state.as(Template.ObjectModification.class).getDefault();
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

        if (variationId != null) {
            State editingState = State.getInstance(editing);
            @SuppressWarnings("unchecked")
            Map<String, Object> variationValues = (Map<String, Object>) editingState.getValue("variations/" + variationId.toString());

            if (variationValues != null) {
                editingState.setValues(variationValues);
            }
        }
    }
}

WorkStream workStream = Query.from(WorkStream.class).where("_id = ?", wp.param(UUID.class, "workStreamId")).first();

if (workStream != null) {
    if (wp.param(boolean.class, "action-skipWorkStream")) {
        workStream.skip(wp.getUser(), editing);
        wp.redirect("", "action-skipWorkStream", null);
        return;

    } else if (wp.param(boolean.class, "action-stopWorkStream")) {
        workStream.stop(wp.getUser());
        wp.redirect("/");
        return;
    }

    State.getInstance(editing).as(WorkStream.Data.class).complete(workStream, wp.getUser());
}

if (wp.tryDelete(editing) ||
        wp.tryDraft(editing) ||
        wp.tryPublish(editing) ||
        wp.tryRestore(editing) ||
        wp.tryTrash(editing) ||
        wp.tryWorkflow(editing)) {
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
    <form class="contentForm"
            method="post"
            enctype="multipart/form-data"
            action="<%= wp.objectUrl("", selected) %>"
            autocomplete="off"
            data-object-id="<%= State.getInstance(selected).getId() %>">
        <div class="contentForm-main">
            <div class="widget widget-content">
                <h1 class="breadcrumbs"><%
                    String search = wp.param(String.class, "search");

                    if (search != null) {
                        wp.writeStart("span", "class", "breadcrumbItem frame");
                            wp.writeStart("a", "href", StringUtils.addQueryParameters(search, "widget", true));
                                wp.writeHtml("Search Result");
                            wp.writeEnd();
                        wp.writeEnd();
                    }

                    wp.writeStart("span", "class", "breadcrumbItem icon icon-object");
                        wp.writeHtml(state.isNew() ? "New " : "Edit ");

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
                    wp.writeEnd();

                    if (selected instanceof Page) {
                        wp.writeStart("span", "class", "breadcrumbItem");
                            wp.write("<a class=\"icon icon-object-template\" href=\"");
                            wp.write(wp.returnableUrl("/content/editableSections.jsp", "id", State.getInstance(selected).getId()));
                            wp.write("\" target=\"contentPageSections-");
                            wp.write(state.getId());
                            wp.write("\">");
                                if (sectionContent != null) {
                                    wp.write(wp.objectLabel(State.getInstance(editing).getType()));
                                } else {
                                    wp.write("Layout");
                                }
                            wp.write("</a>");
                        wp.writeEnd();
                    }

                    wp.include("/WEB-INF/objectVariation.jsp", "object", editing);
                %></h1>

                <div class="widgetControls">
                    <%
                    GuidePage guide = Guide.Static.getPageProductionGuide(template);
                    if (guide != null && guide.getDescription() != null && !guide.getDescription().isEmpty()) {
                        wp.write("<a class=\"icon icon-object-guide\" target=\"guideType\" href=\"", wp.objectUrl("/content/guideType.jsp", selected, "templateId", template.getId(), "variationId", wp.uuidParam("variationId"), "popup", true), "\">PG</a>");
                    }
                    %>
                </div>

                <% if (!State.getInstance(editing).isNew()) { %>
                    <div class="widget-contentCreate">
                        <div class="action action-create">New</div>
                        <ul>
                            <li><a class="action action-create" href="<%= wp.url("/content/edit.jsp",
                                    "typeId", State.getInstance(editing).getTypeId(),
                                    "templateId", template != null ? template.getId() : null)
                                    %>">New <%= wp.typeLabel(editing) %></a></li>
                            <li><a class="action action-copy" href="<%= wp.url("/content/edit.jsp",
                                    "typeId", State.getInstance(editing).getTypeId(),
                                    "templateId", template != null ? template.getId() : null,
                                    "copyId", State.getInstance(editing).getId())
                                    %>" target="_top">Copy This <%= wp.typeLabel(editing) %></a></li>

                        </ul>
                    </div>
                <% } %>

                <% if (sectionContent != null) { %>
                    <p><a class="icon icon-arrow-left" href="<%= wp.url("", "contentId", null) %>">Back to Layout</a></p>
                <% } %>

                <% wp.include("/WEB-INF/objectMessage.jsp", "object", editing); %>
                <% wp.include("/WEB-INF/objectForm.jsp", "object", editing); %>
            </div>

            <% renderWidgets(wp, editing, CmsTool.CONTENT_BOTTOM_WIDGET_POSITION); %>
        </div>

        <div class="contentForm-aside">
            <% renderWidgets(wp, editing, CmsTool.CONTENT_RIGHT_WIDGET_POSITION); %>

            <div class="widget widget-publishing">
                <h1 class="icon icon-action-publish">Publishing</h1>

                <%
                State editingState = State.getInstance(editing);

                if (!editingState.isNew()) {
                    wp.writeStart("a",
                            "class", "icon icon-wrench icon-only",
                            "href", wp.objectUrl("/content/advanced.jsp", editing),
                            "target", "contentAdvanced");
                        wp.writeHtml("Advanced");
                    wp.writeEnd();
                }

                if (workStream != null) {
                    long incomplete = workStream.countIncomplete();
                    long total = workStream.getQuery().count();

                    wp.writeStart("div",
                            "class", "block",
                            "style", wp.cssString(
                                    "border-bottom", "1px solid #bbb",
                                    "padding-bottom", "5px"));
                        wp.writeStart("a",
                                "href", wp.url("/workStreamUsers", "id", workStream.getId()),
                                "target", "workStream");
                            wp.writeHtml(workStream.getUsers().size() - 1);
                            wp.writeHtml(" others");
                        wp.writeEnd();

                        wp.writeHtml(" working on ");

                        wp.writeStart("a",
                                "href", wp.objectUrl("/content/workStreamEdit.jsp", workStream),
                                "target", "workStream");
                            wp.writeObjectLabel(workStream);
                        wp.writeEnd();

                        wp.writeHtml(" with you");

                        wp.writeStart("div", "class", "progress", "style", "margin: 5px 0;");
                            wp.writeStart("div", "class", "progressBar", "style", "width:" + ((total - incomplete) * 100.0 / total) + "%");
                            wp.writeEnd();

                            wp.writeStart("strong");
                                wp.writeHtml(incomplete);
                            wp.writeEnd();

                            wp.writeHtml(" of ");

                            wp.writeStart("strong");
                                wp.writeHtml(total);
                            wp.writeEnd();

                            wp.writeHtml(" left");
                        wp.writeEnd();

                        wp.writeStart("ul", "class", "piped");
                            wp.writeStart("li");
                                wp.writeStart("a",
                                        "class", "icon icon-step-forward",
                                        "href", wp.url("", "action-skipWorkStream", "true"));
                                    wp.writeHtml("Skip");
                                wp.writeEnd();
                            wp.writeEnd();

                            wp.writeStart("li");
                                wp.writeStart("a",
                                        "class", "icon icon-stop",
                                        "href", wp.url("", "action-stopWorkStream", "true"));
                                    wp.writeHtml("Stop");
                                wp.writeEnd();
                            wp.writeEnd();
                        wp.writeEnd();
                    wp.writeEnd();
                }

                boolean isWritable = wp.hasPermission("type/" + editingState.getTypeId() + "/write");
                Content.ObjectModification contentData = State.getInstance(editing).as(Content.ObjectModification.class);
                boolean isDraft = contentData.isDraft() || draft != null;
                History history = wp.getOverlaidHistory(editing);
                boolean isHistory = history != null;
                boolean isTrash = contentData.isTrash();

                if (isWritable) {

                    // Workflow actions.
                    if (editingState.isNew() ||
                            editingState.as(Workflow.Data.class).getCurrentState() != null) {
                        Workflow workflow = Query.from(Workflow.class).where("contentTypes = ?", editingState.getType()).first();

                        if (workflow != null) {
                            Workflow.Data workflowData = editingState.as(Workflow.Data.class);
                            String currentState = workflowData.getCurrentState();
                            Set<String> transitionNames = new LinkedHashSet<String>();

                            for (String transitionName : workflow.getTransitionsFrom(currentState).keySet()) {
                                if (wp.hasPermission("type/" + editingState.getTypeId() + "/" + transitionName)) {
                                    transitionNames.add(transitionName);
                                }
                            }

                            if (currentState != null || !transitionNames.isEmpty()) {
                                wp.writeStart("div", "class", "message message-info");
                                    wp.writeStart("p");
                                        wp.writeHtml("Workflow: ");
                                        wp.writeHtml(currentState);
                                    wp.writeEnd();

                                    wp.writeStart("div", "class", "actions");
                                        Workflow.Log log = workflowData.getLastLog();

                                        if (log != null) {
                                            String comment = log.getComment();

                                            wp.writeStart("p");
                                                if (ObjectUtils.isBlank(comment)) {
                                                    wp.writeHtml(log.getTransition());
                                                    wp.writeHtml(" by ");

                                                } else {
                                                    wp.writeStart("q");
                                                        wp.writeHtml(comment);
                                                    wp.writeEnd();
                                                    wp.writeHtml(" said ");
                                                }

                                                wp.writeObjectLabel(log.getUser());
                                                wp.writeHtml(" at ");
                                                wp.writeHtml(wp.formatUserDateTime(log.getDate()));
                                            wp.writeEnd();
                                        }

                                        if (!transitionNames.isEmpty()) {
                                            wp.writeStart("textarea",
                                                    "name", "workflowComment",
                                                    "placeholder", "Optional Comment");
                                            wp.writeEnd();

                                            for (String transitionName : transitionNames) {
                                                wp.writeStart("button",
                                                        "name", "action-workflow",
                                                        "value", transitionName);
                                                    wp.writeHtml(transitionName);
                                                wp.writeEnd();
                                            }
                                        }
                                    wp.writeEnd();
                                wp.writeEnd();
                            }
                        }
                    }

                    // Message and actions if the content is a draft.
                    if (isDraft) {
                        Content.ObjectModification draftContentData = State.
                                getInstance(draft != null ? draft : editing).
                                as(Content.ObjectModification.class);

                        wp.writeStart("div", "class", "message message-warning");
                            wp.writeStart("p");
                                wp.writeHtml("Draft last saved ");
                                wp.writeHtml(wp.formatUserDateTime(draftContentData.getUpdateDate()));
                                wp.writeHtml(" by ");
                                wp.writeObjectLabel(draftContentData.getUpdateUser());
                                wp.writeHtml(".");
                            wp.writeEnd();

                            wp.writeStart("div", "class", "actions");
                                wp.writeStart("a",
                                        "class", "icon icon-action-edit",
                                        "href", wp.url("", "draftId", null));
                                    wp.writeHtml("Current");
                                wp.writeEnd();

                                wp.writeStart("button",
                                        "class", "link icon icon-action-save",
                                        "name", "action-draft",
                                        "value", "true");
                                    wp.writeHtml("Save");
                                wp.writeEnd();

                                wp.writeStart("button",
                                        "class", "link icon icon-action-delete",
                                        "name", "action-delete",
                                        "value", "true");
                                    wp.writeHtml("Delete");
                                wp.writeEnd();
                            wp.writeEnd();
                        wp.writeEnd();

                    // Message and actions if the content is a past revision.
                    } else if (isHistory) {
                        wp.writeStart("div", "class", "message message-warning");
                            wp.writeStart("p");
                                wp.writeHtml("Past revision saved ");
                                wp.writeHtml(wp.formatUserDateTime(history.getUpdateDate()));
                                wp.writeHtml(" by ");
                                wp.writeObjectLabel(history.getUpdateUser());
                                wp.writeHtml(".");
                            wp.writeEnd();

                            wp.writeStart("div", "class", "actions");
                                wp.writeStart("a",
                                        "class", "icon icon-action-edit",
                                        "href", wp.url("", "historyId", null));
                                    wp.writeHtml("Current");
                                wp.writeEnd();

                                wp.writeHtml(" ");

                                wp.writeStart("a",
                                        "class", "icon icon-object-history",
                                        "href", wp.url("/historyEdit", "id", history.getId()),
                                        "target", "historyEdit");
                                    wp.writeHtml("Name Revision");
                                wp.writeEnd();
                            wp.writeEnd();
                        wp.writeEnd();

                    // Message and actions if the content is a trash.
                    } else if (isTrash) {
                        wp.writeTrashMessage(editing);
                    }

                    // Publish and trash buttons.
                    if (!isTrash && wp.hasPermission("type/" + editingState.getId() + "/publish")) {
                        wp.writeStart("div", "class", "widget-publishingPublish");
                            if (wp.getUser().getCurrentSchedule() == null) {
                                wp.writeTag("input",
                                        "type", "text",
                                        "class", "date dateInput",
                                        "data-emptylabel", "Now",
                                        "name", "publishDate",
                                        "size", 9,
                                        "value", draft != null && draft.getSchedule() != null ?
                                                DateUtils.toString(draft.getSchedule().getTriggerDate(), "yyyy-MM-dd HH:mm:ss") :
                                                wp.dateParam("publishDate") != null ? DateUtils.toString(wp.dateParam("publishDate"), "yyyy-MM-dd HH:mm:ss") : "");
                            }

                            wp.writeStart("button",
                                    "name", "action-publish",
                                    "value", "true");
                                wp.writeHtml("Publish");
                            wp.writeEnd();

                            if (!isDraft && !isHistory && !editingState.isNew()) {
                                wp.writeStart("button",
                                        "class", "link icon icon-action-trash",
                                        "name", "action-trash",
                                        "value", "true");
                                    wp.writeHtml("Trash");
                                wp.writeEnd();
                            }
                        wp.writeEnd();
                    }

                } else {
                    wp.write("<div class=\"message message-warning\"><p>You cannot edit this ");
                    wp.write(wp.typeLabel(state));
                    wp.write("!</p></div>");
                }

                wp.writeStart("ul", "class", "widget-publishingExtra");
                    if (isWritable && !isDraft && !isTrash) {
                        wp.writeStart("li");
                            wp.writeStart("button",
                                    "class", "link icon icon-object-draft",
                                    "name", "action-draft",
                                    "value", "true");
                                wp.writeHtml("Save Draft");
                            wp.writeEnd();
                        wp.writeEnd();
                    }
                wp.writeEnd();
                %>
            </div>
        </div>
    </form>
</div>

<% if (wp.isPreviewable(selected)) { %>
    <div class="contentPreview">
        <div class="widget widget-preview">
            <h1>Preview</h1>

            <%
            Site site = wp.getSite();
            String previewFormId = wp.createId();
            String previewTarget = wp.createId();
            String modeId = wp.createId();
            %>

            <ul class="widget-preview_controls">
                <li><a class="action-live" href="<%= wp.h(state.as(Directory.ObjectModification.class).getPermalink()) %>" target="_blank">Live Page</a></li>
                <li>
                    <form action="<%= wp.url("/content/sharePreview.jsp") %>" method="post" target="_blank">
                        <input name="<%= PageFilter.PREVIEW_ID_PARAMETER %>" type="hidden" value="<%= state.getId() %>">
                        <% if (site != null) { %>
                            <input name="<%= PageFilter.PREVIEW_SITE_ID_PARAMETER %>" type="hidden" value="<%= site.getId() %>">
                        <% } %>
                        <input name="<%= PageFilter.PREVIEW_OBJECT_PARAMETER %>" type="hidden">
                        <button class="action-share">Share</button>
                    </form>
                </li>
                <li>
                    <form
                            id="<%= previewFormId %>"
                            method="post"
                            action="<%= JspUtils.getAbsolutePath(null, request, "/_preview") %>"
                            target="<%= previewTarget %>">
                        <input type="hidden" name="_fields" value="true">
                        <input type="hidden" id="<%= modeId %>" name="_" value="true">
                        <label for="<%= wp.createId() %>">Mode:</label>
                        <select id="<%= wp.getId() %>" onchange="
                                var $select = $(this);
                                $('#<%= modeId %>').attr('name', $select.val());
                                $select.closest('form').submit();">
                            <option value="_">Default</option>
                            <option value="_prod">Production</option>
                            <option value="_debug">Debug</option>
                        </select>
                        <input name="<%= PageFilter.PREVIEW_ID_PARAMETER %>" type="hidden" value="<%= state.getId() %>">
                        <% if (site != null) { %>
                            <input name="<%= PageFilter.PREVIEW_SITE_ID_PARAMETER %>" type="hidden" value="<%= site.getId() %>">
                        <% } %>
                        <input name="<%= PageFilter.PREVIEW_OBJECT_PARAMETER %>" type="hidden">
                    </form>
                </li>
            </ul>
        </div>
    </div>

    <script type="text/javascript">
        (function($, win, undef) {
            var PEEK_WIDTH = 160,
                    $win = $(win),
                    doc = win.document,
                    $doc = $(doc),
                    $body = $(doc.body),

                    $edit = $('.content-edit'),
                    oldEditStyle = $edit.attr('style') || '',
                    $publishingExtra = $('.widget-publishingExtra'),
                    $previewAction,
                    appendPreviewAction,
                    removePreviewAction,

                    $preview = $('.contentPreview'),
                    $previewWidget = $preview.find('.widget-preview'),
                    $previewHeading = $preview.find('h1'),
                    showPreview,
                    previewEventsBound,
                    hidePreview,

                    getUniqueColor,
                    fieldHue = Math.random(),
                    GOLDEN_RATIO = 0.618033988749895;

            // Append a link for activating the preview.
            appendPreviewAction = function() {
                $previewAction = $('<li/>', {
                    'html': $('<a/>', {
                        'class': 'action-preview',
                        'href': '#',
                        'text': 'Preview',
                        'click': function() {
                            removePreviewAction();
                            showPreview();
                            $previewHeading.click();
                            return false;
                        }
                    })
                });

                $publishingExtra.append($previewAction);
            };

            removePreviewAction = function() {
                $previewAction.remove();
                $previewAction = null;
            };

            // Show a peekable preview widget.
            showPreview = function() {
                var $previewForm = $('#<%= previewFormId %>'),
                        $contentForm = $('.contentForm'),
                        action = win.location.href,
                        questionAt = action.indexOf('?'),
                        oldFormData,
                        loadPreview;

                $previewWidget.addClass('widget-loading');
                $preview.show();

                $edit.css({
                    'margin-right': $preview.outerWidth(true),
                    'max-width': 1100
                });

                if (!previewEventsBound) {
                    $preview.append($('<span/>', {
                        'class': 'contentPreviewClose',
                        'text': 'Close',
                        'click': function() {
                            hidePreview();
                            return false;
                        }
                    }));

                    // Preview should be roughly the same width as the window.
                    $win.resize($.throttle(500, function() {
                        var css = $edit.offset(),
                                winWidth = $win.width();

                        css.left += $previewWidget.is('.widget-expanded') ? PEEK_WIDTH : $edit.outerWidth() + 10;
                        css['min-width'] = winWidth - css.left;

                        $preview.css(css);
                        $previewWidget.css('width', winWidth - PEEK_WIDTH);
                    }));

                    // Make the preview expand/collapse when the heading is clicked.
                    $previewHeading.click(function() {
                        var editLeft = $edit.offset().left;

                        $edit.find('.inputContainer').trigger('fieldPreview-disable');

                        if ($previewWidget.is('.widget-expanded')) {
                            $previewWidget.removeClass('widget-expanded');
                            $preview.animate({ 'left': editLeft + $edit.outerWidth() + 10 }, 300, 'easeOutBack');
                            $preview.css('width', '');

                            $.ajax({
                                'type': 'post',
                                'url': CONTEXT_PATH + '/misc/updateUserSettings',
                                'data': 'action=liveContentPreview-enable'
                            });

                        } else {
                            $previewWidget.addClass('widget-expanded');
                            $preview.animate({ 'left': editLeft + PEEK_WIDTH }, 300, 'easeOutBack');
                            $preview.css('width', $win.width() - PEEK_WIDTH - 30);

                            $edit.find('.inputContainer').trigger('fieldPreview-enable');
                        }
                    });

                    previewEventsBound = true;
                }

                $win.resize();

                // Load the preview.
                loadPreview = $.throttle(2000, function() {
                    if (!$preview.is(':visible')) {
                        return;
                    }

                    var newFormData = $contentForm.serialize();

                    // If the form inputs haven't changed, try again later.
                    if (oldFormData === newFormData) {
                        setTimeout(loadPreview, 100);
                        return;
                    }

                    oldFormData = newFormData;
                    $previewWidget.addClass('widget-loading');

                    // Get the correct JSON from the server.
                    $.ajax({
                        'data': newFormData,
                        'type': 'post',
                        'url': CONTEXT_PATH + 'content/state.jsp?id=<%= state.getId() %>&' + (questionAt > -1 ? action.substring(questionAt + 1) : ''),
                        'complete': function(request) {
                            var $previewTarget,
                                    setHeightTimer,
                                    setHeight;

                            // Make sure that the preview IFRAME exists.
                            $(':input[name=<%= PageFilter.PREVIEW_OBJECT_PARAMETER %>]').val(request.responseText);
                            $previewTarget = $('iframe[name=<%= previewTarget %>]');

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
                                $previewWidget.append($previewTarget);
                            }

                            // Resize IFRAME so that there isn't a scrollbar.
                            setHeight = function() {
                                var $body;

                                if ($previewTarget[0]) {
                                    $body = $($previewTarget[0].contentWindow.document.body);
                                    $body.css('overflow', 'hidden');
                                    $previewTarget.height(Math.max($edit.outerHeight(true), $body.outerHeight(true)));

                                } else if (setHeightTimer) {
                                    clearInterval(setHeightTimer);
                                    setHeightTimer = null;
                                }
                            };

                            setHeightTimer = setInterval(setHeight, 100);

                            $previewTarget.load(function() {
                                $previewWidget.removeClass('widget-loading');
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
            };

            hidePreview = function() {
                if ($previewWidget.is('.widget-expanded')) {
                    $previewHeading.click();
                }

                $edit.find('.inputContainer').trigger('fieldPreview-hide');
                $edit.attr('style', oldEditStyle);
                appendPreviewAction();
                $preview.hide();
                $win.resize();

                $.ajax({
                    'type': 'post',
                    'url': CONTEXT_PATH + '/misc/updateUserSettings',
                    'data': 'action=liveContentPreview-disable'
                });
            };

            <% if (Boolean.TRUE.equals(wp.getUser().getState().get("liveContentPreview"))) { %>
                showPreview();
            <% } else { %>
                appendPreviewAction();
            <% } %>

            // Per-field preview.
            getUniqueColor = function($container) {
                var color = $.data($container[0], 'fieldPreview-color');

                if (!color) {
                    fieldHue += GOLDEN_RATIO;
                    fieldHue %= 1.0;
                    color = 'hsl(' + (fieldHue * 360) + ', 50%, 50%)';
                    $.data($container[0], 'fieldPreview-color', color);
                }

                return color;
            };

            $edit.delegate('.inputContainer', 'mouseenter', function() {
                var $container = $(this),
                        $toggle = $.data($container[0], 'fieldPreview-$toggle');

                if ($preview.is(':visible')) {
                    if (!$toggle) {
                        $toggle = $('<span/>', {
                            'class': 'fieldPreviewToggle'
                        });

                        $.data($container[0], 'fieldPreview-$toggle', $toggle);
                        $container.append($toggle);
                    }

                } else if ($toggle) {
                    $toggle.remove();
                }
            });

            $edit.delegate('.inputContainer .fieldPreviewToggle', 'click', function() {
                var $toggle = $(this),
                        $container = $toggle.closest('.inputContainer');

                $container.find('> .inputLabel').trigger('fieldPreview-toggle', [ $toggle ]);
                $toggle.css('color', $container.is('.fieldPreview-displaying') ? getUniqueColor($container) : '');
            });

            $edit.delegate('.inputContainer', 'fieldPreview-enable', function() {
                $(this).addClass('fieldPreview-enabled');
            });

            $edit.delegate('.inputContainer', 'fieldPreview-disable', function() {
                $(this).trigger('fieldPreview-hide').removeClass('fieldPreview-enabled');
            });

            $edit.delegate('.inputContainer', 'fieldPreview-hide', function() {
                var $container = $(this),
                        name = $container.attr('data-name');

                $container.removeClass('fieldPreview-displaying');
                $container.find('> .inputLabel').css({
                    'background-color': '',
                    'color': ''
                });

                $('.fieldPreviewTarget[data-name="' + name + '"]').remove();
                $('.fieldPreviewPaths[data-name="' + name + '"]').remove();
            });

            $edit.delegate('.inputContainer', 'fieldPreview-toggle', function(event, $source) {
                var $container = $(this),
                        name = $container.attr('data-name'),
                        color,

                        $frame,
                        frameOffset,

                        $paths,
                        pathsCanvas;

                event.stopPropagation();

                if ($container.is('.fieldPreview-displaying')) {
                    $container.trigger('fieldPreview-hide');
                    return;
                }

                color = getUniqueColor($container);

                $frame = $preview.find('iframe');
                frameOffset = $frame.offset();

                $container.addClass('fieldPreview-displaying');
                $container.find('> .inputLabel').css({
                    'background-color': color,
                    'color': 'white'
                });

                // Draw arrows between the label and the previews.
                $paths = $('<canvas/>', {
                    'class': 'fieldPreviewPaths',
                    'data-name': name,
                    'css': {
                        'left': 0,
                        'pointer-events': 'none',
                        'position': 'absolute',
                        'top': 0,
                        'z-index': 5
                    }
                });

                // For browsers that don't support pointer-events.
                $paths.click(function() {
                    $edit.find('.inputContainer').trigger('fieldPreview-hide');
                });

                $paths.attr({
                    'width': $doc.width(),
                    'height': $doc.height()
                });

                $body.append($paths);

                pathsCanvas = $paths[0].getContext('2d');

                $frame.contents().find('[data-name="' + name + '"]').each(function() {
                    var $placeholder = $(this),
                            $target,
                            targetOffset,
                            pathSourceX, pathSourceY, pathSourceDirection,
                            pathTargetX, pathTargetY, pathTargetDirection,
                            sourceOffset,
                            targetOffset,
                            isBackReference = false,
                            pathSourceControlX,
                            pathSourceControlY,
                            pathTargetControlX,
                            pathTargetControlY;

                    if ($placeholder.parent().is('body')) {
                        return;
                    }

                    $target = $placeholder.nextAll(':visible:first');

                    if ($target.length === 0) {
                        $target = $placeholder.parent();
                    }

                    if ($target.find('> * [data-name="' + name + '"]').length > 0) {
                        return;
                    }

                    targetOffset = $target.offset();

                    $body.append($('<span/>', {
                        'class': 'fieldPreviewTarget',
                        'data-name': name,
                        'css': {
                            'outline-color': color,
                            'height': $target.outerHeight(),
                            'left': frameOffset.left + targetOffset.left,
                            'position': 'absolute',
                            'top': frameOffset.top + targetOffset.top,
                            'width': $target.outerWidth()
                        }
                    }));

                    if (!$source) {
                        $source = $container.find('> .inputLabel');
                    }

                    sourceOffset = $source.offset();
                    targetOffset = $target.offset();
                    targetOffset.left += frameOffset.left;
                    targetOffset.top += frameOffset.top;

                    if (sourceOffset.left > targetOffset.left) {
                        var targetWidth = $target.outerWidth();
                        pathTargetX = targetOffset.left + targetWidth;
                        pathTargetY = targetOffset.top + $target.outerHeight() / 2;
                        isBackReference = true;

                        if (targetOffset.left + targetWidth > sourceOffset.left) {
                            pathSourceX = sourceOffset.left + $source.width();
                            pathSourceY = sourceOffset.top + $source.height() / 2;
                            pathSourceDirection = 1;
                            pathTargetDirection = 1;

                        } else {
                            pathSourceX = sourceOffset.left;
                            pathSourceY = sourceOffset.top + $source.height() / 2;
                            pathSourceDirection = -1;
                            pathTargetDirection = 1;
                        }

                    } else {
                        pathSourceX = sourceOffset.left + $source.width();
                        pathSourceY = sourceOffset.top + $source.height() / 2;
                        pathTargetX = targetOffset.left;
                        pathTargetY = targetOffset.top + $target.height() / 2;
                        pathSourceDirection = 1;
                        pathTargetDirection = -1;
                    }

                    pathSourceControlX = pathSourceX + pathSourceDirection * 100;
                    pathSourceControlY = pathSourceY;
                    pathTargetControlX = pathTargetX + pathTargetDirection * 100;
                    pathTargetControlY = pathTargetY;

                    pathsCanvas.strokeStyle = color;
                    pathsCanvas.fillStyle = color;

                    // Reference curve.
                    pathsCanvas.lineWidth = isBackReference ? 0.4 : 1.0;
                    pathsCanvas.beginPath();
                    pathsCanvas.moveTo(pathSourceX, pathSourceY);
                    pathsCanvas.bezierCurveTo(pathSourceControlX, pathSourceControlY, pathTargetControlX, pathTargetControlY, pathTargetX, pathTargetY);
                    pathsCanvas.stroke();

                    // Arrow head.
                    var arrowSize = pathTargetX > pathTargetControlX ? 5 : -5;
                    if (isBackReference) {
                        arrowSize *= 0.8;
                    }
                    pathsCanvas.beginPath();
                    pathsCanvas.moveTo(pathTargetX, pathTargetY);
                    pathsCanvas.lineTo(pathTargetX - 2 * arrowSize, pathTargetY - arrowSize);
                    pathsCanvas.lineTo(pathTargetX - 2 * arrowSize, pathTargetY + arrowSize);
                    pathsCanvas.closePath();
                    pathsCanvas.fill();
                });
            });

            $edit.delegate('.inputContainer', 'click', function() {
                if ($previewWidget.is('.widget-expanded')) {
                    $(this).trigger('fieldPreview-toggle');
                    return false;

                } else {
                    return true;
                }
            });
        })(jQuery, window);
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
                wp.write(wp.h(widget.getInternalName()));
                wp.write("\">");

                String displayHtml;

                try {
                    displayHtml = widget.createDisplayHtml(wp, object);

                } catch (Exception ex) {
                    StringWriter sw = new StringWriter();
                    HtmlWriter hw = new HtmlWriter(sw);
                    hw.putAllStandardDefaults();
                    hw.start("pre", "class", "message message-error").object(ex).end();
                    displayHtml = sw.toString();
                }

                if (!ObjectUtils.isBlank(displayHtml)) {
                    wp.write(displayHtml);
                }
            }
        }
        wp.write("</div>");
    }
}
%>
