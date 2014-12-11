package com.psddev.cms.tool;

import java.io.IOException;

import javax.servlet.ServletException;

import com.psddev.cms.db.ToolUi;
import com.psddev.dari.db.Modification;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.ObjectUtils;

public interface DashboardWidget extends Recordable {

    public void writeHtml(ToolPageContext page, Dashboard dashboard) throws IOException, ServletException;

    @FieldInternalNamePrefix("cms.dashboard.widget.")
    public static final class Data extends Modification<DashboardWidget> {

        @ToolUi.Hidden
        private String name;

        public String getName() {
            return !ObjectUtils.isBlank(name) ? name : getId().toString();
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPermissionId(Dashboard dashboard) {
            return "widget/" + getInternalName(dashboard);
        }

        public String getInternalName(Dashboard dashboard) {
            return dashboard.as(Dashboard.Data.class).getInternalName() + "." + getName();
        }
    }
}
