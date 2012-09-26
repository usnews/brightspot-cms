package com.psddev.cms.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ContainerSection extends Section {

    private String beginEngine;
    private String beginScript;
    private String endEngine;
    private String endScript;

    @Embedded
    private List<Section> children;

    public String getBeginEngine() {
        return beginEngine;
    }

    public void setBeginEngine(String beginEngine) {
        this.beginEngine = beginEngine;
    }

    public String getBeginScript() {
        return beginScript;
    }

    public void setBeginScript(String beginScript) {
        this.beginScript = beginScript;
    }

    public String getEndEngine() {
        return endEngine;
    }

    public void setEndEngine(String endEngine) {
        this.endEngine = endEngine;
    }

    public String getEndScript() {
        return endScript;
    }

    public void setEndScript(String endScript) {
        this.endScript = endScript;
    }

    public List<Section> getChildren() {
        if (children == null) {
            children = new ArrayList<Section>();
        }
        return children;
    }

    public void setChildren(List<Section> children) {
        this.children = children;
    }

    @Override
    public Map<String, Object> toDefinition() {
        Map<String, Object> map = super.toDefinition();

        map.put("beginEngine", getBeginEngine());
        map.put("beginScript", getBeginScript());
        map.put("endEngine", getEndEngine());
        map.put("endScript", getEndScript());

        List<Map<String, Object>> childMaps = new ArrayList<Map<String, Object>>();
        for (Section child : getChildren()) {
            childMaps.add(child.toDefinition());
        }
        map.put("children", childMaps);

        return map;
    }
}
