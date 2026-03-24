package com.ying.learneyjourney.Util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public final class HtmlSanitizer {

    private HtmlSanitizer() {
    }

    public static String sanitize(String html) {
        Safelist safelist = Safelist.relaxed()
                .addTags("h1", "h2", "h3", "hr", "u")
                .addAttributes(":all", "style");
        return Jsoup.clean(html, safelist);
    }
}
