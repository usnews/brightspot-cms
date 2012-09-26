<%@ page import="

com.psddev.cms.db.Content,
com.psddev.cms.db.Site,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Database,
com.psddev.dari.db.Database,
com.psddev.dari.db.Query,
com.psddev.dari.db.State,
com.psddev.dari.util.PaginatedResult,
com.psddev.dari.util.Task
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.isFormPost()) {
    String action = wp.param("action");

    if ("Start".equals(action)) {
        DATABASE = Database.Static.getDefault();
        OWNER = Query.findById(Site.class, wp.uuidParam("owner"));
        OWNER_SITE_SETTER_TASK.start();
        Thread.sleep(1000);

    } else if ("Stop".equals(action)) {
        OWNER_SITE_SETTER_TASK.stop();
    }
}

%><% wp.include("/WEB-INF/header.jsp"); %>
<div class="widget">

    <% if (OWNER_SITE_SETTER_TASK.isRunning()) { %>
        <h1>Running...</h1>
        <p>Run duration: <%= wp.h(OWNER_SITE_SETTER_TASK.getDuration(), "N/A") %></p>
        <p>Progress: <%= wp.h(OWNER_SITE_SETTER_TASK.getProgress(), "N/A") %></p>

        <form method="post">
            <input type="submit" name="action" value="Stop" />
        </form>

        <script type="text/javascript">
            setTimeout(function() {
                window.location = window.location;
            }, 2000);
        </script>

    <% } else { %>
        <h1>NOT Running!</h1>
        <p>Last run duration: <%= wp.h(OWNER_SITE_SETTER_TASK.getDuration(), "N/A") %></p>
        <p>Last progress: <%= wp.h(OWNER_SITE_SETTER_TASK.getProgress(), "N/A") %></p>

        <%
        Throwable lastException = OWNER_SITE_SETTER_TASK.getLastException();
        if (lastException != null) {
            %>
            <p>Last error: <%= wp.h(lastException) %></p>
        <% } %>

        <form method="post">
            <select name="owner">
                <% for (Site site : Site.findAll()) { %>
                    <option value="<%= site.getId() %>"><%= wp.objectLabel(site) %></option>
                <% } %>
            </select>
            <input type="submit" name="action" value="Start" />
        </form>
    <% } %>

</div>
<% wp.include("/WEB-INF/footer.jsp"); %><%!

private static Database DATABASE;
private static Site OWNER;
private static final Task OWNER_SITE_SETTER_TASK = new Task() {

    @Override
    public void doTask() throws Exception {
        long offset = 0;
        int limit = 500;
        for (PaginatedResult<?> result; (result = DATABASE.readPartial(Query.
                from(Content.class).
                sortAscending("id"),
                offset, limit)).hasItems();
                offset += limit) {

            try {
                DATABASE.beginWrites();

                boolean isSaved = false;
                for (Object item : result.getItems()) {
                    addProgressIndex(1);
                    Site.ObjectModification siteSegment = State.getInstance(item).as(Site.ObjectModification.class);
                    if (siteSegment.getOwner() == null) {
                        siteSegment.setOwner(OWNER);
                        siteSegment.save();
                        isSaved = true;
                    }
                }

                if (isSaved) {
                    DATABASE.commitWrites();
                }

            } finally {
                DATABASE.endWrites();
            }
        }
    }
};
%>
