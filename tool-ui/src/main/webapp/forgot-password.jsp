<%@ page session="false" import="

com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.AuthenticationFilter,
com.psddev.cms.tool.CmsTool,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Application,
com.psddev.dari.db.Query,
com.psddev.dari.util.HtmlWriter,
com.psddev.dari.util.JspUtils,
com.psddev.dari.util.MailMessage,
com.psddev.dari.util.MailProvider,
com.psddev.dari.util.NewPasswordPolicy,
com.psddev.dari.util.PasswordException,
com.psddev.dari.util.RoutingFilter,
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

PasswordException error = null;
String username = wp.param("username");
boolean submitted = false;

if (wp.isFormPost()) {
    try {
        if (StringUtils.isBlank(username)) {
            throw new PasswordException("Oops! No user with that username");
        }
        ToolUser user = Query.from(ToolUser.class).where("email = ? or username = ?", username, username).first();
        if (user == null) {
            throw new PasswordException("Oops! No user with that username");
        }
        String email = user.getEmail();
        if (StringUtils.isBlank(email) || email.indexOf("@") < 1) {
            throw new PasswordException("Oops! No email with that username");
        }
        String baseUrl = Settings.get(String.class, ToolPageContext.TOOL_URL_PREFIX_SETTING);
        if (StringUtils.isBlank(baseUrl)) {
            baseUrl = JspUtils.getAbsoluteUrl(request, "/");
        }

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(baseUrl);
        urlBuilder.append(RoutingFilter.Static.getApplicationPath(Application.Static.getInstance(CmsTool.class).getApplicationName()));
        urlBuilder.append(StringUtils.ensureStart("reset-password.jsp", "/"));
        String changePasswordToken = UUID.randomUUID().toString();

        String url = urlBuilder.toString();
        url = StringUtils.addQueryParameters(url, "rpr", changePasswordToken);

        StringBuilder body = new StringBuilder();
        body.append("<p>Dear ");
        body.append(user.getName());
        body.append(",</p>");
        body.append("<p>You recently requested to reset your password at ");
        body.append(baseUrl);
        body.append(".</p>");
        body.append("<p>Please click on the link below (expires in 24 hours) to reset your password:</p>");
        body.append("<a href=\"");
        body.append(url);
        body.append("\" target=\"_blank\">Password Reset</a><br />");
        body.append("<br />");
        body.append("<p>Thank you,<br />");
        body.append("Brightspot Support</p>");

        // TODO: make sender configurable
        MailProvider.Static.getDefault().send(new MailMessage(email).
                from("asung@perfectsensedigital.com").
                subject("Password reset request").
                bodyHtml(body.toString()));
        user.setChangePasswordToken(changePasswordToken);
        user.save();
        submitted = true;
    } catch (PasswordException e) {
        if (Settings.get(boolean.class, "cms/tool/suppressErrorMessage")) {
            submitted = true;
        } else {
            error = e;
        }
    }
}

// --- Presentation ---
wp.writeHeader(null, false);
%>

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
body.hasToolBroadcast {
    margin-top: 195px;
}
</style>

<div class="widget">
    <h1>Forgot Password</h1>

    <% if (submitted) { %>
        <div class="message message-info">
            <p>You'll receive email to reset password shortly.</p>
        </div>
    <% } else { %>

    <%
    if (error != null) {
        new HtmlWriter(wp.getWriter()).object(error);
    }
    %>

    <form action="<%= wp.url("") %>" method="post">
        <div class="inputContainer">
            <div class="inputLabel">
                <label for="<%= wp.createId() %>">Username</label>
            </div>
            <div class="inputSmall">
                <input class="autoFocus" id="<%= wp.getId() %>" name="username" type="text" value="<%= wp.h(username) %>">
            </div>
        </div>

        <div class="buttons">
            <button class="action">Submit</button>
            <a href="<%= wp.url("logIn.jsp", AuthenticationFilter.RETURN_PATH_PARAMETER, wp.param(AuthenticationFilter.RETURN_PATH_PARAMETER)) %>">Go Back To Log In Page</a>
        </div>
    </form>

    <%
    }
    %>

</div>

<% wp.include("/WEB-INF/footer.jsp"); %>
