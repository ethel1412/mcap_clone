package nic.meg.mcap.services.impl;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nic.meg.mcap.dto.request.AcademicDetailsDTO;
import nic.meg.mcap.dto.request.AcademicRecordDTO;
import nic.meg.mcap.dto.request.LatestAcademicRecordRequestDTO;
import nic.meg.mcap.dto.request.PastAcademicRecordRequestDTO;
import nic.meg.mcap.entities.Address;
import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.Application;
import nic.meg.mcap.repositories.AddressRepository;
import nic.meg.mcap.repositories.ApplicationRepository;
import nic.meg.mcap.services.AcademicService;
import nic.meg.mcap.services.DocumentService;
import nic.meg.mcap.services.PdfGenerationService;
import nic.meg.mcap.services.ProgrammePreferenceService;

@Service
public class PdfGenerationServiceImpl implements PdfGenerationService {

	@Autowired
	private TemplateEngine templateEngine;
	@Autowired
	private ApplicationRepository applicationRepository;
	@Autowired
	private AddressRepository addressRepository;
	@Autowired
	private ProgrammePreferenceService programmePreferenceService;
	@Autowired
	private DocumentService documentService;
	@Autowired
	private AcademicService academicService;

	private static final Logger logger = LoggerFactory.getLogger(PdfGenerationServiceImpl.class);

	@Override
	public byte[] generateApplicationPdf(Long applicationId, String applicantNo) throws IOException {
		Application application = applicationRepository.findById(applicationId)
				.filter(app -> app.getApplicant().getApplicantNo().equals(applicantNo))
				.orElseThrow(() -> new SecurityException(
						"Unauthorized access or application not found for PDF generation."));

		Applicant applicant = application.getApplicant();

		Map<String, Object> data = new HashMap<>();
		data.put("applicant", applicant);

		// --- START OF THE CORRECT FIX ---

		// 1. Fetch the main DTO which contains the separate 'latest' and 'past' lists.
		AcademicDetailsDTO academicDetails = academicService.getAcademicDetails(applicantNo);

		// 2. Create a new, single list of the type the PDF template expects
		// (AcademicRecordDTO).
		List<AcademicRecordDTO> allAcademicRecordsForPdf = new ArrayList<>();

		// 3. Manually convert and add the latest records.
		if (academicDetails.getLatestRecords() != null) {
			for (LatestAcademicRecordRequestDTO latestDto : academicDetails.getLatestRecords()) {
				AcademicRecordDTO recordForPdf = new AcademicRecordDTO();
				// Copy all common fields from the latestDto to the generic DTO for the PDF.
				recordForPdf.setQualificationLevel(latestDto.getQualificationLevel());
				recordForPdf.setBoardOrUniversity(latestDto.getBoardOrUniversity());
				recordForPdf.setSchoolOrCollege(latestDto.getSchoolOrCollege());
				recordForPdf.setStreamOrMajor(latestDto.getStreamOrMajor());
				recordForPdf.setPercentage(latestDto.getPercentage());
				try {
					if (latestDto.getDateOfPassing() != null && !latestDto.getDateOfPassing().isEmpty()) {
						recordForPdf.setDateOfPassing(LocalDate.parse(latestDto.getDateOfPassing()));
					}
				} catch (DateTimeParseException e) {
					logger.info("Date parsing error:");
				}

				allAcademicRecordsForPdf.add(recordForPdf);
			}
		}

		// 4. Manually convert and add the past records.
		if (academicDetails.getPastRecords() != null) {
			for (PastAcademicRecordRequestDTO pastDto : academicDetails.getPastRecords()) {
				AcademicRecordDTO recordForPdf = new AcademicRecordDTO();
				recordForPdf.setQualificationLevel(pastDto.getQualificationLevel());
				recordForPdf.setBoardOrUniversity(pastDto.getBoardOrUniversity());
				recordForPdf.setSchoolOrCollege(pastDto.getSchoolOrCollege());
				recordForPdf.setStreamOrMajor(pastDto.getStreamOrMajor());
				recordForPdf.setPercentage(pastDto.getPercentage());
				try {
					if (pastDto.getDateOfPassing() != null && !pastDto.getDateOfPassing().isEmpty()) {
						recordForPdf.setDateOfPassing(LocalDate.parse(pastDto.getDateOfPassing()));
					}
				} catch (DateTimeParseException e) {
					logger.info("Date parsing error:");
				}

				allAcademicRecordsForPdf.add(recordForPdf);
			}
		}

		// 5. Put the final, combined list into the data map for the template.
		data.put("academicRecords", allAcademicRecordsForPdf);

		// The rest of your method is correct and remains unchanged.
		data.put("preferences", programmePreferenceService.getPreferencesByApplicationId(applicationId, applicantNo));
		data.put("uploadedDocuments", documentService.getUploadedDocuments(applicantNo));
		List<Address> addresses = addressRepository.findByEntityId(applicant.getApplicantId());
		data.put("permanentAddress", addresses.stream().filter(a -> "Permanent".equalsIgnoreCase(a.getAddressType()))
				.findFirst().orElse(null));
		data.put("communicationAddress", addresses.stream()
				.filter(a -> "Communication".equalsIgnoreCase(a.getAddressType())).findFirst().orElse(null));
		documentService.getDocumentByApplicantAndType(applicantNo, "Photo").ifPresent(
				doc -> data.put("applicantPhotoBase64", Base64.getEncoder().encodeToString(doc.getFileContent())));
		documentService.getDocumentByApplicantAndType(applicantNo, "Signature").ifPresent(
				doc -> data.put("applicantSignatureBase64", Base64.getEncoder().encodeToString(doc.getFileContent())));

		return generatePdfFromTemplate("applicant/pdf/application-pdf", data);
	}

	@Override
	public byte[] generatePdfFromTemplate(String templateName, Map<String, Object> data) throws IOException {
		final ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes();
		final HttpServletRequest request = attributes.getRequest();
		final HttpServletResponse response = attributes.getResponse();
		final ServletContext servletContext = request.getServletContext();
		final JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
		final IWebExchange exchange = application.buildExchange(request, response);
		final WebContext context = new WebContext(exchange, request.getLocale());
		context.setVariables(data);

		final String htmlContent = templateEngine.process(templateName, context);

		String baseUrl;
		try {
			baseUrl = ResourceUtils.getFile("classpath:static/").toURI().toString();
		} catch (FileNotFoundException e) {
			throw new IOException("Could not find 'classpath:static/' folder. Please ensure it exists.", e);
		}

		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final PdfRendererBuilder builder = new PdfRendererBuilder();

		builder.withHtmlContent(htmlContent, baseUrl);

		builder.toStream(outputStream);
		builder.run();

		return outputStream.toByteArray();
	}

    @Override
    public byte[] generateReceiptPdf(Long applicationId, String applicantNo) throws IOException {
        Application application = applicationRepository.findById(applicationId)
                .filter(app -> app.getApplicant().getApplicantNo().equals(applicantNo))
                .orElseThrow(() -> new SecurityException("Unauthorized access or application not found."));

        // Extra security: Ensure they actually paid before generating a receipt
        if (!application.isPaymentComplete()) {
            throw new IllegalStateException("Payment is not complete for this application.");
        }

        Applicant applicant = application.getApplicant();
        Map<String, Object> data = new HashMap<>();

        // Format Applicant Name safely handling null middle names
        String fullName = applicant.getFirstName() +
                (applicant.getMiddleName() != null && !applicant.getMiddleName().trim().isEmpty() ? " " + applicant.getMiddleName() : "") +
                " " + applicant.getLastName();

        // Map the exact variables we used in our HTML template
        data.put("applicantName", fullName.trim());
        data.put("applicationNo", application.getApplicationNo());
        data.put("programmeLevel", application.getAdmissionWindow().getProgrammeLevel().name());
        data.put("amountPaid", application.getAmountPaid() != null ? application.getAmountPaid() : "0.00");
        data.put("transactionId", application.getTransactionId() != null ? application.getTransactionId() : "N/A");

        // Format the Date beautifully (e.g., "May 04, 2026 14:30")
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        String formattedDate = application.getPaymentTimestamp() != null ? application.getPaymentTimestamp().format(formatter) : "N/A";
        data.put("paymentDate", formattedDate);

        // Call your existing magical helper method!
        return generatePdfFromTemplate("applicant/payment/receipt-template", data);
    }
}