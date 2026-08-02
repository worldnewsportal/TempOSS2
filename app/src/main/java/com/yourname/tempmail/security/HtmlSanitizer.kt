package com.yourname.tempmail.security

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

/**
 * Sanitizes untrusted e-mail HTML to renderable, safe markup:
 *   - strips <script> and event handlers
 *   - allows only a small set of tags/attrs
 *   - never yields anything that can execute JS
 */
class HtmlSanitizer(
    private val safelist: Safelist = DEFAULT,
) {
    fun sanitize(html: String?): String? {
        if (html.isNullOrBlank()) return null
        return Jsoup.clean(html, DEFAULT)
    }

    fun stripToText(html: String?): String {
        if (html.isNullOrBlank()) return ""
        return Jsoup.parse(html).text()
    }

    companion object {
        val DEFAULT: Safelist = Safelist.relaxed()
            .addTags("table", "thead", "tbody", "tfoot", "tr", "td", "th", "caption")
            .addTags("p", "div", "span", "br", "hr")
            .addTags("h1", "h2", "h3", "h4", "h5", "h6")
            .addTags("a", "img", "blockquote", "pre", "code", "ul", "ol", "li")
            .addTags("b", "strong", "i", "em", "u", "s", "sub", "sup", "small", "mark")
            .addAttributes("a", "href", "title", "rel")
            .addAttributes("img", "src", "alt", "width", "height")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addProtocols("img", "src", "http", "https")
            .removeProtocols("a", "href", "javascript", "vbscript")
            .removeProtocols("img", "src", "javascript")
    }
}