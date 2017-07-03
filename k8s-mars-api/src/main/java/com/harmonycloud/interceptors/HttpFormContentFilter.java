package com.harmonycloud.interceptors;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.harmonycloud.common.util.HttpPutFormContentRequestWrapper;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.filter.HttpPutFormContentFilter;



public class HttpFormContentFilter extends HttpPutFormContentFilter {

	private final FormHttpMessageConverter formConverter = new AllEncompassingFormHttpMessageConverter();

	/**
	 * The default character set to use for reading form data.
	 */
	public void setCharset(Charset charset) {
		this.formConverter.setCharset(charset);
	}

	@Override
	protected void doFilterInternal(final HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		if (("PUT".equals(request.getMethod()) || "PATCH".equals(request.getMethod())
				|| "DELETE".equals(request.getMethod())) && isFormContentType(request)) {
			HttpInputMessage inputMessage = new ServletServerHttpRequest(request) {
				@Override
				public InputStream getBody() throws IOException {
					return request.getInputStream();
				}
			};
			MultiValueMap<String, String> formParameters = formConverter.read(null, inputMessage);

            HttpServletRequest wrapper = new HttpPutFormContentRequestWrapper(request, formParameters);

			filterChain.doFilter(wrapper, response);
		} else {
			filterChain.doFilter(request, response);
		}


    }

	private boolean isFormContentType(HttpServletRequest request) {
		String contentType = request.getContentType();
		if (contentType != null) {
			try {
				MediaType mediaType = MediaType.parseMediaType(contentType);
				return (MediaType.APPLICATION_FORM_URLENCODED.includes(mediaType));
			} catch (IllegalArgumentException ex) {
				return false;
			}
		} else {
			return false;
		}
	}


}
