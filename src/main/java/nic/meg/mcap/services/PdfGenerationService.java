package nic.meg.mcap.services;

import java.io.IOException;
import java.util.Map;

public interface PdfGenerationService {
    byte[] generatePdfFromTemplate(String templateName, Map<String, Object> context) throws IOException;
    byte[] generateApplicationPdf(Long applicationId, String applicantNo) throws IOException;
    byte[] generateReceiptPdf(Long applicationId, String applicantNo) throws IOException;
}