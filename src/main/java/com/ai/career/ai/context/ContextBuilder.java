package com.ai.career.ai.context;

import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.Profile;
import com.ai.career.domain.entity.Skill;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ContextBuilder {

    public Map<String, String> buildContext(Profile profile, Job job) {
        Map<String, String> context = new HashMap<>();

        if (profile != null) {
            context.put("fullName", profile.getFullName() != null ? profile.getFullName() : "Candidate");
            context.put("summary", profile.getSummary() != null ? profile.getSummary() : "");
            context.put("location", profile.getLocation() != null ? profile.getLocation() : "");

            String skillsStr = profile.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.joining(", "));
            context.put("skills", skillsStr);
        }

        if (job != null) {
            context.put("jobTitle", job.getTitle() != null ? job.getTitle() : "");
            context.put("company", job.getCompany() != null ? job.getCompany() : "");
            context.put("jobLocation", job.getLocation() != null ? job.getLocation() : "");
            context.put("jobDescription", job.getDescription() != null ? job.getDescription() : "");
        }

        return context;
    }
}
