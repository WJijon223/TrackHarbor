package com.trackharbor.trackharbor.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JobDescriptionScraper {

    public String scrapeJobDescription(String jobUrl) {
        try {
            if (jobUrl == null || jobUrl.isBlank()) {
                return "";
            }

            Document doc = Jsoup.connect(jobUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            String text = doc.body().text();

            if (text.length() > 8000) {
                return text.substring(0, 8000);
            }

            return text;

        } catch (Exception e) {
            return "";
        }
    }
}