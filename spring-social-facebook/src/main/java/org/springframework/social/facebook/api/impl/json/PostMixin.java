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
package org.springframework.social.facebook.api.impl.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.social.facebook.api.*;
import org.springframework.social.facebook.api.Post.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Annotated mixin to add Jackson annotations to Post.
 * Also defines Post subtypes to deserialize into based on the "type" attribute. 
 * @author Craig Walls
 */
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class PostMixin extends FacebookObjectMixin {
	
	@JsonProperty("id")
	String id;
	
	@JsonProperty("actions")
	List<Action> actions;
	
	@JsonProperty("admin_creator")
	AdminCreator adminCreator;
	
	@JsonProperty("application")
	Reference application;

	@JsonProperty("caption")
	String caption;
	
	@JsonProperty("created_time")
	Date createdTime; 

	@JsonProperty("description")
	String description;
	
	@JsonProperty("from") 
	Reference from; 
	
	@JsonProperty("icon")
	String icon;
	
	@JsonProperty("is_hidden")
	boolean isHidden;
	
	@JsonProperty("is_published")
	boolean isPublished;

	@JsonProperty("attachments")
	@JsonDeserialize(using=AttachmentListDeserializer.class)
	List<StoryAttachment> attachments;

	@JsonProperty("link")
	String link;
	
	@JsonProperty("message")
	String message;
	
	@JsonProperty("message_tags")
	@JsonDeserialize(using=MessageTagMapDeserializer.class)
	Map<Integer,List<MessageTag>> messageTags;
	
	@JsonProperty("name")
	String name;
	
	@JsonProperty("object_id")
	String objectId;
	
	@JsonProperty("picture")
	String picture;
	
	@JsonProperty("place")
	Page place;
	
	@JsonProperty("privacy")
	Privacy privacy;
	
	@JsonProperty("properties")
	List<PostProperty> properties;
	
	@JsonProperty("source")
	String source;
	
	@JsonProperty("status_type")
	@JsonDeserialize(using = StatusTypeDeserializer.class)
	StatusType statusType;
	
	@JsonProperty("story")
	String story;
	
	@JsonProperty("to")
	@JsonDeserialize(using = ReferenceListDeserializer.class)
	List<Reference> to;
	
	@JsonProperty("type")
	@JsonDeserialize(using = PostTypeDeserializer.class)
	PostType type;
	
	@JsonProperty("updated_time")
	Date updatedTime;

	@JsonProperty("with_tags")
	@JsonDeserialize(using = ReferenceListDeserializer.class)
	List<Reference> withTags;
	
	@JsonProperty("shares")
	@JsonDeserialize(using = CountDeserializer.class)
	Integer sharesCount;

	@JsonIgnoreProperties(ignoreUnknown = true)
	public abstract static class AdminCreatorMixin {
		
		@JsonProperty("id")
		String id;
		
		@JsonProperty("name")
		String name;
		
		@JsonProperty("namespace")
		String namespace;
		
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public abstract static class PrivacyMixin {
		
		@JsonProperty("description")
		String description;
		
		@JsonProperty("value")
		@JsonDeserialize(using = PrivacyTypeDeserializer.class)
		PrivacyType value;
		
		@JsonProperty("friends")
		@JsonDeserialize(using = FriendsPrivacyTypeDeserializer.class)
		FriendsPrivacyType friends;
		
		@JsonProperty("networks")
		String networks;
		
		@JsonProperty("allow")
		String allow;
		
		@JsonProperty("deny")
		String deny;

	}
	
	private static class PostTypeDeserializer extends JsonDeserializer<PostType> {
		@Override
		public PostType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			try {
				return PostType.valueOf(jp.getText().toUpperCase());
			} catch (IllegalArgumentException e) {
				return PostType.UNKNOWN;
			}
		}
	}

	private static class PrivacyTypeDeserializer extends JsonDeserializer<PrivacyType> {
		@Override
		public PrivacyType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			try {
				return PrivacyType.valueOf(jp.getText().toUpperCase());
			} catch (IllegalArgumentException e) {
				return PrivacyType.UNKNOWN;
			}
		}
	}

	private static class FriendsPrivacyTypeDeserializer extends JsonDeserializer<FriendsPrivacyType> {
		@Override
		public FriendsPrivacyType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			try {
				return FriendsPrivacyType.valueOf(jp.getText().toUpperCase());
			} catch (IllegalArgumentException e) {
				return FriendsPrivacyType.UNKNOWN;
			}
		}
	}

	private static class StatusTypeDeserializer extends JsonDeserializer<StatusType> {
		@Override
		public StatusType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			return StatusType.valueOf(jp.getText().toUpperCase());
		}
	}

	private static class CountDeserializer extends JsonDeserializer<Integer> {
		@Override
		public Integer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			Map map = jp.readValueAs(Map.class);
			return map.containsKey("count") ? Integer.valueOf(String.valueOf(map.get("count"))): 0; 
		}
		
		@Override
		public Integer getNullValue(DeserializationContext ctxt) throws JsonMappingException {
			return 0;
		}
	}

	private class AttachmentListDeserializer extends JsonDeserializer<List<StoryAttachment>> {
		@SuppressWarnings("unchecked")
		@Override
		public List<StoryAttachment> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new FacebookModule());
			jp.setCodec(mapper);
			if (jp.hasCurrentToken()) {
				JsonNode dataNode = (JsonNode) jp.readValueAs(JsonNode.class).get("data");
				if (dataNode != null) {
					return (List<StoryAttachment>) mapper.reader(new TypeReference<List<StoryAttachment>>() {}).readValue(dataNode);
				}
			}

			return Collections.emptyList();
		}
	}
}
