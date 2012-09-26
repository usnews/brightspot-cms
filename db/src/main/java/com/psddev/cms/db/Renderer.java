package com.psddev.cms.db;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.util.ObjectUtils;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class Renderer {

    /** Modification of a type to add rendering information. */
    public static class TypeModification extends Modification<ObjectType> {

        private static final String FIELD_PREFIX = "cms.render.";

        public static final String ENGINE_FIELD = FIELD_PREFIX + "renderEngine";
        public static final String SCRIPT_FIELD = FIELD_PREFIX + "renderScript";

        private @InternalName(ENGINE_FIELD) String engine;
        private @InternalName(SCRIPT_FIELD) String script;

        /** Returns the legacy rendering JSP. */
        private String getDefaultRecordJsp() {
            return (String) getState().getValue("cms.defaultRecordJsp");
        }

        /** Returns the rendering engine. */
        public String getEngine() {
            if (ObjectUtils.isBlank(engine)) {
                String jsp = getDefaultRecordJsp();
                if (!ObjectUtils.isBlank(jsp)) {
                    setEngine("JSP");
                }
            }
            return engine;
        }

        /** Sets the rendering engine. */
        public void setEngine(String engine) {
            this.engine = engine;
        }

        /** Returns the rendering script. */
        public String getScript() {
            if (ObjectUtils.isBlank(script)) {
                String jsp = getDefaultRecordJsp();
                if (!ObjectUtils.isBlank(jsp)) {
                    setScript(jsp);
                }
            }
            return script;
        }

        /** Sets the rendering script. */
        public void setScript(String script) {
            this.script = script;
        }
    }

    /** Specifies the engine used to render instances of the target type. */
    @Documented
    @Inherited
    @ObjectType.AnnotationProcessorClass(EngineProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Engine {
        String value();
    }

    private static class EngineProcessor implements ObjectType.AnnotationProcessor<Engine> {
        @Override
        public void process(ObjectType type, Engine annotation) {
            type.as(TypeModification.class).setEngine(annotation.value());
        }
    }

    /** Specifies the script used to render instances of the target type. */
    @Documented
    @Inherited
    @ObjectType.AnnotationProcessorClass(ScriptProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Script {
        String value();
    }

    private static class ScriptProcessor implements ObjectType.AnnotationProcessor<Script> {
        @Override
        public void process(ObjectType type, Script annotation) {
            type.as(TypeModification.class).setScript(annotation.value());
        }
    }
}
