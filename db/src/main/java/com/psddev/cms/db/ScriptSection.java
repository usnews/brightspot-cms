package com.psddev.cms.db;

import java.util.Map;

public class ScriptSection extends Section {

    private String engine;
    private String script;

    public String getEngine() {
        return engine;
    }
    
    public void setEngine(String engine) {
        this.engine = engine;
    }
    
    public String getScript() {
        return script;
    }
    
    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public Map<String, Object> toDefinition() {
        Map<String, Object> map = super.toDefinition();
        map.put("engine", getEngine());
        map.put("script", getScript());
        return map;
    }
}
