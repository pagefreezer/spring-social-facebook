/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.facebook.api.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;
import org.springframework.social.MissingAuthorizationException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

class AbstractFacebookOperations {

	private final static Log logger = LogFactory.getLog(AbstractFacebookOperations.class);

	private final boolean isAuthorized;

	public AbstractFacebookOperations(boolean isAuthorized) {
		this.isAuthorized = isAuthorized;
	}

	public AbstractFacebookOperations(boolean isAuthorized, RestTemplate restTemplate) {
		this(isAuthorized);
		restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
		if (logger.isDebugEnabled()) {
			restTemplate.getInterceptors().add(new OperationsRequestLoggingInterceptor());
		}
	}
	
	protected void requireAuthorization() {
		if (!isAuthorized) {
			throw new MissingAuthorizationException("facebook");
		}
	}

	private class OperationsRequestLoggingInterceptor implements ClientHttpRequestInterceptor {

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
			ClientHttpResponse response = execution.execute(request, body);

			logger.debug("Request: " + request.getMethod().name() + " " + request.getURI().toString());
			logger.debug("Request body: " + (body.length == 0 ? "empty" : new String(body)));
			logger.debug("Response: " + response.getRawStatusCode() + " " + response.getStatusText());
			logger.debug("Response body: " + IOUtils.toString(response.getBody()));

			return response;
		}
	}
}
