package com.psddev.cms.db;

import java.util.Map;

import com.psddev.dari.db.Query;
import com.psddev.dari.util.AuthenticationFailure;
import com.psddev.dari.util.AuthenticationPolicy;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.Password;
import com.psddev.dari.util.PasswordPolicy;
import com.psddev.dari.util.Settings;

public class ToolAuthenticationPolicy implements AuthenticationPolicy {

    public static final String NO_USER_PASS_EXISTS_FAILURE =
            "Oops! No user with that email and password.";

    @Override
    public final Object authenticate(String email, String password) {

        ToolUser user = Query.findUnique(ToolUser.class, "email", email);

        if (user != null) {
            if (!user.getPassword().check(password)) {
                return new ToolAuthenticationFailure(NO_USER_PASS_EXISTS_FAILURE);
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

            PasswordPolicy policy = PasswordPolicy.Static.getInstance(
                    Settings.get(String.class, PasswordPolicy.DEFAULT_PASSWORD_POLICY_SETTING));
            if (policy == null) {
                policy = new ToolPasswordPolicy();
            }
            user.setPassword(Password.create(password, policy));

            user.save();

        } else { // user is null
            return new ToolAuthenticationFailure(NO_USER_PASS_EXISTS_FAILURE);
        }

        return user;
    }

    @Override
    public void initialize(String settingsKey, Map<String, Object> settings) {
        // nothing to do...
    }

    /** Returns a default failure for when a more specific one does not exist. */
    public static AuthenticationFailure getDefaultAuthenticationFailure() {
        return new ToolAuthenticationFailure(NO_USER_PASS_EXISTS_FAILURE);
    }
}
