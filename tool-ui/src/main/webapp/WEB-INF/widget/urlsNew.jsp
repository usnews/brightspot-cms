<%@ page session="false" import="

com.psddev.cms.db.Content,
com.psddev.cms.db.Directory,
com.psddev.cms.db.Site,
com.psddev.cms.db.Workflow,
com.psddev.cms.tool.JspWidget,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.State,
com.psddev.dari.util.CompactMap,
com.psddev.dari.util.ObjectUtils,

java.util.LinkedHashSet,
java.util.List,
java.util.Map,
java.util.Set,
java.util.UUID
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);
Object object = JspWidget.getOriginal(wp);
Object varied = JspWidget.getObject(wp);
Site site = wp.getSite();
State state = State.getInstance(object);

String namePrefix = state.getId() + "/directory.";
String automaticName = namePrefix + "automatic";
String pathName = namePrefix + "path";
String removeName = namePrefix + "remove";
String typeName = namePrefix + "type";
String siteIdName = namePrefix + "siteId";

Directory.Data dirData = state.as(Directory.Data.class);
Map<UUID, Site> sites = new CompactMap<UUID, Site>();

for (Site s : Query.
        from(Site.class).
        sortAscending("name").
        selectAll()) {
    sites.put(s.getId(), s);
}

boolean initialDraft = state.isNew() ||
        state.as(Content.ObjectModification.class).isDraft() ||
        state.as(Workflow.Data.class).getCurrentState() != null;

if (JspWidget.isUpdating(wp)) {
    dirData.setPathsMode(wp.param(boolean.class, automaticName) ? null : Directory.PathsMode.MANUAL);
    dirData.clearPaths();

    List<String> paths = wp.params(String.class, pathName);
    List<UUID> siteIds = wp.params(UUID.class, siteIdName);
    List<Directory.PathType> types = wp.params(Directory.PathType.class, typeName);

    for (int i = 0, size = Math.min(paths.size(), types.size()); i < size; i ++) {
        if (!wp.param(boolean.class, removeName + "." + i)) {
            dirData.addPath(i < siteIds.size() ? sites.get(siteIds.get(i)) : null, paths.get(i), types.get(i));
        }
    }

    // Automatically generate URLs if requested.
    if (initialDraft) {
        if (!Directory.PathsMode.MANUAL.equals(dirData.getPathsMode())) {
            Set<Directory.Path> oldPaths = new LinkedHashSet<Directory.Path>(dirData.getPaths());
            Set<String> oldRawPaths = new LinkedHashSet<String>(dirData.getRawPaths());

            for (Directory.Path path : State.getInstance(varied).as(Directory.ObjectModification.class).createPaths(site)) {
                dirData.addPath(path.getSite(), path.getPath(), path.getType());
            }

            Set<Directory.Path> newPaths = new LinkedHashSet<Directory.Path>(dirData.getPaths());
            Set<String> newRawPaths = new LinkedHashSet<String>(dirData.getRawPaths());

            newPaths.removeAll(oldPaths);
            newRawPaths.removeAll(oldRawPaths);
            state.getExtras().put("cms.newPaths", newPaths);
            dirData.setAutomaticRawPaths(newRawPaths);

        } else {
            dirData.setAutomaticRawPaths(null);
        }

    } else {
        if (!Directory.PathsMode.MANUAL.equals(dirData.getPathsMode())) {
            dirData.setPathsMode(Directory.PathsMode.MANUAL);
            dirData.setAutomaticRawPaths(null);

            for (Directory.Path path : State.getInstance(varied).as(Directory.ObjectModification.class).createPaths(site)) {
                dirData.addPath(path.getSite(), path.getPath(), path.getType());
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

if (initialDraft) {
    wp.writeStart("div", "class", "widget-urlsAutomatic");
        wp.writeStart("label");
            wp.writeElement("input",
                    "type", "checkbox",
                    "name", automaticName,
                    "value", "true",
                    "checked", Directory.PathsMode.MANUAL.equals(dirData.getPathsMode()) ? null : "checked");

            wp.writeHtml(" Generate Permalink?");
        wp.writeEnd();

        wp.writeStart("div", "id", automaticContainerId);
        wp.writeEnd();
    wp.writeEnd();

} else if (!Directory.PathsMode.MANUAL.equals(dirData.getPathsMode())) {
    wp.writeElement("input",
            "type", "hidden",
            "name", automaticName,
            "value", true);
}

Set<Directory.Path> paths = initialDraft ? dirData.getManualPaths() : dirData.getPaths();

if (!paths.isEmpty()) {
    int index = 0;

    wp.writeStart("ul");
        for (Directory.Path path : paths) {
            Site pathSite = path.getSite();
            String pathPath = path.getPath();
            String href = pathSite != null ? pathSite.getPrimaryUrl() + pathPath : pathPath;

            while (href.endsWith("*")) {
                href = href.substring(0, href.length() - 1);
            }

            wp.writeStart("li", "class", "widget-urlsItem");
                wp.writeElement("input",
                        "type", "hidden",
                        "id", wp.createId(),
                        "name", pathName,
                        "value", pathPath);

                wp.writeStart("div", "class", "widget-urlsItemLabel");
                    wp.writeStart("a", "href", href, "target", "_blank");
                        wp.writeHtml(pathPath);
                    wp.writeEnd();

                    wp.writeStart("label",
                            "class", "widget-urlsItemRemove");
                        wp.writeHtml(" ");

                        wp.writeElement("input",
                                "type", "checkbox",
                                "name", removeName + "." + index,
                                "value", "true");

                        wp.writeHtml(" Remove");
                    wp.writeEnd();
                wp.writeEnd();

                if (!sites.isEmpty()) {
                    wp.writeStart("select", "name", siteIdName);
                        wp.writeStart("option", "value", "");
                            wp.writeHtml("Global");
                        wp.writeEnd();

                        for (Site s : sites.values()) {
                            wp.writeStart("option",
                                    "selected", s.equals(path.getSite()) ? "selected" : null,
                                    "value", s.getId());
                                wp.writeObjectLabel(s);
                            wp.writeEnd();
                        }
                    wp.writeEnd();

                    wp.writeHtml(" ");
                }

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

            ++ index;
        }
    wp.writeEnd();
}
%>

<div class="repeatableInputs">
    <ul>
        <script type="text/template">
            <li class="widget-urlsItem" data-type="URL">
                <textarea class="widget-urlsItemLabel" name="<%= wp.h(pathName) %>"></textarea>

                <%
                if (!sites.isEmpty()) {
                    wp.writeStart("select", "name", siteIdName);
                        wp.writeStart("option", "value", "");
                            wp.writeHtml("Global");
                        wp.writeEnd();

                        for (Site s : sites.values()) {
                            wp.writeStart("option", "value", s.getId(), "selected", s.equals(site) ? "selected" : null);
                                wp.writeObjectLabel(s);
                            wp.writeEnd();
                        }
                    wp.writeEnd();

                    wp.writeHtml(" ");
                }

                wp.writeStart("select", "name", typeName);
                    for (Directory.PathType pathType : Directory.PathType.values()) {
                        wp.writeStart("option", "value", pathType.name());
                            wp.writeHtml(pathType);
                        wp.writeEnd();
                    }
                wp.writeEnd();
                %>
            </li>
        </script>
    </ul>
</div>

<script type="text/javascript">
    (function($, window, undefined) {
        var $automaticContainer = $('#<%= automaticContainerId %>'),
                $form = $automaticContainer.closest('form');

        $form.bind('cms-updateContentState', function(event, data) {
            $automaticContainer.html(data._urlWidgetHtml || '');
        });
    })(jQuery, window);
</script>
