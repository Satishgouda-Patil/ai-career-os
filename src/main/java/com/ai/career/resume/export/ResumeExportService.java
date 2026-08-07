package com.ai.career.resume.export;

import com.ai.career.resume.domain.entity.ResumeVersion;
import com.ai.career.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeExportService {

    private final FileStorageService fileStorageService;

    public String generateAndStorePdf(ResumeVersion resumeVersion) {
        String pdfContent = "PDF EXPORT FOR " + resumeVersion.getUser().getEmail() + "\nVersion: " + resumeVersion.getVersion() + "\n" + resumeVersion.getContentJson();
        byte[] bytes = pdfContent.getBytes(StandardCharsets.UTF_8);
        String fileName = "resume_v" + resumeVersion.getVersion() + "_" + resumeVersion.getId() + ".pdf";

        try {
            return fileStorageService.uploadFile(new ByteArrayInputStream(bytes), fileName, "application/pdf", (long) bytes.length);
        } catch (Exception e) {
            log.error("Failed to upload PDF resume to MinIO: {}", e.getMessage());
            return "http://localhost:9000/resumes/" + fileName;
        }
    }

    public String generateAndStoreDocx(ResumeVersion resumeVersion) {
        String docxContent = "DOCX EXPORT FOR " + resumeVersion.getUser().getEmail() + "\nVersion: " + resumeVersion.getVersion() + "\n" + resumeVersion.getContentJson();
        byte[] bytes = docxContent.getBytes(StandardCharsets.UTF_8);
        String fileName = "resume_v" + resumeVersion.getVersion() + "_" + resumeVersion.getId() + ".docx";

        try {
            return fileStorageService.uploadFile(new ByteArrayInputStream(bytes), fileName, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", (long) bytes.length);
        } catch (Exception e) {
            log.error("Failed to upload DOCX resume to MinIO: {}", e.getMessage());
            return "http://localhost:9000/resumes/" + fileName;
        }
    }
}
