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
package org.springframework.social.facebook.api;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.social.*;

import static org.junit.Assert.fail;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

public class ErrorHandlingTest extends AbstractFacebookApiTest {

	@Test(expected=UncategorizedApiException.class)
	public void code1HttpsRequired() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonResource("error-1-httpsRequired")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}
	
	@Test(expected=RateLimitExceededException.class)
	public void code4Service() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST)
					.body(jsonResource("error-4-rateLimitExceeded"))
					.contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}

	@Test(expected=ServerException.class)
	public void code2Service() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonResource("error-2-serviceUnavailable")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}
	
	@Test(expected=org.springframework.social.InsufficientPermissionException.class)
	public void code10PermissionDenied() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonResource("error-10-permissionDenied")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}

	@Test(expected=RateLimitExceededException.class)
	public void code17Service() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withStatus(HttpStatus.BAD_REQUEST)
						.body(jsonResource("error-17-rateLimitExceeded"))
						.contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}
	
	@Test(expected=UncategorizedApiException.class)
	public void code100BadRequestUri() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonResource("error-100-badRequestUrl")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}
	
	@Test(expected=UncategorizedApiException.class)
	public void code100NonExistingFields() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonResource("error-100-nonExistingFields")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}
	
	@Test(expected=InvalidAuthorizationException.class)
	public void code104NoAccessToken() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonResource("error-104-noAccessToken")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}
	
	@Test(expected=InvalidAuthorizationException.class)
	public void code190BogusAccessToken() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonResource("error-190-bogusAccessToken")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}
	
	@Test(expected=ExpiredAuthorizationException.class)
	public void code190TokenExpired() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonResource("error-190-tokenExpired")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}
	
	@Test(expected=RevokedAuthorizationException.class)
	public void code190UserRevokedToken() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonResource("error-190-userRevokedToken")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}
	
	@Test(expected=RevokedAuthorizationException.class)
	public void code190UserLoggedOut() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonResource("error-190-userLoggedOut")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}
	
	@Test(expected=InsufficientPermissionException.class)
	public void code200NotAuthorizedForAction() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonResource("error-200-notAuthorizedForAction")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}
	
	@Test(expected=UncategorizedApiException.class)
	public void code368Blocked() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonResource("error-368-blocked")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}	
	
	@Test(expected=DuplicateStatusException.class)
	public void code506DuplicatePost() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonResource("error-506-duplicate")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}

	@Test(expected=RateLimitExceededException.class)
	public void code613Service() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withStatus(HttpStatus.BAD_REQUEST)
						.body(jsonResource("error-613-rateLimitExceeded"))
						.contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}

	@Test(expected=ResourceNotFoundException.class)
	public void code803UnknownAlias() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonResource("error-803-unknownAlias")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}	
	
	@Test(expected=ResourceNotFoundException.class)
	public void code803UnknownPath() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonResource("error-2500-bogusPath")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", User.class);
		fail();
	}
	
}
