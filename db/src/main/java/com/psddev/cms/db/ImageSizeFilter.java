
package com.psddev.cms.db;

import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.AbstractFilter;
import com.psddev.dari.util.ImageEditor;
import com.psddev.dari.util.JavaImageEditor;
import com.psddev.dari.util.JavaImageServlet;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StorageItem;
import com.psddev.dari.util.StringUtils;
import com.psddev.dari.util.UuidFormatException;
import com.psddev.dari.util.UuidUtils;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

public class ImageSizeFilter extends AbstractFilter {
    protected static final String JAVA_IMAGE_SERVLET_PATH = JavaImageServlet.class.getAnnotation(RoutingFilter.Path.class).value();
    private static final String IMAGE_PATH_ATTRIBUTE = "_Image_Path";

    @Override
    protected void doRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        String path = request.getServletPath();

        String pathInfo = StringUtils.getPathInfo(path, JAVA_IMAGE_SERVLET_PATH);

        if (pathInfo != null && pathInfo.substring(1).contains("/")) {
            //check if path contains image size
            int imageSizeIndex = pathInfo.substring(1).indexOf("/");
            String imageSize = pathInfo.substring(1, 1 + imageSizeIndex);

            StandardImageSize standardImageSize = findStandardImageSize(imageSize);
            if (standardImageSize != null) {
                String imageId = pathInfo.substring(2 + imageSizeIndex, pathInfo.lastIndexOf("."));

                try {
                    UUID id = UuidUtils.fromString(imageId);
                    Object image = Query.from(Object.class).where("id = ?", id).first();
                    if (image != null) {
                        State state = State.getInstance(image);
                        StorageItem storageItem = null;
                        if (state.getPreview() != null) {
                            storageItem = state.getPreview();
                        } else {
                            storageItem = findStorageItem(state);
                        }

                        if (storageItem != null) {
                            ImageTag.Builder imageTagBuilder = new ImageTag.Builder(storageItem);
                            imageTagBuilder.setStandardImageSize(standardImageSize);

                            JavaImageEditor javaImageEditor = ObjectUtils.to(JavaImageEditor.class, ImageEditor.Static.getInstance(ImageEditor.JAVA_IMAGE_EDITOR_NAME));
                            String forwardPath = JAVA_IMAGE_SERVLET_PATH +
                                                    imageTagBuilder.toUrl()
                                                                   .substring(imageTagBuilder.toUrl().indexOf(javaImageEditor.getBaseUrl()) + javaImageEditor.getBaseUrl().length() - 1);

                            request.setAttribute(IMAGE_PATH_ATTRIBUTE, forwardPath);
                            HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request) {
                                private String newPath;
                                private String queryString;
                                private String url;
                                
                                @Override
                                public String getServletPath() {
                                    return !StringUtils.isBlank(this.getNewPath()) ? this.getNewPath() : super.getServletPath();
                                }
                                
                                @Override
                                public String getQueryString() {
                                    if (queryString == null) {
                                        queryString = ((String)this.getAttribute(IMAGE_PATH_ATTRIBUTE)).split("\\?")[1];
                                    }
                                    return StringUtils.isBlank(super.getQueryString()) ? queryString : super.getQueryString() + "&" + queryString;
                                }

                                @Override
                                public String getParameter(String string) {
                                   return string !=null && string.equals("url") ? getUrl() : super.getParameter(string);
                                }

                                public String getNewPath() {
                                    if (newPath == null) {
                                        newPath = ((String)this.getAttribute(IMAGE_PATH_ATTRIBUTE)).split("\\?")[0];
                                    }
                                    return newPath;
                                }

                                public String getUrl() {
                                    if (url == null) {
                                        url = this.getNewPath().substring(this.getNewPath().indexOf("?url=") + 5);
                                    }
                                    return url;
                                }

                            };

                            request = requestWrapper;
                        }
                    }

                } catch (UuidFormatException ex) {
                    //no valid UUID continue in the chian
                }
            }
        }

        chain.doFilter(request, response);
    }

    public static StandardImageSize findStandardImageSize(String internalSizeName) {
        for (StandardImageSize size : StandardImageSize.findAll()) {
            if (StringUtils.equals(size.getInternalName(), internalSizeName)) {
                return size;
            }
        }
        return null;
    }

    private static StorageItem findStorageItem(State state) {
        ObjectType objectType = state.getType();

        if (objectType != null) {
            for (ObjectField objectField : objectType.getFields()) {
                if (ObjectField.FILE_TYPE.equals(objectField.getInternalType())) {
                    String field = objectField.getInternalName();
                    Object fieldValue = state.get(field);
                    if (fieldValue instanceof StorageItem) {
                        return (StorageItem) fieldValue;
                    }
                }
            }
        }

        return null;
    }

}
