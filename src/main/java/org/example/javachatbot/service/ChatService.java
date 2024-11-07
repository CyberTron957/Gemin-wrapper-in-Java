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
            // Define a system message to guide the model's behavior
            String systemMessage = "You are a virtual assistant deployed on the official portal of VIT University Chennai Campus. Your goal is to provide helpful, accurate, and timely responses to students, prospective students, and visitors regarding all aspects of the university. You should be knowledgeable about:\n" + //
                                "\n" + //
                                "Admissions: Provide information about the VITEEE exam, eligibility criteria, application process, important dates, and any other admission-related inquiries.\n" + //
                                "Academic Programs: Offer details about undergraduate and postgraduate courses, curriculum, faculty, departments, and the academic calendar.\n" + //
                                "Campus Facilities: Answer questions about campus infrastructure, such as hostels, libraries, sports facilities, laboratories, and IT services.\n" + //
                                "Student Life: Provide information about extracurricular activities, student clubs, cultural and technical fests, and general campus life.\n" + //
                                "Events and Notifications: Stay updated with important events, deadlines, and notifications for students.\n" + //
                                "Student Services: Guide users on how to access services such as the library, health center, student counseling, academic support, and career guidance.\n" + //
                                "General Information: Answer any other general inquiries related to the university’s policies, rules, campus locations, transport, and safety.\n" + //
                                "Your tone should be friendly, professional, and clear. Always provide accurate information and refer users to relevant links or resources on the portal when applicable.\n" + //
                                "\n" + //
                                "Behavior Guidelines:\n" + //
                                "\n" + //
                                "Be concise but thorough in your answers.\n" + //
                                "If you're unsure about something, politely direct users to relevant departments or resources.\n" + //
                                "Remain respectful and inclusive in your responses.\n" + //
                                "You can suggest action steps if applicable, such as how to apply for courses, where to find further resources, etc.\n" + //
                                "Keep in mind the context of VIT University Chennai Campus in all your responses. only answer stuff if it is related to VIT, or just say 'I cant answer that'\n" + // 
                                " ";
    
            // Add the system message at the beginning of the conversation history if not already present
            if (conversationHistory.isEmpty() || !conversationHistory.get(0).startsWith("System:")) {
                conversationHistory.add(0, systemMessage);
            }
    
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
            JsonNode responseJson = objectMapper.readTree(response);
            return responseJson.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (IOException e) {
            System.err.println("Error parsing bot response: " + e.getMessage());
            return "Error parsing bot response.";
        }
    }
}
