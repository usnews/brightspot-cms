package com.psddev.cms.tool;

import com.psddev.dari.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Areas within the CMS. */
public class Area extends Plugin {

    private Area parent;
    private String url;

    /** Returns the parent. */
    public Area getParent() {
        return parent;
    }

    /** Sets the parent. */
    public void setParent(Area parent) {
        this.parent = parent;
    }

    /** Returns the URL. */
    public String getUrl() {
        return url;
    }

    /** Sets the URL. */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the unique ID that represents this area for use in
     * permissions.
     */
    public String getPermissionId() {
        String key = getInternalName();
        for (Area parent = this; (parent = parent.getParent()) != null; ) {
            key = parent.getInternalName() + "/" + key;
        }
        return "area/" + key;
    }

    /**
     * Returns {@code true} if this area or any of its children is associated
     * with the given {@code path}.
     */
    public boolean isSelected(Tool tool, String path) {
        Tool areaTool = getTool();
        if (!ObjectUtils.equals(areaTool, tool)) {
            return false;
        }

        Area selected = null;
        List<Area> areas = areaTool.findPlugins(Area.class);

        for (Area area : areas) {
            if (ObjectUtils.equals(area.getUrl(), path)) {
                selected = area;
                break;
            }
        }

        if (selected == null) {
            Collections.sort(areas, new Comparator<Area>() {
                @Override
                public int compare(Area x, Area y) {
                    int xUrlLength = x.getUrl().length();
                    int yUrlLength = y.getUrl().length();
                    return xUrlLength < yUrlLength ? 1 : xUrlLength > yUrlLength ? -1 : 0;
                }
            });
            for (Area area : areas) {
                if (ObjectUtils.equals(area.getTool(), tool) &&
                        area.getUrl().endsWith("/") &&
                        path.startsWith(area.getUrl())) {
                    selected = area;
                    break;
                }
            }
        }

        if (selected == null) {
            for (Area area : areaTool.findTopAreas()) {
                selected = area;
                break;
            }
        }

        if (equals(selected)) {
            return true;
        }

        for (Area child : findChildren()) {
            if (child.isSelected(tool, path)) {
                return true;
            }
        }

        return false;
    }

    /** Returns {@code true} if this area has any children. */
    public boolean hasChildren() {
        for (Area area : getTool().findPlugins(Area.class)) {
            if (equals(area.getParent())) {
                return true;
            }
        }
        return false;
    }

    /** Finds the child areas. */
    public List<Area> findChildren() {
        List<Area> children = new ArrayList<Area>();
        for (Area area : getTool().findPlugins(Area.class)) {
            if (equals(area.getParent())) {
                children.add(area);
            }
        }
        return children;
    }

    // --- Deprecated ---

    /** @deprecated Use {@link #isSelected(Tool, String)} instead. */
    @Deprecated
    public boolean isSelected(String path) {
        return false;
    }
}
