package org.example.javachatbot.controller;

import org.example.javachatbot.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat")
    public String chat(Model model, HttpSession session) {
        List<String> conversationHistory = (List<String>) session.getAttribute("conversationHistory");
        if (conversationHistory == null) {
            conversationHistory = new ArrayList<>();
            session.setAttribute("conversationHistory", conversationHistory);
        }
        model.addAttribute("conversationHistory", conversationHistory);
        return "chat";
    }

    @PostMapping("/chat")
    @ResponseBody
    public String chat(@RequestBody String message, HttpSession session) {
        List<String> conversationHistory = (List<String>) session.getAttribute("conversationHistory");
        if (conversationHistory == null) {
            conversationHistory = new ArrayList<>();
            session.setAttribute("conversationHistory", conversationHistory);
        }
        return chatService.getResponse(message, conversationHistory);
    }
}
