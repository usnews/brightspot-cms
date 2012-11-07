package com.psddev.cms.db;

import com.psddev.dari.db.Query;
import com.psddev.dari.util.AuthenticationException;
import com.psddev.dari.util.AuthenticationPolicy;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.Password;
import com.psddev.dari.util.PasswordException;
import com.psddev.dari.util.PasswordPolicy;
import com.psddev.dari.util.Settings;

import java.util.Map;

public class ToolAuthenticationPolicy implements AuthenticationPolicy {

    @Override
    public ToolUser authenticate(String email, String password) throws AuthenticationException {
        ToolUser user = Query.findUnique(ToolUser.class, "email", email);

        if (user != null) {
            if (user.getPassword().check(password)) {
                return user;
            }

        } else if (!ObjectUtils.isBlank(email) &&
                Settings.get(boolean.class, "cms/tool/isAutoCreateUser")) {
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

            PasswordPolicy policy = PasswordPolicy.Static.getInstance(Settings.get(String.class, "cms/tool/passwordPolicy"));
            Password hashedPassword;

            try {
                hashedPassword = Password.validateAndCreateCustom(policy, null, null, password);
            } catch (PasswordException error) {
                throw new AuthenticationException(error);
            }

            user = new ToolUser();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(hashedPassword);
            user.save();

            return user;
        }

        throw new AuthenticationException(
                "Oops! No user with that email and password.");
    }

    @Override
    public void initialize(String settingsKey, Map<String, Object> settings) {
    }
}
