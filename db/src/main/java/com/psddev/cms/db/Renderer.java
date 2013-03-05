package com.psddev.cms.db;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.ObjectUtils;

public interface Renderer extends Recordable {

    /** Modification of a type to add rendering information. */
    @FieldInternalNamePrefix("cms.render.")
    public static class TypeModification extends Modification<ObjectType> {

        @InternalName("renderScript")
        private String modulePath;

        private String pagePath;

        // Returns the legacy rendering JSP.
        private String getDefaultRecordJsp() {
            return (String) getState().get("cms.defaultRecordJsp");
        }

        /**
         * Returns the servlet path used to render instances of this type
         * as a module.
         */
        public String getModulePath() {
            if (ObjectUtils.isBlank(modulePath)) {
                String jsp = getDefaultRecordJsp();

                if (!ObjectUtils.isBlank(jsp)) {
                    modulePath = jsp;
                }
            }

            return modulePath;
        }

        /**
         * Sets the servlet path used to render instances of this type
         * as a module.
         */
        public void setModulePath(String modulePath) {
            this.modulePath = modulePath;
        }

        /**
         * Returns the servlet path used to render instances of this type
         * as a page.
         */
        public String getPagePath() {
            return pagePath;
        }

        /**
         * Sets the servlet path used to render instances of this type
         * as a page.
         */
        public void setPagePath(String pagePath) {
            this.pagePath = pagePath;
        }

        // --- Deprecated ---

        private static final String FIELD_PREFIX = "cms.render.";

        /** @deprecated No replacement. */
        @Deprecated
        public static final String ENGINE_FIELD = FIELD_PREFIX + "renderEngine";

        /** @deprecated No replacement. */
        @Deprecated
        public static final String SCRIPT_FIELD = FIELD_PREFIX + "renderScript";

        @Deprecated
        @InternalName(ENGINE_FIELD)
        private String engine;

        /** @deprecated No replacement. */
        @Deprecated
        public String getEngine() {
            if (ObjectUtils.isBlank(engine)) {
                String jsp = getDefaultRecordJsp();
                if (!ObjectUtils.isBlank(jsp)) {
                    setEngine("JSP");
                }
            }
            return engine;
        }

        /** @deprecated No replacement. */
        @Deprecated
        public void setEngine(String engine) {
            this.engine = engine;
        }

        /** @deprecated Use {@link #getModulePath} instead. */
        @Deprecated
        public String getScript() {
            return getModulePath();
        }

        /** @deprecated Use {@link #setModulePath} instead. */
        @Deprecated
        public void setScript(String script) {
            setModulePath(script);
        }
    }

    /**
     * Specifies the servlet path used to render instances of the target type
     * as a module.
     */
    @Documented
    @Inherited
    @ObjectType.AnnotationProcessorClass(ModulePathProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface ModulePath {
        String value();
    }

    /**
     * Specifies the servlet path used to render instances of the target type
     * as a page.
     */
    @Documented
    @Inherited
    @ObjectType.AnnotationProcessorClass(PagePathProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface PagePath {
        String value();
    }

    // --- Deprecated ---

    /** @deprecated No replacement. */
    @Deprecated
    @Documented
    @Inherited
    @ObjectType.AnnotationProcessorClass(EngineProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Engine {
        String value();
    }

    /** @deprecated Use {@link ModulePath} instead. */
    @Deprecated
    @Documented
    @Inherited
    @ObjectType.AnnotationProcessorClass(ScriptProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Script {
        String value();
    }
}

class ModulePathProcessor implements ObjectType.AnnotationProcessor<Renderer.ModulePath> {
    @Override
    public void process(ObjectType type, Renderer.ModulePath annotation) {
        type.as(Renderer.TypeModification.class).setModulePath(annotation.value());
    }
}

class PagePathProcessor implements ObjectType.AnnotationProcessor<Renderer.PagePath> {
    @Override
    public void process(ObjectType type, Renderer.PagePath annotation) {
        type.as(Renderer.TypeModification.class).setPagePath(annotation.value());
    }
}

class EngineProcessor implements ObjectType.AnnotationProcessor<Renderer.Engine> {
    @Override
    public void process(ObjectType type, Renderer.Engine annotation) {
        type.as(Renderer.TypeModification.class).setEngine(annotation.value());
    }
}

class ScriptProcessor implements ObjectType.AnnotationProcessor<Renderer.Script> {
    @Override
    public void process(ObjectType type, Renderer.Script annotation) {
        type.as(Renderer.TypeModification.class).setScript(annotation.value());
    }
}
