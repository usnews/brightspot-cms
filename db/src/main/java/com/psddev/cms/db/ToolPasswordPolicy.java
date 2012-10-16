package com.psddev.cms.db;

import java.util.Map;

import com.psddev.dari.util.PasswordPolicy;
import com.psddev.dari.util.ValidationException;

/** Default password policy which enforces no password requirements. */
public class ToolPasswordPolicy implements PasswordPolicy {

    @Override
    public void initialize(String settingsKey, Map<String, Object> settings) {
    }

    @Override
    public void validate(String password) throws ValidationException {
    }
}
