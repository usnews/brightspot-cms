package com.psddev.cms.db;

import com.psddev.dari.util.CollectionUtils;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StorageItem;
import com.psddev.image.HotSpot;
import com.psddev.image.HotSpots;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageHotSpot {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageHotSpot.class);

    public static List<Integer> crop(StorageItem item, Integer cropWidth, Integer cropHeight) {
        if (item != null &&
            item.getMetadata().containsKey("height") &&
            item.getMetadata().containsKey("width") &&
            ((cropWidth != null && ObjectUtils.to(Integer.class, item.getMetadata().get("width")) > cropWidth) ||
                cropHeight != null && ObjectUtils.to(Integer.class, item.getMetadata().get("height")) > cropHeight)) {

            List<HotSpot> hotSpots = HotSpots.Data.getHotSpots(item);
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

                if (cropWidth == null) {
                    double scale = (double) imageHeight / cropHeight;
                    cropWidth = ((Double) (cropWidth * scale)).intValue();
                } else if (cropHeight == null) {
                    double scale = (double) imageWidth / cropWidth;
                    cropHeight = ((Double) (cropHeight * scale)).intValue();
                } else {
                    double horzScale = (double) imageWidth / cropWidth;
                    double vertScale = (double) imageHeight / cropHeight;
                    if (vertScale < horzScale) {
                        cropHeight = imageHeight;
                        cropWidth = ((Double) (cropWidth * vertScale)).intValue();
                    } else {
                        cropWidth = imageWidth;
                        cropHeight = ((Double) (cropHeight * horzScale)).intValue();
                    }
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

                //rotate bounding box
                if (CollectionUtils.getByPath(item.getMetadata(), "cms.edits/rotate") != null) {
                    Integer angle = ObjectUtils.to(Integer.class, CollectionUtils.getByPath(item.getMetadata(), "cms.edits/rotate"));
                    double radians = Math.toRadians(angle);

                    int centerX = (x1 + x2) / 2;
                    int centerY = (y1 + y2) / 2;

                    x1 = (new Double(centerX + Math.cos(radians) * (x1 - centerX) - Math.sin(radians) * (y1 - centerY))).intValue();
                    y1 = (new Double(centerY + Math.sin(radians) * (x1 - centerX) + Math.cos(radians) * (y1 - centerY))).intValue();

                    x2 = (new Double(centerX + Math.cos(radians) * (x2 - centerX) - Math.sin(radians) * (y2 - centerY))).intValue();
                    y2 = (new Double(centerY + Math.sin(radians) * (x2 - centerX) + Math.cos(radians) * (y2 - centerY))).intValue();

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
        return null;
    }
}
