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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.social.UncategorizedApiException;
import org.springframework.social.facebook.api.*;
import org.springframework.social.facebook.api.Post.PostType;
import org.springframework.social.support.URIBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.springframework.social.facebook.api.impl.PagedListUtils.getPagedListParameters;
import static org.springframework.social.facebook.api.impl.PagedListUtils.getPagingParameters;

class FeedTemplate extends AbstractFacebookOperations implements FeedOperations {

	private static final PagingParameters FIRST_PAGE = new PagingParameters(25, null, null, null);

	private static final String[] ALL_FIELDS = {
			"id", "likes.limit(1)", "comments.limit(1)", "from", "story", "story_tags", "picture", "link", "source",
			"name", "caption", "description", "icon", "actions", "privacy", "type",
			"status_type", "created_time", "updated_time", "is_hidden", "subscribed", "is_expired", "attachments" };

	private final GraphApi graphApi;
	
	private ObjectMapper objectMapper;
	
	private final RestTemplate restTemplate;

	public FeedTemplate(GraphApi graphApi, RestTemplate restTemplate, ObjectMapper objectMapper, boolean isAuthorized) {
		super(isAuthorized, restTemplate);
		this.graphApi = graphApi;
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
	}

	public PagedList<Post> getFeed() {
		return getFeed("me", FIRST_PAGE);
	}

	public PagedList<Post> getFeed(PagingParameters pagedListParameters) {
		return getFeed("me", pagedListParameters);
	}
	
	public PagedList<Post> getFeed(String ownerId) {
		return getFeed(ownerId, FIRST_PAGE);
	}
		
	public PagedList<Post> getFeed(String ownerId, PagingParameters pagedListParameters) {
		requireAuthorization();
		JsonNode responseNode = fetchConnectionList(GraphApi.GRAPH_API_URL + ownerId + "/feed", pagedListParameters,
				getPagingParameters(pagedListParameters), ALL_FIELDS);
		return deserializeList(responseNode, null, Post.class);
	}

	public PagedList<PoPst> getHomeFeed() {
		return getHomeFeed(FIRST_PAGE);
	}
	
	public PagedList<Post> getHomeFeed(PagingParameters pagedListParameters) {
		requireAuthorization();
		JsonNode responseNode = fetchConnectionList(GraphApi.GRAPH_API_URL + "me/home", pagedListParameters,
				getPagingParameters(pagedListParameters));
		return deserializeList(responseNode, null, Post.class);
	}

	public PagedList<Post> getStatuses() {
		return getStatuses("me", FIRST_PAGE);
	}
	
	public PagedList<Post> getStatuses(PagingParameters pagedListParameters) {
		return getStatuses("me", pagedListParameters);
	}

	public PagedList<Post> getStatuses(String userId) {
		return getStatuses(userId, FIRST_PAGE);
	}
	
	public PagedList<Post> getStatuses(String userId, PagingParameters pagedListParameters) {
		requireAuthorization();
		JsonNode responseNode = fetchConnectionList(GraphApi.GRAPH_API_URL + userId + "/statuses", pagedListParameters,
				getPagingParameters(pagedListParameters));
		return deserializeList(responseNode, "status", Post.class);
	}

	public PagedList<Post> getLinks() {
		return getLinks("me", FIRST_PAGE);
	}

	public PagedList<Post> getLinks(PagingParameters pagedListParameters) {
		return getLinks("me", pagedListParameters);
	}

	public PagedList<Post> getLinks(String ownerId) {
		return getLinks(ownerId, FIRST_PAGE);
	}
	
	public PagedList<Post> getLinks(String ownerId, PagingParameters pagedListParameters) {
		requireAuthorization();
		JsonNode responseNode = fetchConnectionList(GraphApi.GRAPH_API_URL + ownerId + "/links", pagedListParameters,
				getPagingParameters(pagedListParameters));
		return deserializeList(responseNode, "link", Post.class);
	}

	public PagedList<Post> getPosts() {
		return getPosts("me", FIRST_PAGE);
	}

	public PagedList<Post> getPosts(PagingParameters pagedListParameters) {
		return getPosts("me", pagedListParameters);
	}

	public PagedList<Post> getPosts(String ownerId) {
		return getPosts(ownerId, FIRST_PAGE);
	}
	
	public PagedList<Post> getPosts(String ownerId, PagingParameters pagedListParameters) {
		requireAuthorization();
		JsonNode responseNode = fetchConnectionList(GraphApi.GRAPH_API_URL + ownerId + "/posts", pagedListParameters,
				getPagingParameters(pagedListParameters));
		return deserializeList(responseNode, null, Post.class);
	}

	public PagedList<Post> getTagged() {
		return getTagged("me", FIRST_PAGE);
	}

	public PagedList<Post> getTagged(PagingParameters pagedListParameters) {
		return getTagged("me", pagedListParameters);
	}

	public PagedList<Post> getTagged(String ownerId) {
		return getTagged(ownerId, FIRST_PAGE);
	}
	
	public PagedList<Post> getTagged(String ownerId, PagingParameters pagedListParameters) {
		requireAuthorization();
		JsonNode responseNode = fetchConnectionList(GraphApi.GRAPH_API_URL + ownerId + "/tagged", pagedListParameters,
				getPagingParameters(pagedListParameters));
		return deserializeList(responseNode, null, Post.class);
	}

	public Post getPost(String entryId) {
		ObjectNode responseNode = (ObjectNode) restTemplate.getForObject(GraphApi.GRAPH_API_URL + entryId, JsonNode.class);
		return deserializePost(null, Post.class, responseNode);
	}

	public String updateStatus(String message) {
		return post("me", message);
	}

	public String postLink(String message, FacebookLink link) {
		return postLink("me", message, link);
	}
	
	public String postLink(String ownerId, String message, FacebookLink link) {
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		map.set("link", link.getLink());
		map.set("name", link.getName());
		map.set("caption", link.getCaption());
		map.set("description", link.getDescription());
		map.set("message", message);
		return graphApi.publish(ownerId, "feed", map);
	}
	
	public String post(PostData post) {
		return graphApi.publish(post.getTargetFeedId(), "feed", post.toRequestParameters());
	}
	
	public String post(String ownerId, String message) {
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		map.set("message", message);
		return graphApi.publish(ownerId, "feed", map);
	}

	public void deletePost(String id) {
		graphApi.delete(id);
	}

	public PagedList<Post> searchPublicFeed(String query) {
		return searchPublicFeed(query, FIRST_PAGE);
	}
	
	public PagedList<Post> searchPublicFeed(String query, PagingParameters pagedListParameters) {
		String url = GraphApi.GRAPH_API_URL + "search?q={query}&type=post";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("query", query);
		if (pagedListParameters.getLimit() != null) {
			url += "&limit={limit}";
			params.put("limit", pagedListParameters.getLimit());
		}
		if (pagedListParameters.getSince() != null) {
			url += "&since={since}";
			params.put("since", pagedListParameters.getSince());
		}
		if (pagedListParameters.getUntil() != null) {
			url += "&until={until}";
			params.put("until", pagedListParameters.getUntil());
		}
		JsonNode responseNode = restTemplate.getForObject(url, JsonNode.class, params);
		return deserializeList(responseNode, null, Post.class);
	}
	
	public PagedList<Post> searchHomeFeed(String query) {
		return searchHomeFeed(query, FIRST_PAGE);
	}
	
	public PagedList<Post> searchHomeFeed(String query, PagingParameters pagedListParameters) {
		requireAuthorization();
		URIBuilder uriBuilder = URIBuilder.fromUri(GraphApi.GRAPH_API_URL + "me/home").queryParam("q", query);
		uriBuilder = appendPagedListParameters(pagedListParameters, uriBuilder);
		URI uri = uriBuilder.build();
		JsonNode responseNode = restTemplate.getForObject(uri, JsonNode.class);
		return deserializeList(responseNode, null, Post.class);
	}

	public PagedList<Post> searchUserFeed(String query) {
		return searchUserFeed("me", query, FIRST_PAGE);
	}

	public PagedList<Post> searchUserFeed(String query, PagingParameters pagedListParameters) {
		return searchUserFeed("me", query, pagedListParameters);
	}

	public PagedList<Post> searchUserFeed(String userId, String query) {
		return searchUserFeed(userId, query, FIRST_PAGE);
	}
	
	public PagedList<Post> searchUserFeed(String userId, String query, PagingParameters pagedListParameters) {
		requireAuthorization();
		URIBuilder uriBuilder = URIBuilder.fromUri(GraphApi.GRAPH_API_URL + userId + "/feed").queryParam("q", query);
		uriBuilder = appendPagedListParameters(pagedListParameters, uriBuilder);		
		URI uri = uriBuilder.build();
		JsonNode responseNode = restTemplate.getForObject(uri, JsonNode.class);
		return deserializeList(responseNode, null, Post.class);
	}
	
	public PagedList<Post> getCheckins() {
		return getCheckins(new PagingParameters(25, 0, null, null));
	}

	public PagedList<Post> getCheckins(PagingParameters pagedListParameters) {
		MultiValueMap<String, String> params = getPagingParameters(pagedListParameters);
		params.set("with", "location");
		return graphApi.fetchConnections("me", "posts", Post.class, params);
	}

	public Post getCheckin(String checkinId) {
		return graphApi.fetchObject(checkinId, Post.class);
	}
	
	// private helpers
	
	private JsonNode fetchConnectionList(String baseUri, PagingParameters pagedListParameters, MultiValueMap<String, String> queryParameters, String... fields) {
		if(fields.length > 0 && queryParameters != null) {
			String joinedFields = join(fields);
			queryParameters.set("fields", joinedFields);
		}
		URIBuilder uriBuilder = URIBuilder.fromUri(baseUri);
		uriBuilder = appendPagedListParameters(pagedListParameters, uriBuilder);
		if (queryParameters != null) {
			uriBuilder = uriBuilder.queryParams(queryParameters);
		}
		URI uri = uriBuilder.build();
		JsonNode responseNode = restTemplate.getForObject(uri, JsonNode.class);
		return responseNode;
	}

	private String join(String[] strings) {
		StringBuilder builder = new StringBuilder();
		if(strings.length > 0) {
			builder.append(strings[0]);
			for (int i = 1; i < strings.length; i++) {
				builder.append("," + strings[i]);
			}
		}
		return builder.toString();
	}

	private <T> PagedList<T> deserializeList(JsonNode jsonNode, String postType, Class<T> type) {
		JsonNode dataNode = jsonNode.get("data");
		List<T> posts = new ArrayList<T>();
		for (Iterator<JsonNode> iterator = dataNode.iterator(); iterator.hasNext();) {
			posts.add(deserializePost(postType, type, (ObjectNode) iterator.next()));
		}
		if (jsonNode.has("paging")) {
			JsonNode pagingNode = jsonNode.get("paging");
			PagingParameters previousPage = getPagedListParameters(pagingNode, "previous");
			PagingParameters nextPage = getPagedListParameters(pagingNode, "next");
			return new PagedList<T>(posts, previousPage, nextPage);
		}
		
		return new PagedList<T>(posts, null, null);
	}

	private <T> T deserializePost(String postType, Class<T> type, ObjectNode node) {
		try {
			if (postType == null) {
				postType = determinePostType(node);
			}

			// Must have separate postType field for polymorphic deserialization. If we key off of the "type" field, then it will
			// be null when trying to deserialize the type property.
			node.put("postType", postType); // used for polymorphic deserialization
			node.put("type", postType); // used to set Post's type property
			node.put("hasLikes", containsContent("likes", node)); // used to check if contains at least 1 like
			node.put("hasComments", containsContent("comments", node)); // used to check if contains at least 1 comm
			return objectMapper.reader(type).readValue(node.toString()); // TODO: EXTREMELY HACKY--TEMPORARY UNTIL I FIGURE OUT HOW JACKSON 2 DOES THIS
		} catch (IOException shouldntHappen) {
			throw new UncategorizedApiException("facebook", "Error deserializing " + postType + " post", shouldntHappen);
		}
	}

	private String determinePostType(ObjectNode node) {
		if (node.has("type")) {
			try {
				String type = node.get("type").textValue();
				PostType.valueOf(type.toUpperCase());
				return type;
			} catch (IllegalArgumentException e) {
				return "post";
			}
		}
		return "post";
	}

	private boolean containsContent(String tag, ObjectNode node) {
		if (!node.has(tag)) {
			return false;
		}
		try {
			JsonNode jsonTag = node.get(tag);
			if (!jsonTag.has("data")) {
				return false;
			}
			return jsonTag.elements().hasNext();
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	private URIBuilder appendPagedListParameters(PagingParameters pagedListParameters,
			URIBuilder uriBuilder) {
		if (pagedListParameters.getLimit() != null) {
			uriBuilder = uriBuilder.queryParam("limit", String.valueOf(pagedListParameters.getLimit()));
		}
		if (pagedListParameters.getSince() != null) {
			uriBuilder = uriBuilder.queryParam("since", String.valueOf(pagedListParameters.getSince()));
		}
		if (pagedListParameters.getUntil() != null) {
			uriBuilder = uriBuilder.queryParam("until", String.valueOf(pagedListParameters.getUntil()));
		}
		if (pagedListParameters.getAfter() != null) {
			uriBuilder = uriBuilder.queryParam("after", String.valueOf(pagedListParameters.getAfter()));
		}
		if (pagedListParameters.getBefore() != null) {
			uriBuilder = uriBuilder.queryParam("before", String.valueOf(pagedListParameters.getBefore()));
		}
		if (pagedListParameters.getPagingToken() != null) {
			uriBuilder = uriBuilder.queryParam("__paging_token", String.valueOf(pagedListParameters.getPagingToken()));
		}
		if (pagedListParameters.getOffset() != null) {
			uriBuilder = uriBuilder.queryParam("offset", String.valueOf(pagedListParameters.getOffset()));
		}
		return uriBuilder;
	}

}
