package com.trackharbor.trackharbor.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class AiTipsService {

    private final Client client;
    private final JobDescriptionScraper scraper;

    public AiTipsService() {
        Dotenv dotenv = Dotenv.load();

        String apiKey = dotenv.get("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing GEMINI_API_KEY in .env file.");
        }

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();

        this.scraper = new JobDescriptionScraper();
    }

    public List<String> generateTips(String companyName, String positionName, String jobUrl) {

        String scrapedJobDescription = scraper.scrapeJobDescription(jobUrl);

        String prompt = buildPrompt(
                companyName,
                positionName,
                jobUrl,
                scrapedJobDescription
        );

        ExecutorService geminiExecutor = Executors.newSingleThreadExecutor();

        try {
            Future<GenerateContentResponse> future = geminiExecutor.submit(() ->
                    client.models.generateContent(
                            "gemini-2.5-flash",
                            prompt,
                            null
                    )
            );

            GenerateContentResponse response = future.get(20, TimeUnit.SECONDS);

            String responseText = response.text();

            return parseTips(responseText);

        } catch (TimeoutException e) {
            throw new RuntimeException(
                    "Gemini request timed out after 20 seconds.",
                    e
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Gemini request failed.",
                    e
            );
        } finally {
            geminiExecutor.shutdownNow();
        }
    }

    private String buildPrompt(
            String companyName,
            String positionName,
            String jobUrl,
            String scrapedJobDescription
    ) {

        boolean hasJobDescription =
                scrapedJobDescription != null &&
                        !scrapedJobDescription.isBlank();

        if (hasJobDescription) {

            return """
                    You are helping a college computer science student prepare for a job or internship application.

                    Generate exactly 3 practical interview/application tips based on the job description below.

                    Company: %s
                    Position: %s
                    Job URL: %s

                    Job description text:
                    %s

                    Rules:
                    - Return exactly 3 tips.
                    - Each tip should be one sentence.
                    - Do not include an intro.
                    - Do not include markdown headings.
                    - Do not number the tips.
                    - Make the tips specific to the role.
                    """.formatted(
                    companyName,
                    positionName,
                    jobUrl,
                    scrapedJobDescription
            );
        }

        return """
                You are helping a college computer science student prepare for a job or internship application.

                The job URL was invalid, unavailable, or could not be scraped.

                Generate exactly 3 practical interview/application tips using the company name, likely company values, and actual position title.

                Company: %s
                Position: %s
                Job URL: %s

                Rules:
                - Return exactly 3 tips.
                - Each tip should be one sentence.
                - Do not include an intro.
                - Do not include markdown headings.
                - Do not number the tips.
                - Make reasonable assumptions, but do not pretend you read the job description.
                """.formatted(
                companyName,
                positionName,
                jobUrl
        );
    }

    private List<String> parseTips(String responseText) {

        List<String> tips = new ArrayList<>();

        if (responseText == null || responseText.isBlank()) {
            throw new IllegalStateException(
                    "Gemini returned an empty response."
            );
        }

        String[] lines = responseText.split("\\R");

        for (String line : lines) {

            String cleaned = line
                    .replaceFirst("^\\s*[-•*]\\s*", "")
                    .replaceFirst("^\\s*\\d+[.)]\\s*", "")
                    .trim();

            if (!cleaned.isBlank()) {
                tips.add(cleaned);
            }

            if (tips.size() == 3) {
                break;
            }
        }

        if (tips.size() < 3) {
            throw new IllegalStateException(
                    "Gemini did not return 3 usable tips."
            );
        }

        return tips;
    }
}