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
package org.springframework.social.facebook.connect;

import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Template;
import org.springframework.social.support.ClientHttpRequestFactorySelector;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Facebook-specific extension of OAuth2Template to use a RestTemplate that recognizes form-encoded responses as "text/plain".
 * Facebook token responses are form-encoded results with a content type of "text/plain", which prevents the FormHttpMessageConverter
 * registered by default from parsing the results.
 * @author Craig Walls
 */
public class FacebookOAuth2Template extends OAuth2Template {

	public FacebookOAuth2Template(String clientId, String clientSecret) {
		super(clientId, clientSecret, "https://www.facebook.com/v2.3/dialog/oauth", "https://graph.facebook.com/v2.3/oauth/access_token");
		setUseParametersForClientAuthentication(true);
	}

	@Override
	protected RestTemplate createRestTemplate() {
		RestTemplate restTemplate = new RestTemplate(ClientHttpRequestFactorySelector.getRequestFactory());
		FormHttpMessageConverter messageConverter = new FormHttpMessageConverter() {
			public boolean canRead(Class<?> clazz, MediaType mediaType) {
				// always read as x-www-url-formencoded even though Facebook sets contentType to text/plain				
				return true;
			}
		};
		restTemplate.setMessageConverters(Collections.<HttpMessageConverter<?>>singletonList(messageConverter));
		return restTemplate;
	}
	
	@Override
	@SuppressWarnings("unchecked")	
	protected AccessGrant postForAccessGrant(String accessTokenUrl, MultiValueMap<String, String> parameters) {
		MultiValueMap<String, String> response = getRestTemplate().postForObject(accessTokenUrl, parameters, MultiValueMap.class);
		String expires;
		if (response.containsKey("expires_in")) {
			expires = response.getFirst("expires_in"); //Available for API v2.3+
		} else {
			expires = response.getFirst("expires"); //Available only for API v2.0 and v2.1
		}
		return new AccessGrant(response.getFirst("access_token"), null, null, expires != null ? Long.valueOf(expires) : null);
	}
}
