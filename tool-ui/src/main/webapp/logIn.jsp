<%@ page import="

com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.ToolFilter,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Database,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.JspUtils,
com.psddev.dari.util.Password,
com.psddev.dari.util.Settings,

java.net.MalformedURLException,
java.net.URL
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
String email = wp.param("email");
if (wp.isFormPost()) {

    ToolUser user = Database.Static.findUnique(wp.getDatabase(), ToolUser.class, "email", email);
    String password = wp.param("password");
    if (user != null) {
        if (!user.getPassword().check(password)) {
            user = null;
        }

    } else if (!ObjectUtils.isBlank(email)
            && Settings.get(boolean.class, "cms/tool/isAutoCreateUser")) {
        String name = email;
        int atAt = email.indexOf("@");
        if (atAt >= 0) {
            name = email.substring(0, atAt);
            if (ObjectUtils.isBlank(name)) {
                name = email;
            } else {
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
            }
        }

        user = new ToolUser();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(Password.create(password));
        user.save();
    }

    if (user == null) {
        wp.getErrors().add(new IllegalArgumentException("Oops! No user with that email and password."));

    } else {
        ToolFilter.logIn(request, response, user);
        try {
            wp.redirect(new URL(JspUtils.getAbsoluteUrl(
                    request, wp.param(ToolFilter.RETURN_PATH_PARAMETER, "/"))).toString());
        } catch (MalformedURLException e) {
            wp.redirect("/");
        }
        return;
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
    <% wp.include("/WEB-INF/errors.jsp"); %>
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
