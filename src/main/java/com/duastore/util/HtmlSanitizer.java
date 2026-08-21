package com.duastore.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * Lớp hỗ trợ xử lý html sanitizer.
 */
public final class HtmlSanitizer {

    private static final Safelist SAFELIST = Safelist.relaxed()
            .addTags("h1", "h2", "h3", "h4", "h5", "h6")
            .addTags("figure", "figcaption")
            .addAttributes("img", "src", "alt", "width", "height", "loading", "class")
            .addAttributes("a", "href", "title", "target", "rel", "class")
            .addAttributes("iframe", "src", "width", "height", "allowfullscreen", "loading")
            .addAttributes("video", "src", "controls", "width", "height", "class")
            .addAttributes("source", "src", "type")
            .preserveRelativeLinks(false);

    private HtmlSanitizer() {
    }

    public static String sanitize(String html) {
        if (html == null) {
            return null;
        }
        return Jsoup.clean(html, SAFELIST);
    }
}
