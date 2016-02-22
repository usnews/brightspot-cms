package com.psddev.cms.view;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skeletal {@link ViewTemplateLoader} implementation that implements
 * {@link #getLastModified(String)} based on the URL returned from
 * {@link #getTemplateAsURL(String)} implemented by a sub-class.
 */
public abstract class AbstractViewTemplateLoader implements ViewTemplateLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractViewTemplateLoader.class);

    /**
     * Returns the template located at the named path as a URL.
     *
     * @param path the path to the template.
     * @return the template URL.
     * @throws MalformedURLException
     */
    protected abstract URL getTemplateAsURL(String path) throws MalformedURLException;

    @Override
    public long getLastModified(String path) {

        URL resource = null;
        try {
            resource = getTemplateAsURL(path);
        } catch (MalformedURLException e) {
            // do nothing
        }

        if (resource == null) {
            LOGGER.warn("Could not load resource: %s", path);
            return -1;
        }

        URLConnection uc = null;
        try {
            uc = resource.openConnection();

            if (uc instanceof JarURLConnection) {
                URL jarURL = ((JarURLConnection) uc).getJarFileURL();
                if (jarURL.getProtocol().equals("file")) {
                    uc = null;
                    String file = jarURL.getFile();
                    return new File(file).lastModified();
                }
            }

            return uc.getLastModified();

        } catch (IOException ex) {
            LOGGER.warn("Could not get last modified date of: %s", resource);
            return -1;

        } finally {
            try {
                if (uc != null) {
                    InputStream is = uc.getInputStream();
                    if (is != null) {
                        is.close();
                    }
                }
            } catch (IOException e) {
                LOGGER.warn("Could not close: %s", resource);
            }
        }
    }
}
