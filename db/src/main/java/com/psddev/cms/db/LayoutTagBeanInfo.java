package com.psddev.cms.db;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class LayoutTagBeanInfo extends SimpleBeanInfo {

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            return new PropertyDescriptor[] {
                    new PropertyDescriptor("class", LayoutTag.class, null, "setCssClass") };

        } catch (IntrospectionException error) {
            throw new IllegalStateException(error);
        }
    }
}
