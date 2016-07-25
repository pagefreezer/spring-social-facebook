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
package org.springframework.social.facebook.api;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.social.NotAuthorizedException;
import org.springframework.social.facebook.api.impl.FacebookTemplate;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author Craig Walls
 */
public class OpenGraphTemplateTest extends AbstractFacebookApiTest {

	@Override
	protected FacebookTemplate createFacebookTemplate() {
		return new FacebookTemplate("someAccessToken", "socialshowcase");
	}
	
	@Test
	public void publishAction() {
		mockServer.expect(requestTo(GRAPH_API_FACEBOOK + "/me/socialshowcase:ding"))
			.andExpect(method(POST))
			.andExpect(content().string("thing=http%3A%2F%2Fwww.springsource.org%2Fspringsocial"))
			.andExpect(header("Authorization", "OAuth someAccessToken"))
			.andRespond(withSuccess(jsonResource("id-only"), MediaType.APPLICATION_JSON));
		assertEquals("297875170268724", facebook.openGraphOperations().publishAction("ding", "thing", "http://www.springsource.org/springsocial"));
	}

	@Test(expected = NotAuthorizedException.class)
	public void publishAction_unauthorized() {
		unauthorizedFacebook.openGraphOperations().publishAction("ding", "thing", "http://www.springsource.org/springsocial");
	}

}
