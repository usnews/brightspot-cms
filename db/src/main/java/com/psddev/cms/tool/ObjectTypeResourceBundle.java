package com.psddev.cms.tool;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.psddev.cms.db.ToolUi;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.util.CodeUtils;
import com.psddev.dari.util.CompactMap;
import com.psddev.dari.util.ObjectUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.Set;

class ObjectTypeResourceBundle extends ListResourceBundle {

    private static final LoadingCache<ObjectType, ObjectTypeResourceBundle> BUNDLES = CacheBuilder
            .newBuilder()
            .weakKeys()
            .build(new CacheLoader<ObjectType, ObjectTypeResourceBundle>() {

                @Override
                @ParametersAreNonnullByDefault
                public ObjectTypeResourceBundle load(ObjectType type) throws Exception {
                    return new ObjectTypeResourceBundle(type);
                }
            });

    static {
        CodeUtils.addRedefineClassesListener(classes ->
                classes.stream()
                        .map(ObjectType::getInstance)
                        .filter(type -> type != null)
                        .forEach(BUNDLES::invalidate));
    }

    private final Map<String, Object> map;
    private final Object[][] contents;

    public static ObjectTypeResourceBundle getInstance(ObjectType type) {
        return BUNDLES.getUnchecked(type);
    }

    private ObjectTypeResourceBundle(ObjectType type) {
        Map<String, Object> map = new CompactMap<>();

        map.put("displayName", type.getDisplayName());

        Set<String> tabs = new HashSet<>();

        for (ObjectField field : type.getFields()) {
            String tab = field.as(ToolUi.class).getTab();

            if (!ObjectUtils.isBlank(tab)) {
                tabs.add(tab);
            }

            map.put("field." + field.getInternalName(), field.getDisplayName());
        }

        for (String tab : tabs) {
            map.put("tab." + tab, tab);
        }

        map.put("tab.Main", "Main");

        this.map = ImmutableMap.copyOf(map);
        this.contents = map.entrySet().stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .toArray(Object[][]::new);
    }

    public Map<String, Object> getMap() {
        return map;
    }

    @Override
    protected Object[][] getContents() {
        return contents;
    }
}
