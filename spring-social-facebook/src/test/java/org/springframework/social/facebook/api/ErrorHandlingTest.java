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

import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.social.DuplicateStatusException;
import org.springframework.social.ExpiredAuthorizationException;
import org.springframework.social.InsufficientPermissionException;
import org.springframework.social.InvalidAuthorizationException;
import org.springframework.social.MissingAuthorizationException;
import org.springframework.social.RateLimitExceededException;
import org.springframework.social.ResourceNotFoundException;
import org.springframework.social.RevokedAuthorizationException;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class ErrorHandlingTest extends AbstractFacebookApiTest {

	private static final String LOGGED_OUT_REVOKATION = "The authorization has been revoked. Reason: Error validating access token: The session is invalid because the user logged out.";
	private static final String NOT_AUTHORIZED_REVOKATION = "The authorization has been revoked. Reason: Error validating access token: 123456789 has not authorized application 987654321";

	
	@Test
	public void unknownAlias() {
		try {
			mockServer.expect(requestTo("https://graph.facebook.com/v2.0/dummyalias"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withStatus(HttpStatus.NOT_FOUND).body(jsonResource("error-404-unknown-alias")).contentType(MediaType.APPLICATION_JSON));
			facebook.fetchObject("dummyalias", FacebookProfile.class);
			fail("Expected GraphAPIException when fetching an unknown object alias");
		} catch (ResourceNotFoundException e) {
			assertEquals("(#803) Some of the aliases you requested do not exist: dummyalias", e.getMessage());
		}				
	}
	
	@Test
	public void unknownPath() {
		try {
			mockServer.expect(requestTo("https://graph.facebook.com/v2.0/me/boguspath"))
				.andExpect(method(GET))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withBadRequest().body(jsonResource("error-400-unknown-path")).contentType(MediaType.APPLICATION_JSON));
			facebook.fetchConnections("me", "boguspath", String.class);
			fail();
		} catch (ResourceNotFoundException e) {
			assertEquals("Unknown path components: /boguspath", e.getMessage());
		}
	}

	@Test(expected = MissingAuthorizationException.class)
	public void resource_noAccessToken() {
		FacebookTemplate facebook = new FacebookTemplate(); // use anonymous FacebookTemplate in this test
		MockRestServiceServer mockServer = MockRestServiceServer.createServer(facebook.getRestTemplate());
		mockServer.expect(requestTo("https://graph.facebook.com/v2.0/me"))
			.andExpect(method(GET))
			.andRespond(withBadRequest().body(jsonResource("error-400-resource-no-access-token")).contentType(MediaType.APPLICATION_JSON));
		facebook.userOperations().getUserProfile();
	}
	
	@Test(expected = MissingAuthorizationException.class)
	public void currentUser_noAccessToken() {
		FacebookTemplate facebook = new FacebookTemplate(); // use anonymous FacebookTemplate in this test
		MockRestServiceServer mockServer = MockRestServiceServer.createServer(facebook.getRestTemplate());
		mockServer.expect(requestTo("https://graph.facebook.com/v2.0/me"))
			.andExpect(method(GET))
			.andRespond(withBadRequest().body(jsonResource("error-400-current-user-no-token")).contentType(MediaType.APPLICATION_JSON));
		facebook.userOperations().getUserProfile();
	}
	
	@Test(expected = ExpiredAuthorizationException.class)
	public void currentUser_expiredToken() { // The token has expired
		mockServer.expect(requestTo("https://graph.facebook.com/v2.0/me"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withUnauthorizedRequest().body(jsonResource("error-401-token-expired")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", String.class);
		fail();
	}
	
	@Test(expected = RevokedAuthorizationException.class)
	public void currentUser_removedApp() { // The user removed the app via Facebook's web application
		mockServer.expect(requestTo("https://graph.facebook.com/v2.0/me"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withUnauthorizedRequest().body(jsonResource("error-401-invalid-token-removed-app")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", String.class);
		fail();
	}
	
	@Test(expected = RevokedAuthorizationException.class)
	public void currentUser_loggedOut() { // The user logged out of Facebook
		mockServer.expect(requestTo("https://graph.facebook.com/v2.0/me"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withUnauthorizedRequest().body(jsonResource("error-401-invalid-token-logged-out")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", String.class);
		fail();
	}
	
	@Test(expected = RevokedAuthorizationException.class)
	public void currentUser_sessionDoesNotMatch() { // The user logged out of Facebook and another user has logged in (?)
		mockServer.expect(requestTo("https://graph.facebook.com/v2.0/me"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withUnauthorizedRequest().body(jsonResource("error-401-session-does-not-match")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", String.class);
		fail();
	}
	
	@Test(expected = RevokedAuthorizationException.class)
	public void currentUser_changedPassword() { // The user has changed their password
		mockServer.expect(requestTo("https://graph.facebook.com/v2.0/me"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withUnauthorizedRequest().body(jsonResource("error-401-invalid-token-changed-password")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", String.class);
		fail();
	}
	
	@Test(expected = InvalidAuthorizationException.class)
	public void currentUser_unknownInvalidAuthorization() { // The token is invalid, but the reason is not one otherwise expected by this error handler.
		mockServer.expect(requestTo("https://graph.facebook.com/v2.0/me"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withUnauthorizedRequest().body(jsonResource("error-401-invalid-token-unknown-reason")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", String.class);
		fail();
	}
	
	@Test(expected = InvalidAuthorizationException.class)
	public void currentUser_unknownInvalidAppId() { // This shouldn't happen normally, but could if the access token is manually typed and a mistake is made
		mockServer.expect(requestTo("https://graph.facebook.com/v2.0/me"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withUnauthorizedRequest().body(jsonResource("error-401-invalid-token-invalid-appid")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", String.class);
		fail();
	}
	
	@Test(expected = InvalidAuthorizationException.class)
	public void currentUser_unknownInvalidToken() { // This shouldn't happen normally, but could if the access token is manually typed and a mistake is made
		mockServer.expect(requestTo("https://graph.facebook.com/v2.0/me"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withUnauthorizedRequest().body(jsonResource("error-401-invalid-oauth-access-token")).contentType(MediaType.APPLICATION_JSON));
		facebook.fetchObject("me", String.class);
		fail();
	}
	
	@Test
	public void userHasntAuthorized() {
		try {
			mockServer.expect(requestTo("https://graph.facebook.com/v2.0/me/feed"))
				.andExpect(method(POST))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withStatus(HttpStatus.FORBIDDEN).body(jsonResource("error-403-not-authorized-for-action")).contentType(MediaType.APPLICATION_JSON));
			facebook.feedOperations().postLink("Test message", new FacebookLink("http://test.com", "Test", "Test this", "Testing some stuff"));
			fail();
		} catch (InsufficientPermissionException e) {
			assertEquals("Insufficient permission for this operation.", e.getMessage());
		}
	}
	
	@Test
	public void notAuthorizedForAction() {		
		try {
			mockServer.expect(requestTo("https://graph.facebook.com/v2.0/193482154020832/declined"))
				.andExpect(method(POST))
				.andExpect(header("Authorization", "OAuth someAccessToken"))
				.andRespond(withStatus(HttpStatus.FORBIDDEN).body(jsonResource("error-403-requires-extended-permission")).contentType(MediaType.APPLICATION_JSON));
			facebook.eventOperations().declineInvitation("193482154020832");
			fail();
		} catch (InsufficientPermissionException e) {
			assertEquals("The operation requires 'rsvp_event' permission.", e.getMessage());
			assertEquals("rsvp_event", e.getRequiredPermission());
		}
	}
	
	@Test(expected = InsufficientPermissionException.class)
	@Ignore("This doesn't seem to happen anymore...It looks like FB fixed their errors.")
	public void falseResponse() {
		mockServer.expect(requestTo("https://graph.facebook.com/v2.0/someobject"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withSuccess("false", MediaType.APPLICATION_JSON));
		facebook.fetchObject("someobject", FacebookProfile.class);
	}

	@Test(expected = RateLimitExceededException.class)
	public void rateLimit() {
		mockServer.expect(requestTo("https://graph.facebook.com/v2.0/me/feed"))
			.andExpect(method(POST))
			.andExpect(content().string("message=Test+Message"))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withBadRequest().body(jsonResource("error-rate-limit")).contentType(MediaType.APPLICATION_JSON));
		facebook.feedOperations().updateStatus("Test Message");
	}

	@Test(expected = DuplicateStatusException.class)
	public void duplicateOrSimilarPostToTwitter() {
		
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);
		mockServer.expect(requestTo("https://graph.facebook.com/v2.0/me/feed"))
			.andExpect(method(POST))
			.andExpect(content().string("message=Test+Message"))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withBadRequest().body(jsonResource("error-duplicate-to-twitter")).contentType(MediaType.APPLICATION_JSON));
		facebook.feedOperations().updateStatus("Test Message");
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void notFound() {
		mockServer.expect(requestTo("https://graph.facebook.com/v2.0/nobody/feed?limit=25&fields=id%2Clikes.limit%281%29%2Ccomments.limit%281%29%2Cfrom%2Cstory%2Cstory_tags%2Cpicture%2Clink%2Csource%2Cname%2Ccaption%2Cdescription%2Cicon%2Cactions%2Cprivacy%2Ctype%2Cstatus_type%2Ccreated_time%2Cupdated_time%2Cis_hidden%2Csubscribed%2Cis_expired"))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.NOT_FOUND).body(jsonResource("error-404-unknown-alias")).contentType(MediaType.APPLICATION_JSON));
		facebook.feedOperations().getFeed("nobody");		
	}

	@Test(expected=UncategorizedApiException.class)
	public void code1HttpsRequired() throws Exception {
		mockServer.expect(requestTo(fbUrl("me")))
			.andExpect(method(GET))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST).body(jsonResource("error-1-httpsRequired")).contentType(MediaType.APPLICATION_JSON));
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
