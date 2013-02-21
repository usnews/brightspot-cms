package com.psddev.cms.db;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.psddev.dari.db.DatabaseEnvironment;
import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;
import com.psddev.dari.util.TypeDefinition;

/** Controls the tool UI display. */
@ToolUi.FieldInternalNamePrefix("cms.ui.")
@Modification.Classes({ ObjectField.class, ObjectType.class })
public class ToolUi extends Modification<Object> {

    private boolean dropDown;
    private Boolean filterable;
    private boolean globalFilter;
    private String heading;
    private Boolean hidden;
    private String inputProcessorPath;
    private String inputSearcherPath;
    private String noteHtml;
    private String noteRendererClassName;
    private String placeholder;
    private Boolean referenceable;
    private Boolean readOnly;
    private boolean richText;
    private Boolean sortable;
    private Boolean suggestions;
    private Number suggestedMaximum;
    private Number suggestedMinimum;

    public boolean isDropDown() {
        return dropDown;
    }

    public void setDropDown(boolean dropDown) {
        this.dropDown = dropDown;
    }

    public Boolean getFilterable() {
        return filterable;
    }

    public void setFilterable(Boolean filterable) {
        this.filterable = filterable;
    }

    public boolean isEffectivelyFilterable() {
        Boolean filterable = getFilterable();

        if (filterable != null) {
            return filterable;
        }

        Object object = getOriginalObject();

        if (!(object instanceof ObjectField)) {
            return false;
        }

        ObjectField field = (ObjectField) object;

        if (isHidden() ||
                !ObjectField.RECORD_TYPE.equals(field.getInternalItemType()) ||
                field.isEmbedded()) {
            return false;
        }

        for (ObjectType type : field.getTypes()) {
            if (type.isEmbedded()) {
                return false;
            }
        }

        return true;
    }

    public boolean isGlobalFilter() {
        return globalFilter;
    }

    public void setGlobalFilter(boolean globalFilter) {
        this.globalFilter = globalFilter;
    }

    /** Returns the heading to display before this object. */
    public String getHeading() {
        return heading;
    }

    /** Sets the heading to display before this object. */
    public void setHeading(String heading) {
        this.heading = heading;
    }

    public boolean isHidden() {
        if (hidden == null) {
            hidden = ObjectUtils.to(Boolean.class, getState().get("cms.ui.isHidden"));
        }
        return hidden != null ? hidden : false;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getInputProcessorPath() {
        return inputProcessorPath;
    }

    public void setInputProcessorPath(String inputProcessorPath) {
        this.inputProcessorPath = inputProcessorPath;
    }

    public String getInputSearcherPath() {
        return inputSearcherPath;
    }

    public void setInputSearcherPath(String inputSearcherPath) {
        this.inputSearcherPath = inputSearcherPath;
    }

    public String getNoteHtml() {
        if (noteHtml == null) {
            setNoteHtml(StringUtils.escapeHtml(ObjectUtils.to(String.class, getState().get("cms.ui.note"))));
        }
        return noteHtml;
    }

    public void setNoteHtml(String noteHtml) {
        this.noteHtml = noteHtml;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends NoteRenderer> getNoteRendererClass() {
        Class<?> c = ObjectUtils.getClassByName(noteRendererClassName);
        return c != null && NoteRenderer.class.isAssignableFrom(c) ? (Class<? extends NoteRenderer>) c : null;
    }

    public void setNoteRendererClass(Class<? extends NoteRenderer> noteRendererClass) {
        this.noteRendererClassName = noteRendererClass != null ? noteRendererClass.getName() : null;
    }

    public String getEffectiveNoteHtml(Object object) {
        Class<? extends NoteRenderer> noteRendererClass = getNoteRendererClass();

        if (noteRendererClass == null) {
            return getNoteHtml();

        } else {
            NoteRenderer renderer = TypeDefinition.getInstance(noteRendererClass).newInstance();
            return renderer.render(object);
        }
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public boolean isReadOnly() {
        if (readOnly == null) {
            readOnly = ObjectUtils.to(Boolean.class, getState().get("cms.ui.isReadOnly"));
        }
        return readOnly != null ? readOnly : false;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isRichText() {
        return richText;
    }

    public void setRichText(boolean richText) {
        this.richText = richText;
    }

    public boolean isReferenceable() {
        if (referenceable == null) {
            referenceable = ObjectUtils.to(Boolean.class, getState().get("cms.ui.isReferenceable"));
        }
        return referenceable != null ? referenceable : false;
    }

    public void setReferenceable(boolean referenceable) {
        this.referenceable = referenceable;
    }

    public Boolean getSortable() {
        return sortable;
    }

    public void setSortable(Boolean sortable) {
        this.sortable = sortable;
    }

    public boolean isEffectivelySortable() {
        Boolean sortable = getSortable();

        if (sortable != null) {
            return sortable;
        }

        Object object = getOriginalObject();

        if (!(object instanceof ObjectField)) {
            return false;
        }

        ObjectField field = (ObjectField) object;

        if (isHidden()) {
            return false;
        }

        String fieldType = field.getInternalType();

        return ObjectField.DATE_TYPE.equals(fieldType) ||
                ObjectField.NUMBER_TYPE.equals(fieldType) ||
                ObjectField.TEXT_TYPE.equals(fieldType);
    }

    public Boolean getSuggestions() {
        return suggestions;
    }

    public boolean isEffectivelySuggestions() {
        return !Boolean.FALSE.equals(suggestions);
    }

    public void setSuggestions(Boolean suggestions) {
        this.suggestions = suggestions;
    }

    public Number getSuggestedMaximum() {
        return suggestedMaximum;
    }

    public void setSuggestedMaximum(Number suggestedMaximum) {
        this.suggestedMaximum = suggestedMaximum;
    }

    public Number getSuggestedMinimum() {
        return suggestedMinimum;
    }

    public void setSuggestedMinimum(Number suggestedMinimum) {
        this.suggestedMinimum = suggestedMinimum;
    }

    /**
     * Specifies whether the target field should be displayed as a drop-down
     * menu.
     */
    @Documented
    @ObjectField.AnnotationProcessorClass(DropDownProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface DropDown {
        boolean value() default true;
    }

    private static class DropDownProcessor implements ObjectField.AnnotationProcessor<DropDown> {

        @Override
        public void process(ObjectType type, ObjectField field, DropDown annotation) {
            field.as(ToolUi.class).setDropDown(annotation.value());
        }
    }

    /**
     * Specifies whether the target field should be offered as a filterable
     * field in search.
     */
    @Documented
    @ObjectField.AnnotationProcessorClass(FilterableProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Filterable {
        boolean value() default true;
    }

    private static class FilterableProcessor implements ObjectField.AnnotationProcessor<Filterable> {

        @Override
        public void process(ObjectType type, ObjectField field, Filterable annotation) {
            field.as(ToolUi.class).setFilterable(annotation.value());
        }
    }

    /**
     * Specifies whether the target type shows up as a filter that can be
     * applied to any types in search.
     */
    @Documented
    @Inherited
    @ObjectType.AnnotationProcessorClass(GlobalFilterProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface GlobalFilter {
        boolean value() default true;
    }

    private static class GlobalFilterProcessor implements ObjectType.AnnotationProcessor<GlobalFilter> {
        @Override
        public void process(ObjectType type, GlobalFilter annotation) {
            type.as(ToolUi.class).setGlobalFilter(annotation.value());
        }
    }

    /** Specifies the text to display before the target field. */
    @Documented
    @Inherited
    @ObjectField.AnnotationProcessorClass(HeadingProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Heading {
        String value();
    }

    private static class HeadingProcessor implements ObjectField.AnnotationProcessor<Heading> {
        @Override
        public void process(ObjectType type, ObjectField field, Heading annotation) {
            field.as(ToolUi.class).setHeading(annotation.value());
        }
    }

    /** Specifies whether the target is hidden in the UI. */
    @Documented
    @Inherited
    @ObjectField.AnnotationProcessorClass(HiddenProcessor.class)
    @ObjectType.AnnotationProcessorClass(HiddenProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.TYPE })
    public @interface Hidden {
        boolean value() default true;
    }

    private static class HiddenProcessor implements
            ObjectField.AnnotationProcessor<Hidden>,
            ObjectType.AnnotationProcessor<Hidden> {

        @Override
        public void process(ObjectType type, ObjectField field, Hidden annotation) {
            field.as(ToolUi.class).setHidden(annotation.value());
        }

        @Override
        public void process(ObjectType type, Hidden annotation) {
            type.as(ToolUi.class).setHidden(annotation.value());
        }
    }

    /**
     * Specifies the path to the processor used to render and update
     * the target field.
     */
    @Documented
    @Inherited
    @ObjectField.AnnotationProcessorClass(InputProcessorPathProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface InputProcessorPath {
        String value();
    }

    private static class InputProcessorPathProcessor implements ObjectField.AnnotationProcessor<InputProcessorPath> {
        @Override
        public void process(ObjectType type, ObjectField field, InputProcessorPath annotation) {
            field.as(ToolUi.class).setInputProcessorPath(annotation.value());
        }
    }

    /**
     * Specifies the path to the searcher used to find a value for
     * the target field.
     */
    @Documented
    @Inherited
    @ObjectField.AnnotationProcessorClass(InputSearcherPathProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface InputSearcherPath {
        String value();
    }

    private static class InputSearcherPathProcessor implements ObjectField.AnnotationProcessor<InputSearcherPath> {
        @Override
        public void process(ObjectType type, ObjectField field, InputSearcherPath annotation) {
            field.as(ToolUi.class).setInputSearcherPath(annotation.value());
        }
    }

    /** Specifies the note displayed along with the target in the UI. */
    @Documented
    @Inherited
    @ObjectField.AnnotationProcessorClass(NoteProcessor.class)
    @ObjectType.AnnotationProcessorClass(NoteProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.TYPE })
    public @interface Note {
        String value();
    }

    private static class NoteProcessor implements
            ObjectField.AnnotationProcessor<Note>,
            ObjectType.AnnotationProcessor<Note> {

        @Override
        public void process(ObjectType type, ObjectField field, Note annotation) {
            field.as(ToolUi.class).setNoteHtml(StringUtils.escapeHtml(annotation.value()));
        }

        @Override
        public void process(ObjectType type, Note annotation) {
            type.as(ToolUi.class).setNoteHtml(StringUtils.escapeHtml(annotation.value()));
        }
    }

    /** Renders the note displayed along with a type or a field. */
    public static interface NoteRenderer {

        /** Renders the note for the given {@code object}. */
        public String render(Object object);
    }

    /**
     * Specifies the class that can render the note displayed along with
     * the target in the UI.
     */
    @Documented
    @Inherited
    @ObjectField.AnnotationProcessorClass(NoteRendererClassProcessor.class)
    @ObjectType.AnnotationProcessorClass(NoteRendererClassProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.TYPE })
    public @interface NoteRendererClass {
        Class<? extends NoteRenderer> value();
    }

    private static class NoteRendererClassProcessor implements
            ObjectField.AnnotationProcessor<NoteRendererClass>,
            ObjectType.AnnotationProcessor<NoteRendererClass> {

        @Override
        public void process(ObjectType type, ObjectField field, NoteRendererClass annotation) {
            field.as(ToolUi.class).setNoteRendererClass(annotation.value());
        }

        @Override
        public void process(ObjectType type, NoteRendererClass annotation) {
            type.as(ToolUi.class).setNoteRendererClass(annotation.value());
        }
    }

    /**
     * Specifies the note, in raw HTML, displayed along with the target
     * in the UI.
     */
    @Documented
    @Inherited
    @ObjectField.AnnotationProcessorClass(NoteHtmlProcessor.class)
    @ObjectType.AnnotationProcessorClass(NoteHtmlProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.TYPE })
    public @interface NoteHtml {
        String value();
    }

    private static class NoteHtmlProcessor implements
            ObjectField.AnnotationProcessor<NoteHtml>,
            ObjectType.AnnotationProcessor<NoteHtml> {

        @Override
        public void process(ObjectType type, ObjectField field, NoteHtml annotation) {
            field.as(ToolUi.class).setNoteHtml(annotation.value());
        }

        @Override
        public void process(ObjectType type, NoteHtml annotation) {
            type.as(ToolUi.class).setNoteHtml(annotation.value());
        }
    }

    /** Specifies the target field's placeholder text. */
    @Documented
    @ObjectField.AnnotationProcessorClass(PlaceholderProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Placeholder {
        String value();
    }

    private static class PlaceholderProcessor implements ObjectField.AnnotationProcessor<Annotation> {
        @Override
        public void process(ObjectType type, ObjectField field, Annotation annotation) {
            field.as(ToolUi.class).setPlaceholder(annotation instanceof FieldPlaceholder ?
                    ((FieldPlaceholder) annotation).value() :
                    ((Placeholder) annotation).value());
        }
    }

    /** Specifies whether the target is read-only. */
    @Documented
    @ObjectField.AnnotationProcessorClass(ReadOnlyProcessor.class)
    @ObjectType.AnnotationProcessorClass(ReadOnlyProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ReadOnly {
        boolean value() default true;
    }

    private static class ReadOnlyProcessor implements
            ObjectField.AnnotationProcessor<ReadOnly>,
            ObjectType.AnnotationProcessor<ReadOnly> {

        @Override
        public void process(ObjectType type, ObjectField field, ReadOnly annotation) {
            field.as(ToolUi.class).setReadOnly(annotation.value());
        }

        @Override
        public void process(ObjectType type, ReadOnly annotation) {
            type.as(ToolUi.class).setReadOnly(annotation.value());
        }
    }

    /**
     * Specifies whether the instances of the target type can be referenced
     * by a {@linkplain com.psddev.dari.db.ReferentialText referential text}
     * object.
     */
    @Documented
    @Inherited
    @ObjectType.AnnotationProcessorClass(ReferenceableProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Referenceable {
        boolean value() default true;
    }

    private static class ReferenceableProcessor implements ObjectType.AnnotationProcessor<Referenceable> {
        @Override
        public void process(ObjectType type, Referenceable annotation) {
            type.as(ToolUi.class).setReferenceable(annotation.value());
        }
    }

    /**
     * Specifies whether the target field should offer rich-text editing
     * options.
     */
    @Documented
    @ObjectField.AnnotationProcessorClass(RichTextProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface RichText {
        boolean value() default true;
    }

    private static class RichTextProcessor implements ObjectField.AnnotationProcessor<RichText> {

        @Override
        public void process(ObjectType type, ObjectField field, RichText annotation) {
            field.as(ToolUi.class).setRichText(annotation.value());
        }
    }

    /**
     * Specifies whether the target field should be offered as a sortable
     * field in search.
     */
    @Documented
    @ObjectField.AnnotationProcessorClass(SortableProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Sortable {
        boolean value() default true;
    }

    private static class SortableProcessor implements ObjectField.AnnotationProcessor<Sortable> {

        @Override
        public void process(ObjectType type, ObjectField field, Sortable annotation) {
            field.as(ToolUi.class).setSortable(annotation.value());
        }
    }

    /** Specifies whether the target field should offer suggestions. */
    @Documented
    @ObjectField.AnnotationProcessorClass(SuggestionsProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Suggestions {
        boolean value() default true;
    }

    private static class SuggestionsProcessor implements ObjectField.AnnotationProcessor<Suggestions> {

        @Override
        public void process(ObjectType type, ObjectField field, Suggestions annotation) {
            field.as(ToolUi.class).setSuggestions(annotation.value());
        }
    }

    /** Specifies the suggested maximum size of the target field value. */
    @Documented
    @ObjectField.AnnotationProcessorClass(SuggestedMaximumProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface SuggestedMaximum {
        double value();
    }

    private static class SuggestedMaximumProcessor implements ObjectField.AnnotationProcessor<Annotation> {
        @Override
        public void process(ObjectType type, ObjectField field, Annotation annotation) {
            field.as(ToolUi.class).setSuggestedMaximum(annotation instanceof FieldSuggestedMaximum ?
                    ((FieldSuggestedMaximum) annotation).value() :
                    ((SuggestedMaximum) annotation).value());
        }
    }

    /** Specifies the suggested minimum size of the target field value. */
    @Documented
    @ObjectField.AnnotationProcessorClass(SuggestedMinimumProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface SuggestedMinimum {
        double value();
    }

    private static class SuggestedMinimumProcessor implements ObjectField.AnnotationProcessor<Annotation> {
        @Override
        public void process(ObjectType type, ObjectField field, Annotation annotation) {
            field.as(ToolUi.class).setSuggestedMinimum(annotation instanceof FieldSuggestedMinimum ?
                    ((FieldSuggestedMinimum) annotation).value() :
                    ((SuggestedMinimum) annotation).value());
        }
    }

    // --- Legacy ---

    private static final String FIELD_PREFIX = "cms.ui.";
    private static final String OPTION_PREFIX = "cms.ui.";

    // --- Type annotations ---

    /**
     * Specifies an array of compatible types that the target type may
     * switch to.
     */
    @Documented
    @Inherited
    @ObjectType.AnnotationProcessorClass(CompatibleTypesProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface CompatibleTypes {

        Class<?>[] value();
    }

    private static class CompatibleTypesProcessor implements ObjectType.AnnotationProcessor<CompatibleTypes> {

        @Override
        public void process(ObjectType type, CompatibleTypes annotation) {
            Set<ObjectType> compatibleTypes = new HashSet<ObjectType>();
            DatabaseEnvironment environment = type.getState().getDatabase().getEnvironment();
            for (Class<?> typeClass : annotation.value()) {
                ObjectType compatibleType = environment.getTypeByClass(typeClass);
                if (compatibleType != null) {
                    compatibleTypes.add(compatibleType);
                }
            }
            setCompatibleTypes(type, compatibleTypes);
        }
    }

    private static final String COMPATIBLE_TYPES_FIELD = FIELD_PREFIX + "compatibleTypes";

    /** Returns the set of types that the given {@code type} may switch to. */
    public static Set<ObjectType> getCompatibleTypes(ObjectType type) {
        Set<ObjectType> types = new HashSet<ObjectType>();
        Collection<?> ids = (Collection<?>) type.getState().getValue(COMPATIBLE_TYPES_FIELD);
        if (!ObjectUtils.isBlank(ids)) {
            DatabaseEnvironment environment = type.getState().getDatabase().getEnvironment();
            for (Object idObject : ids) {
                ObjectType compatibleType = environment.getTypeById(ObjectUtils.to(UUID.class, idObject));
                if (compatibleType != null) {
                    types.add(compatibleType);
                }
            }
        }
        return types;
    }

    /** Sets the set of types that the given {@code type} may switch to. */
    public static void setCompatibleTypes(ObjectType type, Iterable<ObjectType> compatibleTypes) {
        Set<UUID> compatibleTypeIds;
        if (ObjectUtils.isBlank(compatibleTypes)) {
            compatibleTypeIds = null;
        } else {
            compatibleTypeIds = new HashSet<UUID>();
            for (ObjectType compatibleType : compatibleTypes) {
                if (compatibleType != null) {
                    compatibleTypeIds.add(compatibleType.getId());
                }
            }
        }
        type.getState().putValue(COMPATIBLE_TYPES_FIELD, compatibleTypeIds);
    }

    // --- Field annotations ---

    /** Specifies the internal type used to render the target field. */
    @Documented
    @ObjectField.AnnotationProcessorClass(FieldDisplayTypeProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface FieldDisplayType {

        String value();
    }

    private static class FieldDisplayTypeProcessor implements ObjectField.AnnotationProcessor<FieldDisplayType> {

        @Override
        public void process(ObjectType type, ObjectField field, FieldDisplayType annotation) {
            setFieldDisplayType(field, annotation.value());
        }
    }

    private static final String FIELD_INTERNAL_TYPE_OPTION = OPTION_PREFIX + "internalType";

    /** Returns the internal type used to render the given {@code field}. */
    public static String getFieldDisplayType(ObjectField field) {
        return (String) field.getOptions().get(FIELD_INTERNAL_TYPE_OPTION);
    }

    /** Sets the internal type used to render the given {@code field}. */
    public static void setFieldDisplayType(ObjectField field, String type) {
        field.getOptions().put(FIELD_INTERNAL_TYPE_OPTION, type);
    }

    // ---

    /**
     * Specifies whether the values in the target field should be
     * sorted before being saved.
     */
    @Documented
    @ObjectField.AnnotationProcessorClass(FieldSortedProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface FieldSorted {
        boolean value() default true;
    }

    private static class FieldSortedProcessor implements ObjectField.AnnotationProcessor<FieldSorted> {
        @Override
        public void process(ObjectType type, ObjectField field, FieldSorted annotation) {
            setFieldSorted(field, annotation.value());
        }
    }

    private static final String FIELD_SORTED_OPTION = OPTION_PREFIX + "sorted";

    /**
     * Returns {@code true} if the values in the given {@code field}
     * should be sorted before being saved.
     */
    public static boolean isFieldSorted(ObjectField field) {
        return Boolean.TRUE.equals(field.getOptions().get(FIELD_SORTED_OPTION));
    }

    /**
     * Sets whether the values in the given {@code field} should be
     * sorted before being saved.
     */
    public static void setFieldSorted(ObjectField field, boolean isSorted) {
        field.getOptions().put(FIELD_SORTED_OPTION, isSorted);
    }

    // ---

    /**
     * Specifies whether the target field may only contain objects with
     * paths.
     */
    @Documented
    @ObjectField.AnnotationProcessorClass(OnlyPathedProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface OnlyPathed {

        boolean value() default true;
    }

    private static class OnlyPathedProcessor implements ObjectField.AnnotationProcessor<OnlyPathed> {

        @Override
        public void process(ObjectType type, ObjectField field, OnlyPathed annotation) {
            setOnlyPathed(field, annotation.value());
        }
    }

    private static final String IS_ONLY_PATHED_OPTION = OPTION_PREFIX + "isOnlyPathed";

    /**
     * Returns {@code true} if the given {@code field} only allows objects
     * with paths to be stored.
     */
    public static boolean isOnlyPathed(ObjectField field) {
        return Boolean.TRUE.equals(field.getOptions().get(IS_ONLY_PATHED_OPTION));
    }

    /**
     * Sets whether the given {@code field} only allows objects with paths
     * to be stored.
     */
    public static void setOnlyPathed(ObjectField field, boolean isOnlyPathed) {
        Map<String, Object> options = field.getOptions();
        if (isOnlyPathed) {
            options.put(IS_ONLY_PATHED_OPTION, Boolean.TRUE);
        } else {
            options.remove(IS_ONLY_PATHED_OPTION);
        }
    }

    // --- Deprecated ---

    /** @deprecated Use {@link #isHidden()} instead. */
    @Deprecated
    public static boolean isHidden(ObjectField field) {
        return field.as(ToolUi.class).isHidden();
    }

    /** @deprecated Use {@link #setHidden(boolean)} instead. */
    @Deprecated
    public static void setHidden(ObjectField field, boolean isHidden) {
        field.as(ToolUi.class).setHidden(isHidden);
    }

    /** @deprecated Use {@link #getNoteHtml} instead. */
    @Deprecated
    public static String getNote(ObjectField field) {
        return ObjectUtils.to(String.class, field.getState().get("cms.ui.note"));
    }

    /** @deprecated Use {@link #setNoteHtml} instead. */
    @Deprecated
    public static void setNote(ObjectField field, String note) {
        field.as(ToolUi.class).setNoteHtml(StringUtils.escapeHtml(note));
    }

    /** @deprecated Use {@link Placeholder} instead. */
    @Deprecated
    @Documented
    @ObjectField.AnnotationProcessorClass(PlaceholderProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface FieldPlaceholder {
        String value();
    }

    /** @deprecated Use {@link #getPlaceholder()} instead. */
    @Deprecated
    public static String getFieldPlaceholder(ObjectField field) {
        return field.as(ToolUi.class).getPlaceholder();
    }

    /** @deprecated Use {@link #setPlaceholder(String)} instead. */
    @Deprecated
    public static void setFieldPlaceholder(ObjectField field, String placeholder) {
        field.as(ToolUi.class).setPlaceholder(placeholder);
    }

    /** @deprecated Use {@link #isReadOnly()} instead. */
    @Deprecated
    public static boolean isReadOnly(ObjectField field) {
        return field.as(ToolUi.class).isReadOnly();
    }

    /** @deprecated Use {@link #setReadOnly(boolean)} instead. */
    @Deprecated
    public static void setReadOnly(ObjectField field, boolean isReadOnly) {
        field.as(ToolUi.class).setReadOnly(isReadOnly);
    }

    /** @deprecated Use {@link #isReferenceable()} instead. */
    @Deprecated
    public static boolean isReferenceable(ObjectType type) {
        return type.as(ToolUi.class).isReferenceable();
    }

    /** @deprecated Use {@link #setReferenceable(boolean)} instead. */
    public static void setReferenceable(ObjectType type, boolean isReferenceable) {
        type.as(ToolUi.class).setReferenceable(isReferenceable);
    }

    /** @deprecated Use {@link SuggestedMaximum} instead. */
    @Deprecated
    @Documented
    @ObjectField.AnnotationProcessorClass(SuggestedMaximumProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface FieldSuggestedMaximum {
        double value();
    }

    /** @deprecated Use {@link #getSuggestedMaximum()} instead. */
    @Deprecated
    public static Number getFieldSuggestedMaximum(ObjectField field) {
        return field.as(ToolUi.class).getSuggestedMaximum();
    }

    /** @deprecated Use {@link #setSuggestedMaximum(Number)} instead. */
    @Deprecated
    public static void setFieldSuggestedMaximum(ObjectField field, Number maximum) {
        field.as(ToolUi.class).setSuggestedMaximum(maximum);
    }

    /** @deprecated Use {@link SuggestedMinimum} instead. */
    @Deprecated
    @Documented
    @ObjectField.AnnotationProcessorClass(SuggestedMinimumProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface FieldSuggestedMinimum {
        double value();
    }

    /** @deprecated Use {@link #getSuggestedMinimum()} instead. */
    @Deprecated
    public static Number getFieldSuggestedMinimum(ObjectField field) {
        return field.as(ToolUi.class).getSuggestedMinimum();
    }

    /** @deprecated Use {@link #setSuggestedMinimum(Number)} instead. */
    @Deprecated
    public static void setFieldSuggestedMinimum(ObjectField field, Number minimum) {
        field.as(ToolUi.class).setSuggestedMinimum(minimum);
    }
}
