<%@ page import="

com.psddev.cms.db.ToolAuthenticationPolicy,
com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.AuthenticationFilter,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.util.AuthenticationException,
com.psddev.dari.util.AuthenticationPolicy,
com.psddev.dari.util.HtmlWriter,
com.psddev.dari.util.JspUtils,
com.psddev.dari.util.Settings,

java.net.MalformedURLException,
java.net.URL
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
AuthenticationException authError = null;
String email = wp.param("email");

if (wp.isFormPost()) {
    String password = wp.param("password");
    AuthenticationPolicy authPolicy = AuthenticationPolicy.Static.getInstance(Settings.get(String.class, "cms/tool/authenticationPolicy"));

    if (authPolicy == null) {
        authPolicy = new ToolAuthenticationPolicy();
    }

    try {
        AuthenticationFilter.Static.logIn(request, response, (ToolUser) authPolicy.authenticate(email, password));
        try {
            wp.redirect(new URL(JspUtils.getAbsoluteUrl(request, wp.param(AuthenticationFilter.RETURN_PATH_PARAMETER, "/"))).toString());
        } catch (MalformedURLException e) {
            wp.redirect("/");
        }
        return;

    } catch (AuthenticationException error) {
        authError = error;
    }
}

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<style type="text/css">
.toolHeader {
    background-color: transparent;
    border-style: none;
}
.toolTitle {
    float: none;
    height: 100px;
    margin: 30px 0 0 0;
    text-align: center;
}
.toolFooter {
    border-style: none;
    text-align: center;
}
.toolFooter .build {
    background-position: top center;
    text-align: center;
}
.widget {
    margin: 0 auto;
    width: 30em;
}
body {
    margin-top: 170px;
}
</style>

<div class="widget">
    <h1>Log In</h1>

    <%
    if (authError != null) {
        new HtmlWriter(wp.getWriter()).object(authError);
    }
    %>

    <% if (!Query.from(ToolUser.class).hasMoreThan(0)) { %>
        <div class="message message-info">
            <p>Welcome! You're our first user. Give us your email and
            password and we'll make you an administrator.</p>
        </div>
    <% } %>

    <form action="<%= wp.url("") %>" method="post">
        <div class="inputContainer">
            <div class="inputLabel">
                <label for="<%= wp.createId() %>">Email</label>
            </div>
            <div class="smallInput">
                <input class="autoFocus" id="<%= wp.getId() %>" name="email" type="text" value="<%= wp.h(email) %>">
            </div>
        </div>

        <div class="inputContainer">
            <div class="inputLabel">
                <label for="<%= wp.createId() %>">Password</label>
            </div>
            <div class="smallInput">
                <input id="<%= wp.getId() %>" name="password" type="password">
            </div>
        </div>

        <div class="buttons">
            <input type="submit" value="Log in">
        </div>
    </form>
</div>

<% wp.include("/WEB-INF/footer.jsp"); %>
