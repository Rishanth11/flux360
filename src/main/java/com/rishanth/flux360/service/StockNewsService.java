package com.rishanth.flux360.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockNewsService {

    private final RestTemplate restTemplate;

    private static final String RSS_URL =
            "https://economictimes.indiatimes.com/markets/rssfeeds/1977021501.cms";

    public List<Map<String, String>> fetchMarketNews() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0");
        headers.set("Accept", "application/rss+xml, application/xml, text/xml");

        ResponseEntity<String> response = restTemplate.exchange(
                RSS_URL, HttpMethod.GET,
                new HttpEntity<>(headers), String.class
        );

        return parseRss(response.getBody());
    }

    private List<Map<String, String>> parseRss(String xml) {
        List<Map<String, String>> items = new ArrayList<>();
        if (xml == null) return items;

        String[] parts = xml.split("<item>");
        for (int i = 1; i < parts.length && items.size() < 20; i++) {
            String item = parts[i];

            String title   = extractTag(item, "title");
            String link    = extractTag(item, "link");
            String pubDate = extractTag(item, "pubDate");
            String desc    = extractTag(item, "description");

            if (desc  != null) desc  = desc.replaceAll("<[^>]+>", "").trim();
            if (title != null) title = title.replaceAll("<[^>]+>", "")
                    .replace("&amp;", "&").replace("&lt;", "<")
                    .replace("&gt;", ">").replace("&#39;", "'").trim();

            if (title != null && !title.isEmpty() && link != null) {
                Map<String, String> entry = new LinkedHashMap<>();
                entry.put("title",       title);
                entry.put("link",        link);
                entry.put("pubDate",     pubDate != null ? pubDate : "");
                entry.put("description", desc    != null ? desc    : "");
                items.add(entry);
            }
        }
        return items;
    }

    private String extractTag(String xml, String tag) {
        String open  = "<"  + tag + ">";
        String close = "</" + tag + ">";
        int start = xml.indexOf(open);
        int end   = xml.indexOf(close);
        if (start == -1 || end == -1) return null;
        return xml.substring(start + open.length(), end)
                .replace("<![CDATA[", "").replace("]]>", "").trim();
    }
}