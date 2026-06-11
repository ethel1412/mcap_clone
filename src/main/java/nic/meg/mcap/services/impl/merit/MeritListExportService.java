package nic.meg.mcap.services.impl.merit;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.response.MeritListResponseDTO;

@Service
@RequiredArgsConstructor
public class MeritListExportService {

	private final TemplateEngine templateEngine;

	public void exportToPdf(MeritListResponseDTO data, OutputStream outputStream) throws IOException {
		Context context = new Context();
		context.setVariable("meta", data.getMetadata());
		context.setVariable("entries", data.getEntries());

		// 2. Render HTML template to String
		// We will create this HTML file in the next step
		String htmlContent = templateEngine.process("controller/admissions/exports/merit-list-pdf", context);

		// 3. Convert HTML to PDF
		PdfRendererBuilder builder = new PdfRendererBuilder();
		builder.useFastMode();
		builder.withHtmlContent(htmlContent, "/");
		builder.toStream(outputStream);
		builder.run();
	}
}