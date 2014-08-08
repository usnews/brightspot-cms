package com.psddev.cms.db;

import com.psddev.dari.util.ClassFinder;
import com.psddev.dari.util.CollectionUtils;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StorageItem;
import com.psddev.image.HotSpot;
import com.psddev.image.HotSpots;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageHotSpotCrop {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageHotSpotCrop.class);

    public static List<Integer> hotSpotCrop(StorageItem item, Integer cropWidth, Integer cropHeight) {
        //TODO: add support for either cropWidth or cropHeight being null
        if (item != null &&
            cropWidth != null &&
            cropHeight != null &&
            item.getMetadata().containsKey("height") &&
            item.getMetadata().containsKey("width")) {

            //find metadata keys
            Set<Class<? extends HotSpots>> hotSpotClasses = ClassFinder.Static.findClasses(HotSpots.class);
            if (hotSpotClasses != null) {
                List<HotSpot> hotSpots = new ArrayList<HotSpot>();
                for (Class<? extends HotSpots> hotSpotClass : hotSpotClasses) {
                    try {

                        HotSpots hotSpotsData = hotSpotClass.newInstance();
                        hotSpots.addAll(hotSpotsData.getHotSpots(item));
                    } catch (SecurityException ex) {
                        LOGGER.error("Error reading hotspot", ex);
                    } catch (InstantiationException ex) {
                        LOGGER.error("Error reading hotspot", ex);
                    } catch (IllegalAccessException ex) {
                        LOGGER.error("Error reading hotspot", ex);
                    } catch (IllegalArgumentException ex) {
                        LOGGER.error("Error reading hotspot", ex);
                    }
                }

                if (!ObjectUtils.isBlank(hotSpots)) {
                    Integer x2 = null;
                    Integer y2 = null;
                    Integer imageHeight = ObjectUtils.to(Integer.class, item.getMetadata().get("height"));
                    Integer imageWidth = ObjectUtils.to(Integer.class, item.getMetadata().get("width"));

                    if (item.getMetadata().containsKey("rotate")) {
                        Integer angle = ObjectUtils.to(Integer.class, item.getMetadata().get("rotate"));
                        if (angle % 90 == 0 && angle % 180 != 0) {
                            Integer rotate = imageHeight;
                            imageHeight = imageWidth;
                            imageWidth = rotate;
                        }
                    }

                    Integer originalHeight = CollectionUtils.getByPath(item.getMetadata(), ImageTag.ORIGINAL_HEIGHT_METADATA_PATH) != null ?
                            ObjectUtils.to(Integer.class, CollectionUtils.getByPath(item.getMetadata(), ImageTag.ORIGINAL_HEIGHT_METADATA_PATH)) :
                            imageHeight;
                    Integer originalWidth = CollectionUtils.getByPath(item.getMetadata(), ImageTag.ORIGINAL_WIDTH_METADATA_PATH) != null ?
                            ObjectUtils.to(Integer.class, CollectionUtils.getByPath(item.getMetadata(), ImageTag.ORIGINAL_WIDTH_METADATA_PATH)) :
                            imageWidth;

                    double horzScale = (double) imageWidth / cropWidth;
                    double vertScale = (double) imageHeight / cropHeight;

                    if (vertScale < horzScale) {
                        cropHeight = imageHeight;
                        cropWidth = ((Double) (cropWidth * vertScale)).intValue();
                    } else {
                        cropWidth = imageWidth;
                        cropHeight = ((Double) (cropHeight * horzScale)).intValue();
                    }

                    double heightScaleFactor = originalHeight != null && originalHeight > 0 ? (double) imageHeight / originalHeight : 1.0;
                    double widthScaleFactor = originalWidth != null && originalWidth > 0 ? (double) imageWidth / originalWidth : 1.0;

                    //bounding box of hotspots
                    Integer x1 = null;
                    Integer y1 = null;
                    for (HotSpot hotSpot : hotSpots) {
                        x1 = x1 == null || hotSpot.getX() < x1 ? hotSpot.getX() : x1;
                        x2 = x2 == null || (hotSpot.getX() + hotSpot.getWidth()) > x2 ? (hotSpot.getX() + hotSpot.getWidth()) : x2;
                        y1 = y1 == null || hotSpot.getY() < y1 ? hotSpot.getY() : y1;
                        y2 = y2 == null || (hotSpot.getY() + hotSpot.getHeight()) > y2 ? (hotSpot.getY() + hotSpot.getHeight()) : y2;
                    }

                    x1 = ((Double) (x1 * widthScaleFactor)).intValue();
                    x2 = ((Double) (x2 * widthScaleFactor)).intValue();
                    y1 = ((Double) (y1 * heightScaleFactor)).intValue();
                    y2 = ((Double) (y2 * heightScaleFactor)).intValue();

                    int centerX = (x1 + x2) / 2;
                    int centerY = (y1 + y2) / 2;

                    x1 = centerX - (cropWidth / 2);
                    x2 = centerX + (cropWidth / 2);
                    y1 = centerY - (cropHeight / 2);
                    y2 = centerY + (cropHeight / 2);

                    if (x1 < 0) {
                        x1 = 0;
                    } else if (x2 > imageWidth) {
                        x1 = x1 - x2 + imageWidth;
                    }

                    if (y1 < 0) {
                        y1 = 0;
                    } else if (y2 > cropHeight) {
                        y1 = y1 - y2 + cropHeight;
                    }
                    return Arrays.asList(x1, y1, cropWidth, cropHeight);
                }
            }
        }
        return null;
    }
}
