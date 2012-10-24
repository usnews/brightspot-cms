<%@ page import="

com.psddev.cms.db.ToolAuthenticationPolicy,
com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.ToolFilter,
com.psddev.cms.tool.ToolPageContext,

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
        ToolFilter.logIn(request, response, (ToolUser) authPolicy.authenticate(email, password));
        try {
            wp.redirect(new URL(JspUtils.getAbsoluteUrl(request, wp.param(ToolFilter.RETURN_PATH_PARAMETER, "/"))).toString());
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
.toolHat {
    display: none;
}
.toolHeader {
    background-color: transparent;
    border-style: none;
}
.toolHeader > .title {
    float: none;
    margin: 0 auto;
    text-align: center;
    width: 30em;
}
.toolFooter {
    background-position: top center;
    border-style: none;
    margin-left: auto;
    margin-right: auto;
    width: 30em;
}
.toolFooter .build {
    background-position: top center;
    text-align: center;
}
.widget {
    margin: 0 auto;
    width: 30em;
}
</style>

<div class="widget">
    <h1>Log In</h1>

    <%
    if (authError != null) {
        new HtmlWriter(wp.getWriter()).object(authError);
    }
    %>

    <form action="<%= wp.url("") %>" method="post">
        <div class="inputContainer">
            <div class="label">
                <label for="<%= wp.createId() %>">Email</label>
            </div>
            <div class="smallInput">
                <input class="autoFocus" id="<%= wp.getId() %>" name="email" type="text" value="<%= wp.h(email) %>">
            </div>
        </div>

        <div class="inputContainer">
            <div class="label">
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
