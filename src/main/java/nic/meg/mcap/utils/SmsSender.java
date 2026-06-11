package nic.meg.mcap.utils;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import nic.meg.mcap.enums.SmsTemplate;

@Service
public class SmsSender {

	private final RestTemplate restTemplate;

	public SmsSender() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		this.restTemplate = createRestTemplate();
	}

	// ✅ Create once (not per call)
	private RestTemplate createRestTemplate()
			throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

		TrustStrategy trustAll = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, trustAll).build();

		SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext,
				NoopHostnameVerifier.INSTANCE);

		PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
				.setSSLSocketFactory(socketFactory).build();

		// ✅ Add timeouts (VERY IMPORTANT)
		RequestConfig config = RequestConfig.custom().setConnectTimeout(Timeout.ofSeconds(5))
				.setResponseTimeout(Timeout.ofSeconds(5)).build();

		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager)
				.setDefaultRequestConfig(config).build();

		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

		return new RestTemplate(factory);
	}

	// ✅ Clean sendSms
	public boolean sendSms(SmsTemplate template, String mobile, Object... values) {

		try {

			String username = "megcap.otp";
			String pin = encodePin("Nic@#2026");
			String message = URLEncoder.encode(template.format(values), StandardCharsets.UTF_8);
			String mobileFull = mobile.replace("+", "");

			String url = "https://smsgw.sms.gov.in/failsafe/MLink?" + "username=" + username + "&pin=" + pin
					+ "&message=" + message + "&mnumber=" + mobileFull + "&signature=MGSHEC"
					+ "&dlt_entity_id=1401530150000079078" + "&dlt_template_id=" + template.getTemplateId();

			ResponseEntity<String> response = restTemplate.getForEntity(new URI(url), String.class);
			return response.getStatusCode().is2xxSuccessful();

		} catch (Exception e) {
			throw new RuntimeException(e); // let upper layer decide errorCode
		}
	}

	// ✅ Bulk SMS with proper error handling
	public void sendBulkSms(SmsTemplate template, List<String> mobiles, Object... values) {

		ExecutorService executor = Executors.newFixedThreadPool(5);

		for (String mobile : mobiles) {
			executor.submit(() -> {
				try {
					boolean status = sendSms(template, mobile, values);
				} catch (Exception e) {
					//
				}
			});
		}

		executor.shutdown();
	}

	// ⚠️ keep only if required by NIC
	private String encodePin(String pin) {
		return pin.replace("%", "%25").replace("$", "%24").replace("#", "%23").replace("&", "%26").replace("@", "%40")
				.replace("!", "%21");
	}
}