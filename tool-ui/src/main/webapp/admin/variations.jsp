<%@ page import="

com.psddev.cms.db.Profile,
com.psddev.cms.db.Variation,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.State,

java.util.List
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/admin/adminVariations")) {
    return;
}

List<Variation> variations = Query.from(Variation.class).sortAscending("position").select();
Object selected = wp.findOrReserve(Variation.class, Profile.class);
Class<?> selectedClass = selected.getClass();
State selectedState = State.getInstance(selected);

// Automatically set the position to be the first on a new variation.
if (selectedClass == Variation.class && selectedState.isNew()) {
    double minimumPosition;
    if (variations.isEmpty()) {
        minimumPosition = 0;
    } else {
        minimumPosition = Double.MAX_VALUE;
        for (Variation variation : variations) {
            double position = variation.getPosition();
            if (position < minimumPosition) {
                minimumPosition = position;
            }
        }
    }
    ((Variation) selected).setPosition(minimumPosition - 1.0 - Math.random());
}

if (wp.tryStandardUpdate(selected)) {
    return;
}

List<Profile> profiles = Query.from(Profile.class).sortAscending("name").select();

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<div class="withLeftNav">
    <div class="leftNav">
        <div class="widget">

            <h1 class="icon icon-object-variation">Variations &amp; Profiles</h1>

            <h2>Variations</h2>
            <ol class="links">
                <li class="new<%= selectedClass == Variation.class && selectedState.isNew() ? " selected" : "" %>">
                    <a href="<%= wp.typeUrl(null, Variation.class) %>">New Variation</a>
                </li>
                <% for (Variation variation : variations) { %>
                    <li<%= variation.equals(selected) ? " class=\"selected\"" : "" %>>
                        <a href="<%= wp.objectUrl(null, variation) %>"><%= wp.objectLabel(variation) %></a>
                    </li>
                <% } %>
            </ol>

            <h2>Profiles</h2>
            <ul class="links">
                <li class="new<%= selectedClass == Profile.class && selectedState.isNew() ? " selected" : "" %>">
                    <a href="<%= wp.typeUrl(null, Profile.class) %>">New Profile</a>
                </li>
                <% for (Profile profile : profiles) { %>
                    <li<%= profile.equals(selected) ? " class=\"selected\"" : "" %>>
                        <a href="<%= wp.objectUrl(null, profile) %>"><%= wp.objectLabel(profile) %></a>
                    </li>
                <% } %>
            </ul>

        </div>
    </div>
    <div class="main">

        <div class="widget">
            <% wp.writeStandardForm("selected"); %>
        </div>

    </div>
</div>

<% wp.include("/WEB-INF/footer.jsp"); %>
