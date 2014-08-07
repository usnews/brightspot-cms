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

    public static List<Integer> hotSpotCrop(StorageItem item, Integer cropX, Integer cropY, Integer cropWidth, Integer cropHeight) {
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

                    Integer originalHeight = CollectionUtils.getByPath(item.getMetadata(), ImageTag.ORIGINAL_HEIGHT_METADATA_PATH) != null ?
                            ObjectUtils.to(Integer.class, CollectionUtils.getByPath(item.getMetadata(), ImageTag.ORIGINAL_HEIGHT_METADATA_PATH)) :
                            imageHeight;
                    Integer originalWidth = CollectionUtils.getByPath(item.getMetadata(), ImageTag.ORIGINAL_WIDTH_METADATA_PATH) != null ?
                            ObjectUtils.to(Integer.class, CollectionUtils.getByPath(item.getMetadata(), ImageTag.ORIGINAL_WIDTH_METADATA_PATH)) :
                            imageWidth;

                    double scale = (double) cropHeight / cropWidth;
                    if (imageWidth < imageHeight) {
                        cropWidth = imageWidth;
                        cropHeight = ((Double) (imageWidth * scale)).intValue();
                    } else {
                        cropHeight = imageHeight;
                        cropWidth = ((Double) (cropHeight * scale)).intValue();
                    }

                    double heightScaleFactor = originalHeight != null && originalHeight > 0 ? (double) imageHeight / originalHeight : 1.0;
                    double widthScaleFactor = originalWidth != null && originalWidth > 0 ? (double) imageWidth / originalWidth : 1.0;

                    //bounding box of hotspots
                    for (HotSpot hotSpot : hotSpots) {
                        cropX = cropX == null || hotSpot.getX() < cropX ? hotSpot.getX() : cropX;
                        x2 = x2 == null || (hotSpot.getX() + hotSpot.getWidth()) > x2 ? (hotSpot.getX() + hotSpot.getWidth()) : x2;
                        cropY = cropY == null || hotSpot.getY() < cropY ? hotSpot.getY() : cropY;
                        y2 = y2 == null || (hotSpot.getY() + hotSpot.getHeight()) > y2 ? (hotSpot.getY() + hotSpot.getHeight()) : y2;
                    }

                    cropX = ((Double) (cropX * widthScaleFactor)).intValue();
                    x2 = ((Double) (x2 * widthScaleFactor)).intValue();
                    cropY = ((Double) (cropY * heightScaleFactor)).intValue();
                    y2 = ((Double) (y2 * heightScaleFactor)).intValue();

                    int centerX = (cropX + x2) / 2;
                    int centerY = (cropY + y2) / 2;

                    cropX = centerX - (cropWidth / 2);
                    x2 = centerX + (cropWidth / 2);
                    cropY = centerY - (cropHeight / 2);
                    y2 = centerY + (cropHeight / 2);

                    if (cropX < 0) {
                        cropX = 0;
                    } else if (x2 > imageWidth) {
                        cropX = cropX - x2 + imageWidth;
                    }

                    if (cropY < 0) {
                        cropY = 0;
                    } else if (y2 > cropHeight) {
                        cropY = cropY - y2 + cropHeight;
                    }

                    return Arrays.asList(cropX, cropY, cropWidth, cropHeight);
                }
            }
        }
        return null;
    }
}
