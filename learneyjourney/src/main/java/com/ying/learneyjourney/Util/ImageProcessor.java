package com.ying.learneyjourney.Util;
import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageProcessor {

    public static byte[] resizeAndCompress(
            byte[] originalBytes,
            int maxWidth,
            float quality
    ) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(new ByteArrayInputStream(originalBytes))
                .size(maxWidth, maxWidth)     // keep aspect ratio
                .outputFormat("jpg")          // jpg is smaller than png
                .outputQuality(quality)       // 0.0 - 1.0
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }
}