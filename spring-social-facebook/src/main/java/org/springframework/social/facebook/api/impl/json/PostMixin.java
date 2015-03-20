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
package org.springframework.social.facebook.api.impl.json;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.social.facebook.api.*;
import org.springframework.social.facebook.api.Post.FriendsPrivacyType;
import org.springframework.social.facebook.api.Post.PostType;
import org.springframework.social.facebook.api.Post.Privacy;
import org.springframework.social.facebook.api.Post.StatusType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

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
    @JsonDeserialize(using = PrivacyDeserializer.class)
	public abstract static class PrivacyMixin {
		
		@JsonProperty("description")
		String description;
		
		@JsonProperty("value")
		Privacy value;
		
		@JsonProperty("friends")
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
	}

    private static class PrivacyDeserializer extends JsonDeserializer<Privacy> {
        @Override
        public Privacy deserialize(JsonParser jp, DeserializationContext context)
                throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            String description = node.get("description").asText();
            Post.PrivacyType value = Post.PrivacyType.valueOf(node.get("value").asText().toUpperCase());

            String friendsPrivacyString = node.get("friends").asText().toUpperCase();
            FriendsPrivacyType friends = "".equals(friendsPrivacyString) ? FriendsPrivacyType.UNKNOWN
                    : FriendsPrivacyType.valueOf(friendsPrivacyString);
            String allow = node.get("allow").asText();
            String deny = node.get("deny").asText();

            return new Privacy(description, value, friends, allow, deny);
        }
    }
}
