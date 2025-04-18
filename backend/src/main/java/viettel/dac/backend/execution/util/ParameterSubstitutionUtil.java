package viettel.dac.backend.execution.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParameterSubstitutionUtil {

    private final ObjectMapper objectMapper;

    // Pattern for parameter placeholders: ${paramName}
    private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    /**
     * Substitute parameters in a string using the ${param} syntax.
     */
    public String substituteString(String template, Map<String, Object> parameters) {
        if (template == null || parameters == null || parameters.isEmpty()) {
            return template;
        }

        Matcher matcher = PARAMETER_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String paramName = matcher.group(1);
            Object paramValue = parameters.get(paramName);

            if (paramValue == null) {
                log.warn("Parameter '{}' not found in parameters", paramName);
                // Leave the placeholder unchanged
                matcher.appendReplacement(result, "\\${" + paramName + "}");
            } else {
                // Replace with the parameter value
                matcher.appendReplacement(result, Matcher.quoteReplacement(paramValue.toString()));
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Substitute parameters in a map of strings.
     */
    public Map<String, String> substituteMap(Map<String, String> templateMap, Map<String, Object> parameters) {
        if (templateMap == null || templateMap.isEmpty() || parameters == null || parameters.isEmpty()) {
            return templateMap;
        }

        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : templateMap.entrySet()) {
            String key = substituteString(entry.getKey(), parameters);
            String value = substituteString(entry.getValue(), parameters);
            result.put(key, value);
        }

        return result;
    }

    /**
     * Substitute parameters in a JSON object or string.
     */
    public Object substituteJson(Object jsonObject, Map<String, Object> parameters) {
        if (jsonObject == null || parameters == null || parameters.isEmpty()) {
            return jsonObject;
        }

        try {
            // Convert to JsonNode for manipulation
            JsonNode node;
            if (jsonObject instanceof String) {
                node = objectMapper.readTree((String) jsonObject);
            } else {
                node = objectMapper.valueToTree(jsonObject);
            }

            // Perform substitution
            JsonNode result = substituteJsonNode(node, parameters);

            // Convert back to original type
            if (jsonObject instanceof String) {
                return objectMapper.writeValueAsString(result);
            } else {
                return objectMapper.treeToValue(result, jsonObject.getClass());
            }
        } catch (JsonProcessingException e) {
            throw new ExecutionException("Error processing JSON for parameter substitution", e);
        }
    }

    /**
     * Recursively substitute parameters in a JsonNode.
     */
    private JsonNode substituteJsonNode(JsonNode node, Map<String, Object> parameters) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            ObjectNode result = objectNode.deepCopy();

            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                JsonNode value = entry.getValue();

                // Substitute the key if it contains placeholders
                String newKey = substituteString(key, parameters);

                // Recursively substitute in the value
                JsonNode newValue = substituteJsonNode(value, parameters);

                // Remove old key and add new key with new value
                if (!newKey.equals(key)) {
                    result.remove(key);
                }
                result.set(newKey, newValue);
            }

            return result;
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            ArrayNode result = arrayNode.deepCopy();

            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode item = arrayNode.get(i);
                JsonNode newItem = substituteJsonNode(item, parameters);
                result.set(i, newItem);
            }

            return result;
        } else if (node.isTextual()) {
            String text = node.asText();
            String substituted = substituteString(text, parameters);
            return substituted.equals(text) ? node : objectMapper.valueToTree(substituted);
        } else {
            return node;
        }
    }
}
