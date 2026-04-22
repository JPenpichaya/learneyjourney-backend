package com.ying.learneyjourney.service.image;

import com.ying.learneyjourney.constaint.ImageKind;
import com.ying.learneyjourney.constaint.ImageProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageProviderDecisionService {

    public List<ImageProvider> providerOrder(ImageKind kind) {
        if (kind == null) {
            kind = ImageKind.AUTO;
        }

        return switch (kind) {
            case PHOTO -> List.of(ImageProvider.UNSPLASH, ImageProvider.FREEPIK);
            case DIAGRAM, ILLUSTRATION, ICON -> List.of(ImageProvider.FREEPIK, ImageProvider.UNSPLASH);
            case AUTO -> List.of(ImageProvider.FREEPIK, ImageProvider.UNSPLASH);
        };
    }
}
