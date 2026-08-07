package com.ai.career.llm.prompt;

public class PromptTemplates {

    public static final String MATCH_SCORING_PROMPT = """
        You are an expert AI Career Matcher. Evaluate the match score between candidate skills and job requirements.

        User Skills: %s
        Job Title: %s
        Job Description: %s

        Calculate a numerical score from 0 to 100 representing how well the user skills match the job description.
        Return ONLY a single integer score without any additional explanation or formatting.
        """;

    public static final String RESUME_TAILORING_PROMPT = """
        You are an expert AI Resume Writer. Tailor the user summary for a specific job posting.

        User Name: %s
        Current Summary: %s
        User Skills: %s
        Target Job Title: %s
        Target Job Description: %s

        Generate an improved 3-sentence executive resume summary tailored specifically to emphasize relevant experience for this role.
        """;
}
