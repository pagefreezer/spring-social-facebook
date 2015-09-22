/*
 * Copyright 2015 the original author or authors.
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
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

class AbstractFacebookOperations {

	private final static Log logger = LogFactory.getLog(AbstractFacebookOperations.class);

	public AbstractFacebookOperations(RestTemplate restTemplate) {
		if (restTemplate != null) {
			restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
			if (logger.isDebugEnabled()) {
				OperationsRequestLoggingInterceptor loggingInterceptor =
						new OperationsRequestLoggingInterceptor(restTemplate.hashCode());
				if (!restTemplate.getInterceptors().contains(loggingInterceptor)) {
					restTemplate.getInterceptors().add(loggingInterceptor);
				}
			}
		}
	}
	
	private class OperationsRequestLoggingInterceptor implements ClientHttpRequestInterceptor {
		private final int id;
		public OperationsRequestLoggingInterceptor(int id) {
			this.id = id;
		}

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
			ClientHttpResponse response = execution.execute(request, body);

			logger.debug("Request: " + request.getMethod().name() + " " + request.getURI().toString());
			logger.debug("Request body: " + (body.length == 0 ? "empty" : new String(body)));
			logger.debug("Response: " + response.getRawStatusCode() + " " + response.getStatusText());
			logger.debug("Response body: " + IOUtils.toString(response.getBody()));

			return response;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			OperationsRequestLoggingInterceptor that = (OperationsRequestLoggingInterceptor) o;
			return id == that.id;

		}

		@Override
		public int hashCode() {
			return id;
		}
	}

}
