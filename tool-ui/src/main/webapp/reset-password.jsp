<%@ page session="false" import="

com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.AuthenticationFilter,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.util.HtmlWriter,
com.psddev.dari.util.NewPasswordPolicy,
com.psddev.dari.util.Password,
com.psddev.dari.util.PasswordException,
com.psddev.dari.util.PasswordPolicy,
com.psddev.dari.util.Settings,
com.psddev.dari.util.StringUtils,
com.psddev.dari.util.UrlBuilder
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
ToolUser user = ToolUser.Static.getByChangePasswordToken(wp.param(String.class, "rpr"));
if (user != null) {
    if (wp.isFormPost()) {
        String newPassword1 = wp.param("password1");
        String newPassword2 = wp.param("password2");

        try {
            if (StringUtils.isBlank(newPassword1) || StringUtils.isBlank(newPassword2) || !newPassword1.equals(newPassword2)) {
                throw new PasswordException("Passwords don't match!");
            }
            Password current = user.getPassword();
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

            wp.redirect("/");
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
    <h1>Reset Password</h1>

    <%
    if (user == null) {
    %>
    <div class="message">
        The reset password request is no longer valid. <br />
        Please use this <a href="<%= wp.url("forgot-password.jsp") %>">link</a> to reset password again.
    </div>
    <%
    } else {
    %>

    <%
    if (error != null) {
        new HtmlWriter(wp.getWriter()).object(error);
    }
    %>

    <form action="<%= wp.url("") %>" method="post">

        <div class="inputContainer">
            <div class="inputLabel">
                <label for="<%= wp.createId() %>">New Password</label>
            </div>
            <div class="inputSmall">
                <input class="autoFocus" id="<%= wp.getId() %>" name="password1" type="password">
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
            <button class="action">Reset</button>
            <a href="<%= wp.url("logIn.jsp") %>">Go Back To Log In Page</a>
        </div>
    </form>

    <%
    }
    %>

</div>

<% wp.include("/WEB-INF/footer.jsp"); %>
