package com.ying.learneyjourney.component;

import com.ying.learneyjourney.constaint.ImageProvider;
import com.ying.learneyjourney.service.image.ImageSearchProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ImageProviderRegistry {

    private final Map<ImageProvider, ImageSearchProvider> providers = new EnumMap<>(ImageProvider.class);

//    public ImageProviderRegistry(List<ImageSearchProvider> providerList) {
//        for (ImageSearchProvider provider : providerList) {
//            providers.put(provider.provider(), provider);
//        }
//    }

    public ImageSearchProvider get(ImageProvider provider) {
        ImageSearchProvider found = providers.get(provider);
        if (found == null) {
            throw new IllegalArgumentException("No provider registered for " + provider);
        }
        return found;
    }
}
