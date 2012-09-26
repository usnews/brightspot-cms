package com.psddev.cms.db;

import com.psddev.dari.util.PullThroughCache;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

public abstract class ScriptUtils {

    private static final ScriptEngineManager MANAGER = new ScriptEngineManager();

    private static final List<ScriptEngineFactory> FACTORIES = MANAGER.getEngineFactories();

    private static final PullThroughCache<ScriptEngineFactory, ScriptEngine>
            THREAD_SAFE_ENGINES = new PullThroughCache<ScriptEngineFactory, ScriptEngine>() {

        @Override
        protected ScriptEngine produce(ScriptEngineFactory factory) {
            return factory.getScriptEngine();
        }
    };

    private static final PullThroughCache<ScriptEngineFactory, ThreadLocal<ScriptEngine>>
            THREAD_LOCAL_ENGINES = new PullThroughCache<ScriptEngineFactory, ThreadLocal<ScriptEngine>>() {

        @Override
        protected ThreadLocal<ScriptEngine> produce(final ScriptEngineFactory factory) {
            return new ThreadLocal<ScriptEngine>() {

                @Override
                protected ScriptEngine initialValue() {
                    return factory.getScriptEngine();
                }
            };
        }
    };

    public static ScriptEngine getEngine(String name) {

        for (ScriptEngineFactory factory : FACTORIES) {
            if (factory.getNames().contains(name)) {

                if (factory.getParameter("THREADING") != null) {
                    return THREAD_SAFE_ENGINES.get(factory);

                } else {
                    return THREAD_LOCAL_ENGINES.get(factory).get();
                }
            }
        }

        throw new IllegalArgumentException(String.format(
                "[%s] is not a valid script engine name!", name));
    }
}
