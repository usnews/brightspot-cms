package com.psddev.cms.db;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.ObjectUtils;

public interface Renderer extends Recordable {

    /** Global modification that stores rendering hints. */
    @FieldInternalNamePrefix("cms.renderable.")
    public static class Data extends Modification<Object> {

        public Map<String, List<String>> listLayouts;

        public Map<String, List<String>> getListLayouts() {
            if (listLayouts == null) {
                listLayouts = new HashMap<String, List<String>>();
            }
            return listLayouts;
        }

        public void setListLayouts(Map<String, List<String>> listLayouts) {
            this.listLayouts = listLayouts;
        }
    }

    /** Type modification that stores how objects should be rendered. */
    @FieldInternalNamePrefix("cms.render.")
    public static class TypeModification extends Modification<ObjectType> {

        @InternalName("renderScript")
        private String path;

        private String layoutPath;

        // Returns the legacy rendering JSP.
        private String getDefaultRecordJsp() {
            return (String) getState().get("cms.defaultRecordJsp");
        }

        /** Returns the servlet path used to render instances of this type. */
        public String getPath() {
            if (ObjectUtils.isBlank(path)) {
                String jsp = getDefaultRecordJsp();

                if (!ObjectUtils.isBlank(jsp)) {
                    path = jsp;
                }
            }

            return path;
        }

        /** Returns the servlet path used to render instances of this type. */
        public void setPath(String path) {
            this.path = path;
        }

        /**
         * Returns the servlet path used to render the layout around the
         * instances of this type.
         */
        public String getLayoutPath() {
            return layoutPath;
        }

        /**
         * Sets the servlet path used to render the layout around the
         * instances of this type.
         */
        public void setLayoutPath(String layoutPath) {
            this.layoutPath = layoutPath;
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

        /** @deprecated Use {@link #getPath} instead. */
        @Deprecated
        public String getScript() {
            return getPath();
        }

        /** @deprecated Use {@link #setPath} instead. */
        @Deprecated
        public void setScript(String script) {
            setPath(script);
        }
    }

    /**
     * Field modification that stores how field values should be
     * rendered.
     */
    public static class FieldData extends Modification<ObjectField> {

        private Map<String, List<String>> listLayouts;

        public Map<String, List<String>> getListLayouts() {
            if (listLayouts == null) {
                listLayouts = new HashMap<String, List<String>>();
            }
            return listLayouts;
        }

        public void setListLayouts(Map<String, List<String>> listLayouts) {
            this.listLayouts = listLayouts;
        }
    }

    /**
     * Specifies the servlet path used to render instances of the target type
     * as a module.
     */
    @Documented
    @Inherited
    @ObjectType.AnnotationProcessorClass(PathProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Path {
        String value();
    }

    /**
     * Specifies the servlet path used to render instances of the target type
     * as a page.
     */
    @Documented
    @Inherited
    @ObjectType.AnnotationProcessorClass(LayoutPathProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface LayoutPath {
        String value();
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ListLayout {
        String name();
        Class<?>[] itemClasses();
    }

    @Documented
    @Inherited
    @ObjectField.AnnotationProcessorClass(ListLayoutsProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ListLayouts {
        String[] value() default { };
        ListLayout[] map() default { };
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

    /** @deprecated Use {@link Path} instead. */
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

class PathProcessor implements ObjectType.AnnotationProcessor<Renderer.Path> {
    @Override
    public void process(ObjectType type, Renderer.Path annotation) {
        type.as(Renderer.TypeModification.class).setPath(annotation.value());
    }
}

class LayoutPathProcessor implements ObjectType.AnnotationProcessor<Renderer.LayoutPath> {
    @Override
    public void process(ObjectType type, Renderer.LayoutPath annotation) {
        type.as(Renderer.TypeModification.class).setLayoutPath(annotation.value());
    }
}

class ListLayoutsProcessor implements ObjectField.AnnotationProcessor<Renderer.ListLayouts> {
    @Override
    public void process(ObjectType type, ObjectField field, Renderer.ListLayouts annotation) {
        String[] value = annotation.value();
        Renderer.ListLayout[] map = annotation.map();

        Map<String, List<String>> listLayouts = field.as(Renderer.FieldData.class).getListLayouts();

        for (String layoutName : value) {
            listLayouts.put(layoutName, new ArrayList<String>());
        }

        for (Renderer.ListLayout layout : map) {
            List<String> layoutItems = new ArrayList<String>();
            listLayouts.put(layout.name(), layoutItems);

            for (Class<?> itemClass : layout.itemClasses()) {
                layoutItems.add(itemClass.getName());
            }
        }
    }
}

@Deprecated
class EngineProcessor implements ObjectType.AnnotationProcessor<Renderer.Engine> {
    @Override
    public void process(ObjectType type, Renderer.Engine annotation) {
        type.as(Renderer.TypeModification.class).setEngine(annotation.value());
    }
}

@Deprecated
class ScriptProcessor implements ObjectType.AnnotationProcessor<Renderer.Script> {
    @Override
    public void process(ObjectType type, Renderer.Script annotation) {
        type.as(Renderer.TypeModification.class).setScript(annotation.value());
    }
}
