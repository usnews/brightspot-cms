package com.psddev.cms.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.psddev.dari.util.AuthenticationFailure;
import com.psddev.dari.util.HtmlObject;
import com.psddev.dari.util.HtmlWriter;

class ToolAuthenticationFailure implements AuthenticationFailure, HtmlObject {

    private List<String> errors;

    public ToolAuthenticationFailure(String error) {
        this.errors = new ArrayList<String>();
        this.errors.add(error);
    }

    public ToolAuthenticationFailure(List<String> errors) {
        this.errors = new ArrayList<String>(errors);
    }

    public List<String> getErrors() {
        if (errors == null) {
            errors = new ArrayList<String>();
        }
        return errors;
    }

    @Override
    public void format(HtmlWriter writer) throws IOException {
        writer.start("div", "class", "error message").start("ul");
        for (String error : getErrors()) {
            writer.start("li").string(error).end();
        }
        writer.end().end();
    }
}
