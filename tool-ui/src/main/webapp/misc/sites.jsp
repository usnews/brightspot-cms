<%@ page import="

com.psddev.cms.db.Site,
com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,

java.util.ArrayList,
java.util.List
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
ToolUser user = wp.getUser();
if (wp.isFormPost()) {
    Site newCurrentSite = Query.findById(Site.class, wp.uuidParam("id"));
    user.setCurrentSite(newCurrentSite);
    wp.publish(user);
    %><script type="text/javascript">window.top.window.location = window.top.window.location;</script><%
    return;
}

Site currentSite = user.getCurrentSite();
List<Site> sites = new ArrayList<Site>();
for (Site site : Site.findAll()) {
    if (wp.hasPermission(site.getPermissionId())) {
        sites.add(site);
    }
}

// --- Presentation ---

%><h1>Sites</h1>
<form action="<%= wp.url(null) %>" method="post">
    <select name="id">
        <option value="">All Sites</option>
        <% for (Site site : sites) { %>
            <option<%= site.equals(currentSite) ? " selected" : "" %> value="<%= site.getId() %>"><%= wp.objectLabel(site) %></option>
        <% } %>
    </select>
    <input type="submit" value="Switch">
</form>
