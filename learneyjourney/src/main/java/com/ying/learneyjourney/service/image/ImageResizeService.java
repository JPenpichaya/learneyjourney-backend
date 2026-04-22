package com.ying.learneyjourney.service.image;

import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

@Service
public class ImageResizeService {

    public byte[] resizeToJpeg(byte[] originalBytes, int maxWidth) {
        try {
            BufferedImage input = ImageIO.read(new ByteArrayInputStream(originalBytes));
            if (input == null) {
                return originalBytes;
            }

            int width = input.getWidth();
            int height = input.getHeight();

            if (width <= maxWidth) {
                return encodeJpeg(input, 0.85f);
            }

            int newWidth = maxWidth;
            int newHeight = (int) Math.round((double) height * newWidth / width);

            BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, newWidth, newHeight);
            g.drawImage(input, 0, 0, newWidth, newHeight, null);
            g.dispose();

            return encodeJpeg(resized, 0.85f);
        } catch (Exception e) {
            return originalBytes;
        }
    }

    private byte[] encodeJpeg(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");

        if (!writers.hasNext()) {
            ImageIO.write(image, "jpg", output);
            return output.toByteArray();
        }

        ImageWriter writer = writers.next();
        try {
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }

            try (var ios = ImageIO.createImageOutputStream(output)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(image, null, null), param);
            }
        } finally {
            writer.dispose();
        }

        return output.toByteArray();
    }
}