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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;
import org.springframework.social.MissingAuthorizationException;
import org.springframework.social.facebook.api.FacebookThresholdLimitReachedException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

class AbstractFacebookOperations {

	private final Log logger = LogFactory.getLog(AbstractFacebookOperations.class);

	private final boolean isAuthorized;

	public AbstractFacebookOperations(boolean isAuthorized) {
		this.isAuthorized = isAuthorized;
	}

	public AbstractFacebookOperations(boolean isAuthorized, RestTemplate restTemplate) {
		this(isAuthorized);
		restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
		addInterceptors(restTemplate);
	}

	private void addInterceptors(RestTemplate restTemplate) {
		addLoggingInterceptor(restTemplate);
		addRateLimitInterceptor(restTemplate);
	}

	private void addLoggingInterceptor(RestTemplate restTemplate) {
		if (logger.isDebugEnabled()) {
			LoggingRequestsInterceptor interceptor = new LoggingRequestsInterceptor(restTemplate.hashCode());
			if (!restTemplate.getInterceptors().contains(interceptor)) {
				restTemplate.getInterceptors().add(interceptor);
				logger.info(LoggingRequestsInterceptor.class.getSimpleName() + " enabled.");
			}
		}
	}

	private void addRateLimitInterceptor(RestTemplate restTemplate) {
		RateLimitRequestInterceptor interceptor = new RateLimitRequestInterceptor(restTemplate.hashCode());
		if (!restTemplate.getInterceptors().contains(interceptor)) {
			restTemplate.getInterceptors().add(interceptor);
			logger.info(RateLimitRequestInterceptor.class.getSimpleName() + " enabled.");
		}
	}

	protected void requireAuthorization() {
		if (!isAuthorized) {
			throw new MissingAuthorizationException("facebook");
		}
	}

	private class LoggingRequestsInterceptor implements ClientHttpRequestInterceptor {
		private final String id;
		public LoggingRequestsInterceptor(int id) {
			this.id = "logging-interceptor-" + id;
		}

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
			LoggingRequestsInterceptor that = (LoggingRequestsInterceptor) o;
			return id.equals(that.id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}

	/**
	 * https://developers.facebook.com/docs/graph-api/advanced/rate-limiting
	 */
	private class RateLimitRequestInterceptor implements ClientHttpRequestInterceptor {

		private final String id;
		private final int thresholdPercentage = 80; //we shouldn't use more than this percentage
		private final ObjectMapper objectMapper;

		private static final String CALL_COUNT_PARAM = "call_count"; //
		private static final String TOTAL_TIME_PARAM = "total_time";
		private static final String TOTAL_CPU_TIME_PARAM = "total_cputime";
		private static final String X_APP_USAGE_PARAM = "X-App-Usage";

		public RateLimitRequestInterceptor(int id) {
			this.id = "rateLimit-interceptor-" + id;
			this.objectMapper = new ObjectMapper();
		}

		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
			ClientHttpResponse response = execution.execute(request, body);
			logger.debug("Request: " + request.getMethod().name() + " " + request.getURI().toString());
			logger.debug("Response: " + response.getRawStatusCode() + " " + response.getStatusText());
			if (mightGetRateLimited(response.getHeaders())) {
				logger.warn("Approaching API usage.");
				throw new FacebookThresholdLimitReachedException();
			}
			return response;
		}

		/**
		 * The values for callCount, totalTime and totalCpuTime are whole numbers representing the percentage used
		 * values for each of the metrics.
		 * This method indicates if the thresholdPercentage > 80%, if so, then it is a higher chance of getting rate
		 * limited by the Facebook API.
		 *
		 * @param headers
		 * @return true when the thresholdPercentage was reached, false otherwise.
		 */
		private boolean mightGetRateLimited(HttpHeaders headers) {
			logger.debug("Response Headers: " + headers.toSingleValueMap());
			String appUsageParam = null;
			if (headers.containsKey(X_APP_USAGE_PARAM)) {
				logger.debug("Response Headers: " + headers.getFirst(X_APP_USAGE_PARAM));
				appUsageParam = headers.getFirst(X_APP_USAGE_PARAM);
			} else if (headers.containsKey(X_APP_USAGE_PARAM.toLowerCase())) {
				logger.debug("Response Headers: " + headers.getFirst(X_APP_USAGE_PARAM.toLowerCase()));
				appUsageParam = headers.getFirst(X_APP_USAGE_PARAM.toLowerCase());
			} else {
				logger.debug(X_APP_USAGE_PARAM + " parameter is not present.");
				return false;
			}

			if (appUsageParam == null || appUsageParam.trim().length() == 0) {
				logger.debug(X_APP_USAGE_PARAM + " parameter is empty.");
				return false;
			}

			logger.info(X_APP_USAGE_PARAM + " parameter found: " + appUsageParam);
			JsonNode json = getJson(appUsageParam);
			if (json == null) {
				return false;
			}

			if (json.get(CALL_COUNT_PARAM).asInt() >= thresholdPercentage
					|| json.get(TOTAL_TIME_PARAM).asInt() >= thresholdPercentage
					|| json.get(TOTAL_CPU_TIME_PARAM).asInt() >= thresholdPercentage) {
				return true;
			}

			return false;
		}

		/**
		 * Expected input: "{\"call_count\":x,\"total_cputime\":y,\"total_time\":z}"
		 * @param appUsageParam
		 * @return jsonNode with key:value pairs
		 */
		private JsonNode getJson(String appUsageParam) {
			try {
				return this.objectMapper.readTree(appUsageParam);
			} catch (IOException e) {
				logger.error("Unable to parse x-app-usage parameter: " + appUsageParam, e);
				return null;
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			RateLimitRequestInterceptor that = (RateLimitRequestInterceptor) o;
			return id.equals(that.id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}
}
