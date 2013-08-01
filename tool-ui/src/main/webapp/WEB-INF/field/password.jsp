<%@ page import="

com.psddev.cms.db.ToolAuthenticationPolicy,
com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.db.ToolUser,

com.psddev.dari.db.ObjectField,
com.psddev.dari.db.State,
com.psddev.dari.util.AuthenticationException,
com.psddev.dari.util.AuthenticationPolicy,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.Password,
com.psddev.dari.util.PasswordException,
com.psddev.dari.util.PasswordPolicy,
com.psddev.dari.util.Settings,
com.psddev.dari.util.ValidationException
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

State state = State.getInstance(request.getAttribute("object"));

ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
Object fieldValue = state.getValue(fieldName);

String inputName = (String) request.getAttribute("inputName");
String isNewName = inputName + "/isNew";
String currentPasswordName = inputName + "/currentPassword";
String password1Name = inputName + "/password1";
String password2Name = inputName + "/password2";

if ((Boolean) request.getAttribute("isFormPost")) {
    if (wp.boolParam(isNewName)) {
        String password = wp.param(password1Name);
        if (ObjectUtils.isBlank(password)) {
            state.addError(field, "Password can't be blank!");
        } else {
            if (!state.isNew()) {
                String currentPassword = wp.param(currentPasswordName);
                ToolUser user = wp.getUser();

                if (ObjectUtils.isBlank(currentPassword)) {
                    state.addError(field, "Must supply your current password!");

                } else {
                    AuthenticationPolicy authPolicy = AuthenticationPolicy.Static.getInstance(Settings.get(String.class, "cms/tool/authenticationPolicy"));
                    if (authPolicy == null) {
                        authPolicy = new ToolAuthenticationPolicy();
                    }
                    try {
                        authPolicy.authenticate(user.getEmail(), currentPassword);
                    } catch (AuthenticationException authError) {
                        state.addError(field, "Invalid current password!");
                    }
                }
            }

            if (!password.equals(wp.param(password2Name))) {
                state.addError(field, "Passwords don't match!");
            } else {
                try {
                    PasswordPolicy policy = PasswordPolicy.Static.getInstance(Settings.get(String.class, "cms/tool/passwordPolicy"));

                    String algorithm = null;
                    String salt = null;

                    if (state.get(fieldName) != null) {
                        Password current = Password.valueOf(String.valueOf(state.get(fieldName)));

                        algorithm = current.getAlgorithm();
                        salt = current.getSalt();
                    }

                    state.put(fieldName, Password.validateAndCreateCustom(policy, algorithm, salt, password).toString());
                } catch (PasswordException error) {
                    state.addError(field, error.getMessage());
                }
            }
        }
    }
    return;
}

// --- Presentation ---

String isNewInputId = wp.getId();

%><div class="inputSmall">
    <select class="toggleable" data-root=".inputSmall" id="<%= isNewInputId %>" name="<%= isNewName %>">
        <option data-hide=".passwordChange" value="false"<%= wp.boolParam(isNewName) ? "" : " selected" %>>Keep Same</option>
        <option data-show=".passwordChange" value="true"<%= wp.boolParam(isNewName) ? " selected" : "" %>>Change</option>
    </select>

    <div class="passwordChange" style="margin-top: 10px;">

        <% if (!state.isNew()) { %>
            <div>Your Current Password:</div>
            <input name="<%= currentPasswordName %>" type="password">
        <% } %>

        <div>New Password:</div>
        <input name="<%= password1Name %>" type="password">
        <div>Confirm Password:</div>
        <input name="<%= password2Name %>" type="password">
    </div>
</div>

<% if (state.isNew()) { %>
    <script type="text/javascript">
    if (typeof jQuery !== 'undefined') jQuery(function($) {
        $('#<%= isNewInputId %>').val('true').change();
    });
    </script>
<% } %>
