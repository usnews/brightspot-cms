<%@ page import="

com.psddev.cms.db.Site,
com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.JspWidget,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Database,
com.psddev.dari.db.Query,
com.psddev.dari.db.State,

java.util.Arrays,
java.util.List,
java.util.Set,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
List<Site> allSites = Site.findAll();
if (allSites.isEmpty()) {
    return;
}

Object object = JspWidget.getObject(wp);
State state = State.getInstance(object);
Site.ObjectModification siteData = state.as(Site.ObjectModification.class);

String namePrefix = state.getId() + "/sites/";
String ownerName = namePrefix + "owner";
String accessName = namePrefix + "access";
String consumerIdName = namePrefix + "consumerId";

Site owner = siteData.getOwner();
Set<Site> consumers = siteData.getConsumers();

if (JspWidget.isUpdating(wp)) {
    owner = Database.Static.findById(wp.getDatabase(), Site.class, wp.uuidParam(ownerName));
    siteData.setOwner(owner);

    String access = wp.param(accessName);
    consumers.clear();

    if (owner == null) {
        siteData.setGlobal(true);
        siteData.setBlacklist(null);
        siteData.setConsumers(null);

    } else if ("no".equals(access)) {
        siteData.setGlobal(false);

    } else if ("all".equals(access)) {
        siteData.setGlobal(true);

    } else if ("some".equals(access)) {
        siteData.setGlobal(false);
        List<UUID> consumerIds = Arrays.asList(wp.uuidParams(consumerIdName));
        for (Site site : allSites) {
            if (consumerIds.contains(site.getId())) {
                consumers.add(site);
            }
        }
    }

    return;
}


// --- Presentation ---

String accessContainerId = wp.createId();
String sitesContainerId = wp.createId();
String access = siteData.isGlobal() ? "all" :
        consumers.isEmpty() ? "no" :
        "some";

%>
<label for="<%= wp.createId() %>">Owner:</label><br>
<select class="toggleable" name="<%= ownerName %>" style="width: 100%;">
    <option<%= owner == null ? " selected" : "" %> data-hide="#<%= accessContainerId %>" value="">Global</option>
    <% for (Site site : allSites) { %>
        <option<%= site.equals(owner) ? " selected" : "" %> data-show="#<%= accessContainerId %>" value="<%= site.getId() %>"><%= wp.objectLabel(site) %></option>
    <% } %>
</select>

<div id="<%= accessContainerId %>">
    <label for="<%= wp.createId() %>">Access:</label><br>
    <select class="toggleable" id="<%= wp.getId() %>" name="<%= accessName %>" style="width: 100%;">
        <option<%= "no".equals(access) ? " selected" : "" %> data-hide="#<%= sitesContainerId %>" value="no">No Others</option>
        <option<%= "all".equals(access) ? " selected" : "" %> data-hide="#<%= sitesContainerId %>" value="all">All Others</option>
        <option<%= "some".equals(access) ? " selected" : "" %> data-show="#<%= sitesContainerId %>" value="some">Some Others</option>
    </select>
    <ul id="<%= sitesContainerId %>">
        <% for (Site site : allSites) { %>
            <% if (!site.equals(owner)) { %>
                <li>
                    <input<%= consumers.contains(site) ? " checked" : "" %> id="<%= wp.createId() %>" name="<%= consumerIdName %>" type="checkbox" value="<%= site.getId() %>">
                    <label for="<%= wp.getId() %>"><%= wp.objectLabel(site) %></label>
                </li>
            <% } %>
        <% } %>
    </ul>
</div>
