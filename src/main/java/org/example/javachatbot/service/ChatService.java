package org.example.javachatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.List;

@Service
public class ChatService {

    private final WebClient webClient;

    // ObjectMapper to convert to JSON
    private final ObjectMapper objectMapper;

    public ChatService(WebClient webClient) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper(); // Initialize ObjectMapper
    }

    public String getResponse(String inputText, List<String> conversationHistory) {
        try {
            // Add the user's message to the conversation history
            conversationHistory.add("User: " + inputText);

            // Create the conversation history as a list of messages (for context)
            StringBuilder conversation = new StringBuilder();
            for (String message : conversationHistory) {
                conversation.append(message).append("\n");
            }

            // Construct the request body with the full conversation history
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode contentsArray = requestBody.putArray("contents");

            ObjectNode contentObject = objectMapper.createObjectNode();
            ArrayNode partsArray = contentObject.putArray("parts");

            // Include the conversation history as part of the request to give context
            ObjectNode conversationNode = objectMapper.createObjectNode();
            conversationNode.put("text", conversation.toString());  // Include entire conversation history
            partsArray.add(conversationNode);

            contentsArray.add(contentObject);

            // Convert the request body to a JSON string
            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
            System.out.println("JSON Request Body: " + jsonRequestBody);

            // Define the API URL with API key directly in the query string
            String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=AIzaSyATjScIv7jgtrLrApx7DkNT3kl95c7U89c";

            // Make the POST request to the API
            String response = this.webClient.post()
                    .uri(apiUrl)
                    .bodyValue(jsonRequestBody)  // Send JSON request body
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            System.out.println("API Response: " + response);

            // Process the API response
            String botResponse = parseBotResponse(response);

            // Add the bot's response to the conversation history
            conversationHistory.add("Bot: " + botResponse);

            return botResponse;

        } catch (WebClientResponseException e) {
            System.err.println("Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return "Error: " + e.getMessage();
        } catch (IOException e) {
            System.err.println("JSON parsing error: " + e.getMessage());
            return "Error: Unable to parse JSON.";
        }
    }


    // Helper function to parse the bot's response from the API response
    private String parseBotResponse(String response) {
        try {
            // Assuming the API response has the "content" field with the bot's text
            // Adjust this parsing logic based on the actual API response structure
            JsonNode responseJson = objectMapper.readTree(response);
            return responseJson.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (IOException e) {
            System.err.println("Error parsing bot response: " + e.getMessage());
            return "Error parsing bot response.";
        }
    }
}
