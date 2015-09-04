<%@ page session="false" import="

com.psddev.cms.db.ToolAuthenticationPolicy,
com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.AuthenticationFilter,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.util.AuthenticationException,
com.psddev.dari.util.AuthenticationPolicy,
com.psddev.dari.util.HtmlWriter,
com.psddev.dari.util.JspUtils,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.Settings,
com.psddev.dari.util.StringUtils,
com.psddev.dari.util.UrlBuilder,

java.net.MalformedURLException,
java.net.URL,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.getUser() != null) {
    AuthenticationFilter.Static.logOut(response);
    response.sendRedirect(new UrlBuilder(request).
            currentPath().
            currentParameters().
            toString());
    return;
}

AuthenticationException authError = null;
String username = wp.param("username");
String returnPath = wp.param(AuthenticationFilter.RETURN_PATH_PARAMETER);
ToolUser user = ToolUser.Static.getByTotpToken(wp.param(String.class, "totpToken"));

if (wp.isFormPost()) {
    try {

        if (user != null) {
            if (!user.verifyTotp(wp.param(int.class, "totpCode"))) {
                throw new AuthenticationException("The code you've entered is either invalid or has already been used.");
            }

        } else {
            AuthenticationPolicy authPolicy = AuthenticationPolicy.Static.getInstance(Settings.get(String.class, "cms/tool/authenticationPolicy"));

            if (authPolicy == null) {
                authPolicy = new ToolAuthenticationPolicy();
            }

            user = (ToolUser) authPolicy.authenticate(username, wp.param(String.class, "password"));

            if (user.isTfaEnabled()) {
                String totpToken = UUID.randomUUID().toString();

                user.setTotpToken(totpToken);
                user.save();
                wp.redirect("", "totpToken", totpToken);
                return;
            }
        }

        if (user.isChangePasswordOnLogIn()) {
            String changePasswordToken = UUID.randomUUID().toString();
            user.setChangePasswordToken(changePasswordToken);
            user.save();
            wp.redirect("change-password.jsp", "changePasswordToken", changePasswordToken, AuthenticationFilter.RETURN_PATH_PARAMETER, returnPath);
            return;
        }

        if (user.getChangePasswordToken() != null) {
            user.setChangePasswordToken(null);
            user.save();
        }

        AuthenticationFilter.Static.logIn(request, response, user);

        if (!StringUtils.isBlank(returnPath)) {
            try {
                wp.redirect(new URL(JspUtils.getAbsoluteUrl(request, returnPath)).toString());
            } catch (MalformedURLException e) {
                wp.redirect("/");
            }
        } else {
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
    box-shadow: none;
}
.toolTitle {
    float: none;
    height: 100px;
    margin: 30px 0 0 0;
    text-align: center;
    width: auto;
}
.toolTitle a img {
    max-width: 390px;
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
body.hasToolBroadcast {
    margin-top: 195px;
}
.inputContainer {
    background: transparent !important;
}
.buttons {
    border-top-style: none;
}
</style>

<div class="widget widget-logIn">
    <h1>Log In</h1>

    <%
    if (wp.param(boolean.class, "forced")) {
        wp.writeStart("div", "class", "message message-warning");
            wp.writeHtml(wp.localize(null, "login.inactive"));
        wp.writeEnd();
    }

    if (authError != null) {
        new HtmlWriter(wp.getWriter()).object(authError);
    }
    %>

    <% if (!Query.from(ToolUser.class).hasMoreThan(0)) { %>
        <div class="message message-info">
            <p><%= wp.h(wp.localize(null, "login.welcomeMessage")) %></p>
        </div>
    <% } %>

    <form action="<%= wp.url("", "forced", null) %>" method="post">
        <% if (user == null) { %>
            <div class="inputContainer">
                <div class="inputLabel">
                    <label for="<%= wp.createId() %>"><%= wp.h(wp.localize(null, "login.username")) %></label>
                </div>
                <div class="inputSmall">
                    <input class="autoFocus" id="<%= wp.getId() %>" name="username" type="text" value="<%= wp.h(username) %>" placeholder="<%= wp.h(wp.localize(null, "login.usernamePlaceholder")) %>">
                </div>
            </div>

            <div class="inputContainer">
                <div class="inputLabel">
                    <label for="<%= wp.createId() %>"><%= wp.h(wp.localize(null, "login.password")) %></label>
                </div>
                <div class="inputSmall">
                    <input id="<%= wp.getId() %>" name="password" type="password" placeholder="<%= wp.h(wp.localize(null, "login.password")) %>">
                </div>
            </div>

        <% } else { %>
            <div class="inputContainer">
                <div class="inputLabel">
                    <label for="<%= wp.createId() %>"><%= wp.h(wp.localize(null, "login.code")) %></label>
                </div>
                <div class="inputSmall">
                    <input class="autoFocus" id="<%= wp.getId() %>" name="totpCode" type="text" placeholder="<%= wp.h(wp.localize(null, "login.codePlaceholder")) %>">
                </div>
            </div>
        <% } %>

        <div class="buttons">
            <button class="action action-logIn"><%= wp.h(wp.localize(null, "login.loginButton")) %></button>
            <% if (!StringUtils.isBlank(Settings.get(String.class, "cms/tool/forgotPasswordEmailSender")) && user == null) {%>
            <a href="<%= wp.url("forgot-password.jsp", AuthenticationFilter.RETURN_PATH_PARAMETER, returnPath) %>"><%= wp.h(wp.localize(null, "login.forgotPassword")) %></a>
            <% } %>
        </div>
    </form>
</div>

<% wp.include("/WEB-INF/footer.jsp"); %>
