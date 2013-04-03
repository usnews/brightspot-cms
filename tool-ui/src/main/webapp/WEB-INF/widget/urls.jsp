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

java.util.ArrayList,
java.util.LinkedHashMap,
java.util.List,
java.util.Map
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);
Object object = JspWidget.getOriginal(wp);
Object varied = JspWidget.getObject(wp);
boolean isPage = Page.class.isInstance(object) && !Template.class.isInstance(object);
Site site = wp.getSite();
State state = State.getInstance(object);

if (!(isPage || Template.Static.findUsedTypes(site).contains(state.getType()))) {
    return;
}

String namePrefix = state.getId() + "/directory.";
String automaticName = namePrefix + "automatic";
String typeName = namePrefix + "type";
String addName = namePrefix + "add";
Directory.ObjectModification dirData = state.as(Directory.ObjectModification.class);
Template template = state.as(Template.ObjectModification.class).getDefault();

if (JspWidget.isUpdating(wp)) {
    List<String> automatic = wp.params(String.class, automaticName);
    List<Directory.PathType> types = wp.params(Directory.PathType.class, typeName);
    List<String> adds = wp.params(String.class, addName);

    if (automatic.size() > 0) {
        dirData.setPathsMode(automatic.size() != 1 ? null : Directory.PathsMode.MANUAL);
    }

    dirData.clearSitePaths(site);

    for (int i = 0, size = Math.min(types.size(), adds.size()); i < size; i ++) {
        dirData.addSitePath(site, adds.get(i), types.get(i));
    }

    if (automatic.size() != 1 && template != null) {
        List<Directory.Path> manualPaths = dirData.getSitePaths(site);
        List<Directory.Path> automaticPaths = new ArrayList<Directory.Path>();

        state.getExtras().put("cms.automaticPaths", automaticPaths);

        for (Directory.Path path : State.getInstance(varied).as(Directory.ObjectModification.class).createPaths(site)) {
            dirData.addSitePath(path.getSite(), path.getPath(), path.getType());

            if (!manualPaths.contains(path)) {
                automaticPaths.add(path);
            }
        }
    }

    return;
}

String automaticContainerId = wp.createId();
List<String> errors = state.getErrors(state.getField(Directory.PATHS_FIELD));

if (!ObjectUtils.isBlank(errors)) {
    wp.writeStart("div", "class", "message message-error");
        for (String error : errors) {
            wp.writeHtml(error);
        }
    wp.writeEnd();
}

List<Directory.Path> paths = dirData.getSitePaths(site);

if (!paths.isEmpty() &&
        (Directory.PathsMode.MANUAL.equals(dirData.getPathsMode()) ||
        !wp.getCmsTool().isSingleGeneratedPermalink() ||
        State.getInstance(varied).as(Directory.ObjectModification.class).createPaths(site).isEmpty())) {
    wp.writeStart("ul");
        for (Directory.Path path : paths) {
            wp.writeStart("li", "class", "widget-urlsItem");
                wp.writeStart("div", "class", "widget-urlsItemRemove");
                    wp.writeTag("input",
                            "type", "checkbox",
                            "id", wp.createId(),
                            "name", addName,
                            "value", path.getPath(),
                            "checked", "checked");

                    wp.writeHtml(" ");

                    wp.writeStart("label", "for", wp.getId());
                        wp.writeHtml("Keep");
                    wp.writeEnd();
                wp.writeEnd();

                wp.writeStart("div", "class", "widget-urlsItemLabel").writeHtml(path.getPath()).writeEnd();

                wp.writeStart("select", "name", typeName);
                    for (Directory.PathType pathType : Directory.PathType.values()) {
                        wp.writeStart("option",
                                "selected", pathType.equals(path.getType()) ? "selected" : null,
                                "value", pathType.name());
                            wp.writeHtml(pathType);
                        wp.writeEnd();
                    }
                wp.writeEnd();
            wp.writeEnd();
        }
    wp.writeEnd();
}

wp.writeStart("div", "id", automaticContainerId);
wp.writeEnd();
%>

<div class="repeatableInputs">
    <ul>
        <li class="template widget-urlsItem" data-type="URL">
            <textarea class="widget-urlsItemLabel" name="<%= wp.h(addName) %>"></textarea>
            <select name="<%= wp.h(typeName) %>">
                <% for (Directory.PathType pathType : Directory.PathType.values()) { %>
                    <option value="<%= wp.h(pathType.name()) %>"><%= wp.h(pathType) %></option>
                <% } %>
            </select>
        </li>
    </ul>
</div>

<script type="text/javascript">
if (typeof jQuery !== 'undefined') (function($, win, undef) {
    var $automaticContainer = $('#<%= automaticContainerId %>'),
            $form = $automaticContainer.closest('form'),
            updateAutomatic;

    updateAutomatic = $.throttle(100, function() {
        var action = $form.attr('action'),
                questionAt = action.indexOf('?');

        $.ajax({
            'data': $form.serialize(),
            'type': 'post',
            'url': CONTEXT_PATH + 'content/automaticPaths.jsp' + (questionAt > -1 ? action.substring(questionAt) : ''),
            'complete': function(request) {
                $automaticContainer.html(request.responseText);
            }
        });
    });

    updateAutomatic();
    $form.bind('change input', updateAutomatic);
})(jQuery, window);
</script>
