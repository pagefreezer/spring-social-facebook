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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.social.facebook.api.CoverPhoto;

import java.io.IOException;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = CoverPhotoMixin.CoverPhotoDeserializer.class)
abstract class CoverPhotoMixin extends FacebookObjectMixin {

	@JsonProperty("cover_id")
	String id;

	@JsonProperty("source")
	String source;

	@JsonProperty("offset_x")
	int offsetX;

	@JsonProperty("offset_y")
	int offsetY;

	static class CoverPhotoDeserializer extends JsonDeserializer<CoverPhoto> {

		@Override
		public CoverPhoto deserialize(JsonParser jp, DeserializationContext context) throws IOException {
			JsonNode node = jp.getCodec().readTree(jp);
			if (node.has("cover")) {
				node = node.get("cover");
			}

			String id = node.has("id") ? node.get("id").asText() : node.get("cover_id").asText();
			String source = node.has("source") ? node.get("source").asText() : null;
			int x = node.has("offset_x") ? node.get("offset_x").asInt() : 0;
			int y = node.has("offset_y") ? node.get("offset_y").asInt() : 0;
			return new CoverPhoto(id, source, x, y);
		}

	}

}
