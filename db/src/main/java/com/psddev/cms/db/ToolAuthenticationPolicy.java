package com.psddev.cms.db;

import java.util.Map;

import javax.naming.ldap.LdapContext;

import com.psddev.dari.db.Query;
import com.psddev.dari.util.AuthenticationException;
import com.psddev.dari.util.AuthenticationPolicy;
import com.psddev.dari.util.LdapUtils;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.Password;
import com.psddev.dari.util.PasswordException;
import com.psddev.dari.util.PasswordPolicy;
import com.psddev.dari.util.Settings;

public class ToolAuthenticationPolicy implements AuthenticationPolicy {

    @Override
    public ToolUser authenticate(String email, String password) throws AuthenticationException {
        ToolUser user = Query.findUnique(ToolUser.class, "email", email);
        LdapContext context = LdapUtils.createContext();

        if (context != null &&
                LdapUtils.authenticate(context, email, password)) {
            if (user == null) {
                user = new ToolUser();
                user.setName(email);
                user.setEmail(email);
                user.setExternal(true);
                user.save();
            }

            return user;
        }

        if (user != null) {
            if (user.getPassword().check(password)) {
                return user;
            }

        } else if (!ObjectUtils.isBlank(email) &&
                (ObjectUtils.coalesce(
                        Settings.get(Boolean.class, "cms/tool/autoCreateUser"),
                        Settings.get(boolean.class, "cms/tool/isAutoCreateUser")) ||
                !Query.from(ToolUser.class).hasMoreThan(0))) {
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
