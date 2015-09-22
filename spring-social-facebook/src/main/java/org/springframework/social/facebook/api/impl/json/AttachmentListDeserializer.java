package org.springframework.social.facebook.api.impl.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import org.springframework.social.facebook.api.StoryAttachment;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class AttachmentListDeserializer extends JsonDeserializer<List<StoryAttachment>> {

    @SuppressWarnings("unchecked")
    @Override
    public List<StoryAttachment> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new FacebookModule());
        jp.setCodec(mapper);
        if (jp.hasCurrentToken()) {
            JsonNode dataNode = (JsonNode) jp.readValueAs(JsonNode.class).get("data");
            if (dataNode != null) {
                return (List<StoryAttachment>) mapper.reader(new TypeReference<List<StoryAttachment>>() {
                }).readValue(dataNode);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<StoryAttachment> getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return Collections.emptyList();
    }
}