package com.psddev.cms.db;

import java.util.List;

import com.psddev.dari.db.State;

public final class ElFunctionUtils {

    private ElFunctionUtils() {
    }

    public static List<String> listLayouts(Object object, String field) {
        return State.getInstance(object).as(Renderable.Data.class).getListLayouts().get(field);
    }
}
