package com.psddev.cms.db;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

@Operation.DisplayName("Script")
public class ScriptOperation extends Operation {

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
    public void evaluate(Variation variation, Profile profile, Object object) {

        String engineName = getEngine();
        ScriptEngine engine = ScriptUtils.getEngine(engineName);

        Bindings bindings = engine.createBindings();
        bindings.put("variation", variation);
        bindings.put("profile", profile);
        bindings.put("object", object);

        try {
            engine.eval(getScript(), bindings);
        } catch (ScriptException ex) {
            throw new RuntimeException(String.format(
                    "Unable to evaluate [%s] script!", engineName), ex);
        }
    }
}
