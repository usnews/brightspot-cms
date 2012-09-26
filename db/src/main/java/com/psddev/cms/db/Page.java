package com.psddev.cms.db;

import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.State;

import com.psddev.dari.util.ObjectUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Page extends Content {
    
    @Indexed(unique = true)
    @Required
    private String name;

    @DisplayName("Layout")
    @InternalName("layout.v2")
    @ToolUi.FieldDisplayType("layout")
    private Layout layout;

    /** Returns the unique name. */
    public String getName() {
        return name;
    }

    /** Sets the unique name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the layout. */
    public Layout getLayout() {
        if (layout == null) {
            Section legacySection = resolveReference(Section.class, getState().getValue("layout"));
            if (legacySection != null) {
                layout = new Layout();
                layout.setOutermostSection(convertLegacySection(layout, legacySection));
            }
        }
        return layout;
    }

    /** Sets the layout. */
    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    /** Returns an iterable of all the sections in this page. */
    public Iterable<Section> findSections() {
        List<Section> sections = new ArrayList<Section>();
        Layout layout = getLayout();
        if (layout != null) {
            addSections(sections, layout.getOutermostSection());
        }
        return sections;
    }

    private void addSections(List<Section> sections, Section section) {
        if (section != null) {
            sections.add(section);
            if (section instanceof ContainerSection) {
                for (Section child
                        : ((ContainerSection) section).getChildren()) {
                    addSections(sections, child);
                }
            }
        }
    }

    @Embedded
    public static class Layout extends Record {

        @Embedded
        private Section outermostSection;

        public Section getOutermostSection() {
            return outermostSection;
        }

        public void setOutermostSection(Section section) {
            this.outermostSection = section;
        }

        public static Layout fromDefinition(Page page, Map<String, Object> map) {
            Layout layout = new Layout();
            Object outermost = parseDefinition(page, layout, map.get("outermostSection"));
            if (outermost instanceof Section) {
                layout.setOutermostSection((Section) outermost);
            }
            return layout;
        }

        public static Object parseDefinition(Page page, Layout layout, Object object) {

            if (object instanceof List) {
                List<Object> list = new ArrayList<Object>();
                for (Object e : (List<Object>) object) {
                    Object parsed = parseDefinition(page, layout, e);
                    if (parsed != null) {
                        list.add(parsed);
                    }
                }
                return list;

            } else if (object instanceof Map) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                for (Map.Entry<String, Object> e : ((Map<String, Object>) object).entrySet()) {
                    map.put(e.getKey(), parseDefinition(page, layout, e.getValue()));
                }

                if (Boolean.TRUE.equals(map.get("_isIgnore"))) {
                    return null;
                }

                ObjectType type = ObjectType.getInstance((String) map.get("_type"));
                if (type == null) {
                    return map;
                }

                UUID sectionPageId = ObjectUtils.to(UUID.class, map.get("page"));
                Object section;
                if (!Boolean.TRUE.equals(map.get("isShareable"))
                        && !page.getId().equals(sectionPageId)) {
                    section = type.createObject(null);
                } else {
                    UUID id = ObjectUtils.to(UUID.class, map.get("_id"));
                    section = Query.findById(Object.class, id);
                    if (section == null) {
                        section = type.createObject(id);
                    } else if (!(section instanceof Section)) {
                        section = type.createObject(null);
                    } else {
                        State.getInstance(section).setType(type);
                    }
                }

                if (section instanceof Section) {
                    map.remove("_type");
                    map.remove("_id");
                    State state = State.getInstance(section);
                    state.getValues().putAll(map);
                    if (((Section) section).isShareable()) {
                        state.save();
                    } else {
                        state.setId(null);
                        state.setStatus(null);
                    }
                    return section;
                }
            }

            return object;
        }

        public Map<String, Object> toDefinition() {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            Section outermostSection = getOutermostSection();
            if (outermostSection != null) {
                map.put("outermostSection", outermostSection.toDefinition());
            }
            return map;
        }
    }

    private Section convertLegacySection(Layout layout, Section section) {

        if (section == null || section.getClass() != Section.class) {
            return section;
        }

        State state = State.getInstance(section);
        Section newSection;

        String orientation = (String) state.getValue("orientation");
        boolean isHorizontal = "HORIZONTAL".equals(orientation);
        if (isHorizontal || "VERTICAL".equals(orientation)) {

            ContainerSection container = isHorizontal
                    ? new HorizontalContainerSection()
                    : new VerticalContainerSection();

            String beginJsp = (String) state.getValue("beginJsp");
            if (!ObjectUtils.isBlank(beginJsp)) {
                container.setBeginEngine("JSP");
                container.setBeginScript(beginJsp);
            } else {
                String beginText = (String) state.getValue("beginText");
                if (!ObjectUtils.isBlank(beginText)) {
                    container.setBeginEngine("RawText");
                    container.setBeginScript(beginText);
                }
            }

            String endJsp = (String) state.getValue("endJsp");
            if (!ObjectUtils.isBlank(endJsp)) {
                container.setEndEngine("JSP");
                container.setEndScript(endJsp);
            } else {
                String endText = (String) state.getValue("endText");
                if (!ObjectUtils.isBlank(endText)) {
                    container.setEndEngine("RawText");
                    container.setEndScript(endText);
                }
            }

            List<Object> childReferences
                    = (List<Object>) state.getValue("children");
            if (!ObjectUtils.isBlank(childReferences)) {
                for (Object childReference : childReferences) {
                    Section child = convertLegacySection(
                            layout, resolveReference(Section.class,
                            childReference));
                    if (child != null) {
                        container.getChildren().add(child);
                    }
                }
            }

            newSection = container;

        } else {

            Object object = resolveReference(
                    Object.class, state.getValue("record"));

            ScriptSection scriptSection;
            if ("PLACEHOLDER".equals(orientation)) {
                scriptSection = new MainSection();

            } else if (object == null) {
                scriptSection = new ScriptSection();

            } else {
                ContentSection contentSection = new ContentSection();
                contentSection.setContent(object);
                scriptSection = contentSection;
            }

            String recordJsp = (String) state.getValue("recordJsp");
            if (!ObjectUtils.isBlank(recordJsp)) {
                scriptSection.setEngine("JSP");
                scriptSection.setScript(recordJsp);
            } else {
                String recordText = (String) state.getValue("recordText");
                if (!ObjectUtils.isBlank(recordText)) {
                    scriptSection.setEngine("RawText");
                    scriptSection.setScript(recordText);
                }
            }

            newSection = scriptSection;
        }

        newSection.setName((String) state.getValue("name"));
        return newSection;
    }

    private <T> T resolveReference(Class<T> objectClass, Object reference) {
        if (reference instanceof Map) {
            return Query.findById(objectClass, ObjectUtils.to(UUID.class,
                    ((Map<String, Object>) reference).get("_ref")));
        } else if (objectClass.isInstance(reference)) {
            return (T) reference;
        } else {
            return null;
        }
    }
}
