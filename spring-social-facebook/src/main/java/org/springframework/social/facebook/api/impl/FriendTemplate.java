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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.social.facebook.api.FamilyMember;
import org.springframework.social.facebook.api.FriendOperations;
import org.springframework.social.facebook.api.GraphApi;
import org.springframework.social.facebook.api.PagedList;
import org.springframework.social.facebook.api.PagingParameters;
import org.springframework.social.facebook.api.Reference;
import org.springframework.social.support.URIBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

class FriendTemplate extends AbstractFacebookOperations implements FriendOperations {
	
	private final GraphApi graphApi;

	private final RestTemplate restTemplate;

	public FriendTemplate(GraphApi graphApi, RestTemplate restTemplate, boolean isAuthorizedForUser) {
		super(isAuthorizedForUser, restTemplate);
		this.graphApi = graphApi;
		this.restTemplate = restTemplate;
	}
	
	public PagedList<Reference> getFriendLists() {
		requireAuthorization();
		return graphApi.fetchConnections("me", "friendlists", Reference.class);
	}

	public Reference getFriendList(String friendListId) {
		requireAuthorization();
		return graphApi.fetchObject(friendListId, Reference.class);
	}
	
	public PagedList<Reference> getFriends() {
		return getFriends("me");
	}
	
	public PagedList<String> getFriendIds() {
		return getFriendIds("me");
	}
	
	public PagedList<FacebookProfile> getFriendProfiles() {
		return getFriendProfiles("me");
	}

	public PagedList<FacebookProfile> getFriendProfiles(PagingParameters pagedListParameters) {
		return getFriendProfiles("me", pagedListParameters);
	}
	
	public PagedList<Reference> getFriends(String userId) {
		requireAuthorization();
		return graphApi.fetchConnections(userId, "friends", Reference.class);
	}
	
	public PagedList<String> getFriendIds(String userId) {
		requireAuthorization();		
		URI uri = URIBuilder.fromUri(GraphApi.GRAPH_API_URL + userId + "/friends").queryParam("fields", "id").build();
		@SuppressWarnings("unchecked")
		Map<String,PagedList<Map<String, String>>> response = restTemplate.getForObject(uri, Map.class);
		List<Map<String,String>> entryList = response.get("data");
		List<String> idList = new ArrayList<String>(entryList.size());
		for (Map<String, String> entry : entryList) {
			idList.add(entry.get("id"));
		}	
		return new PagedList<String>(idList, null, null);
	}
	
	public PagedList<FacebookProfile> getFriendProfiles(String userId) {
		requireAuthorization();
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
		parameters.set("fields", FULL_PROFILE_FIELDS);
		return graphApi.fetchConnections(userId, "friends", FacebookProfile.class, parameters);
	}

	public PagedList<FacebookProfile> getFriendProfiles(String userId, PagingParameters pagedListParameters) {
		requireAuthorization();
		MultiValueMap<String, String> parameters = PagedListUtils.getPagingParameters(pagedListParameters);
		parameters.set("fields", FULL_PROFILE_FIELDS);
		return graphApi.fetchConnections(userId, "friends", FacebookProfile.class, parameters);
	}

	public PagedList<FamilyMember> getFamily() {
		requireAuthorization();
		return graphApi.fetchConnections("me", "family", FamilyMember.class);
	}

	public PagedList<FamilyMember> getFamily(String userId) {
		requireAuthorization();
		return graphApi.fetchConnections(userId, "family", FamilyMember.class);
	}

	public PagedList<Reference> getSubscribedTo() {
		return getSubscribedTo("me");
	}
	
	public PagedList<Reference> getSubscribedTo(String userId) {
		requireAuthorization();
		return graphApi.fetchConnections(userId, "subscribedTo", Reference.class);
	}
	
	public PagedList<Reference> getSubscribers() {
		return getSubscribers("me");
	}
	
	public PagedList<Reference> getSubscribers(String userId) {
		requireAuthorization();
		return graphApi.fetchConnections(userId, "subscribers", Reference.class);
	}

	private static final String FULL_PROFILE_FIELDS = "id,name,first_name,last_name,gender,locale,education,work,email,third_party_id,link,timezone,updated_time,verified,about,bio,birthday,location,hometown,interested_in,religion,political,quotes,relationship_status,significant_other,website";

}
