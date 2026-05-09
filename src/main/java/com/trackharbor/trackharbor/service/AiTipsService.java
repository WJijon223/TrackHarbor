package com.trackharbor.trackharbor.service;

import java.util.List;

public class AiTipsService {

    public List<String> generateTips(String jobUrl) {
        return List.of(
                "Review the job posting and identify the main technical skills the employer emphasizes.",
                "Prepare one project example that connects directly to the responsibilities in the role.",
                "Practice explaining your experience clearly using the STAR method."
        );
    }
}
