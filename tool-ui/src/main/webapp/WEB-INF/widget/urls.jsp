<%@ page import="

com.psddev.cms.db.Directory,
com.psddev.cms.db.Page,
com.psddev.cms.db.Site,
com.psddev.cms.db.Template,
com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.JspWidget,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils,

java.util.LinkedHashMap,
java.util.List,
java.util.Map
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
Object object = JspWidget.getObject(wp);
boolean isPage = object.getClass() == Page.class;
State objectState = State.getInstance(object);
Directory.ObjectModification objectAsDirMod = objectState.as(Directory.ObjectModification.class);

if (!(isPage || Template.Static.findUsedTypes(wp.getSite()).contains(objectState.getType()))) {
    return;
}

String namePrefix = objectState.getId() + "/directory/";
String modeName = namePrefix + "mode";
String typeName = namePrefix + "type";
String addName = namePrefix + "add";

Template objectTemplate = objectState.as(Template.ObjectModification.class).getDefault();
boolean hasPathScript = !isPage
        && objectTemplate != null
        && !ObjectUtils.isBlank(objectTemplate.getPathsEngine())
        && !ObjectUtils.isBlank(objectTemplate.getPathsScript());

ToolUser user = wp.getUser();
Site site = wp.getSite();

if (JspWidget.isUpdating(wp)) {
    objectAsDirMod.clearSitePaths(site);
    objectAsDirMod.setPathsMode(Directory.PathsMode.valueOf(wp.param(modeName, Directory.PathsMode.MANUAL.name())));

    if (Directory.PathsMode.AUTOMATIC.name().equals(wp.param(modeName))) {
        if (objectTemplate != null) {
            for (Directory.Path path : objectTemplate.makePaths(site, object)) {
                objectAsDirMod.addSitePath(path.getSite(), path.getPath(), path.getType());
            }
        }

    } else {
        String[] types = wp.params(typeName);
        String[] adds = wp.params(addName);
        for (int i = 0, length = Math.min(types.length, adds.length); i < length; i ++) {
            objectAsDirMod.addSitePath(site, adds[i], Directory.PathType.valueOf(types[i]));
        }
    }

    return;
}

String widgetContainerId = wp.createId();
String modeInputId = wp.createId();
String warningContainerId = wp.createId();
String automaticContainerId = wp.createId();
String manualContainerId = wp.createId();

List<String> errors = objectState.getErrors(objectState.getField(Directory.PATHS_FIELD));

Directory.PathsMode pathsMode = objectAsDirMod.getPathsMode();
if (pathsMode == null) {
    pathsMode = objectState.isNew() && hasPathScript ?
            Directory.PathsMode.AUTOMATIC :
            Directory.PathsMode.MANUAL;
}

// --- Presentation ---

%><div id="<%= widgetContainerId %>">
    <% if (!ObjectUtils.isBlank(errors)) { %>
        <div class="error message">
            <% for (String error : errors) { %>
                <%= wp.h(error) %>
            <% } %>
        </div>
    <% } %>

    <% if (hasPathScript) { %>
        <p><select id="<%= modeInputId %>" name="<%= modeName %>">
            <% for (Directory.PathsMode mode : Directory.PathsMode.values()) { %>
                <option value="<%= wp.h(mode.name()) %>"<%= mode.equals(pathsMode) ? " selected" : "" %>><%= wp.h(mode) %></option>
            <% } %>
        </select></p>

        <ul id="<%= automaticContainerId %>">
            <% if (objectTemplate != null) { %>
                <% for (Directory.Path path : objectTemplate.makePaths(site, object)) { %>
                    <li><%= wp.h(path.getPath()) %> (<%= wp.h(path.getType()) %>)</li>
                <% } %>
            <% } %>
        </ul>
    <% } %>

    <div class="repeatableInputs" id="<%= manualContainerId %>">
        <div class="warning message" id="<%= warningContainerId %>" style="display: none;">
            <p>More than one permalink per content isn't recommended!</p>
        </div>

        <ul>
            <% for (Directory.Path path : objectAsDirMod.getSitePaths(site)) { %>
                <li><div style="margin-bottom: 10px;">
                    <textarea name="<%= wp.h(addName) %>"><%= wp.h(path.getPath()) %></textarea>
                    <select name="<%= wp.h(typeName) %>">
                        <% for (Directory.PathType pathType : Directory.PathType.values()) { %>
                            <option value="<%= wp.h(pathType.name()) %>"<%= pathType.equals(path.getType()) ? " selected" : "" %>><%= wp.h(pathType) %></option>
                        <% } %>
                    </select>
                </div></li>
            <% } %>

            <li class="template"><div style="margin-bottom: 10px;">
                <textarea name="<%= wp.h(addName) %>"></textarea>
                <select name="<%= wp.h(typeName) %>">
                    <% for (Directory.PathType pathType : Directory.PathType.values()) { %>
                        <option value="<%= wp.h(pathType.name()) %>"><%= wp.h(pathType) %></option>
                    <% } %>
                </select>
            </div></li>
        </ul>
    </div>
</div>

<script type="text/javascript">
if (typeof jQuery !== 'undefined') jQuery(function($) {
    var $widgetContainer = $('#<%= widgetContainerId %>');
    var $modeInput = $('#<%= modeInputId %>');
    var $warningContainer = $('#<%= warningContainerId %>');
    var $automaticContainer = $('#<%= automaticContainerId %>');
    var $manualContainer = $('#<%= manualContainerId %>');

    var updateWarning = function() {
        if ($widgetContainer.find('select:not(:disabled) option:selected[value=<%= Directory.PathType.PERMALINK.name() %>]').length > 1) {
            $warningContainer.show();
        } else {
            $warningContainer.hide();
        }
    };
    updateWarning();

    <% if (!isPage) { %>
        var $form = $widgetContainer.closest('form');
        var updateAutomaticUrls = function() {
            var action = $form.attr('action');
            var questionAt = action.indexOf('?');
            $.ajax({
                'data': $form.serialize(),
                'type': 'post',
                'url': CONTEXT_PATH + 'content/automaticPaths.jsp' + (questionAt > -1 ? action.substring(questionAt) : ''),
                'complete': function(request) {
                    $automaticContainer.html(request.responseText);
                }
            });
        };

        $modeInput.change(function() {
            if ($modeInput.val() === '<%= Directory.PathsMode.AUTOMATIC.name() %>') {
                $automaticContainer.show();
                $manualContainer.hide();
                $form.bind('change keyup', updateAutomaticUrls);
                $widgetContainer.die('change', updateWarning);
            } else {
                $automaticContainer.hide();
                $manualContainer.show();
                $form.unbind('change keyup', updateAutomaticUrls);
                $widgetContainer.live('change', updateWarning);
            }
        });
        $modeInput.change();
    <% } %>
});
</script>
