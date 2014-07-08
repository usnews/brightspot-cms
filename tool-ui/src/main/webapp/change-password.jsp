<%@ page session="false" import="

com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.AuthenticationFilter,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.util.HtmlWriter,
com.psddev.dari.util.JspUtils,
com.psddev.dari.util.NewPasswordPolicy,
com.psddev.dari.util.Password,
com.psddev.dari.util.PasswordException,
com.psddev.dari.util.PasswordPolicy,
com.psddev.dari.util.Settings,
com.psddev.dari.util.StringUtils,
com.psddev.dari.util.UrlBuilder,

java.net.MalformedURLException,
java.net.URL
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
ToolUser user = ToolUser.Static.getByChangePasswordToken(wp.param(String.class, "changePasswordToken"));
if (user != null) {
    if (wp.isFormPost()) {
        String currentPassword = wp.param("current_password");
        String newPassword1 = wp.param("password1");
        String newPassword2 = wp.param("password2");

        try {
            if (StringUtils.isBlank(currentPassword)) {
                throw new PasswordException("Must supply your current password!");
            }
            if (StringUtils.isBlank(newPassword1) || StringUtils.isBlank(newPassword2) || !newPassword1.equals(newPassword2)) {
                throw new PasswordException("Passwords don't match!");
            }
            Password current = user.getPassword();
            if (!current.check(currentPassword)) {
                throw new PasswordException("Invalid current password!");
            }

            NewPasswordPolicy newPasswordPolicy = NewPasswordPolicy.Static.getInstance(Settings.get(String.class, "cms/tool/newPasswordPolicy"));
            PasswordPolicy passwordPolicy = null;
            if (newPasswordPolicy == null) {
                passwordPolicy = PasswordPolicy.Static.getInstance(Settings.get(String.class, "cms/tool/passwordPolicy"));
            }
            Password hashedPassword;
            if (newPasswordPolicy != null || (newPasswordPolicy == null && passwordPolicy == null)) {
                hashedPassword = Password.validateAndCreateCustom(newPasswordPolicy, user, current.getAlgorithm(), null, newPassword1);
            } else {
                hashedPassword = Password.validateAndCreateCustom(passwordPolicy, current.getAlgorithm(), null, newPassword1);
            }
            user.updatePassword(hashedPassword);
            user.setChangePasswordOnLogIn(false);
            user.save();

            AuthenticationFilter.Static.logIn(request, response, user);

            try {
                wp.redirect(new URL(JspUtils.getAbsoluteUrl(request, wp.param(AuthenticationFilter.RETURN_PATH_PARAMETER, wp.url("/")))).toString());
            } catch (MalformedURLException e) {
                wp.redirect("/");
            }

            return;
        } catch (PasswordException e) {
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
    <h1>Change Password</h1>

    <%
    if (user == null) {
    %>
    <div class="message">
        You've been inactive for too long.<br />
        Please <a href="<%= wp.url("logIn.jsp") %>">log in</a> again.
    </div>
    <%
    } else {
    %>

    <div class="message">
        Your password has expired! Please change your password.
    </div>

    <%
    if (error != null) {
        new HtmlWriter(wp.getWriter()).object(error);
    }
    %>

    <form action="<%= wp.url("") %>" method="post">
        <div class="inputContainer">
            <div class="inputLabel">
                <label for="<%= wp.createId() %>">Current Password</label>
            </div>
            <div class="inputSmall">
                <input class="autoFocus" id="<%= wp.getId() %>" name="current_password" type="password">
            </div>
        </div>

        <div class="inputContainer">
            <div class="inputLabel">
                <label for="<%= wp.createId() %>">New Password</label>
            </div>
            <div class="inputSmall">
                <input id="<%= wp.getId() %>" name="password1" type="password">
            </div>
        </div>

        <div class="inputContainer">
            <div class="inputLabel">
                <label for="<%= wp.createId() %>">Confirm Password</label>
            </div>
            <div class="inputSmall">
                <input id="<%= wp.getId() %>" name="password2" type="password">
            </div>
        </div>

        <div class="buttons">
            <button class="action">Change</button>
        </div>
    </form>

    <%
    }
    %>

</div>

<% wp.include("/WEB-INF/footer.jsp"); %>
