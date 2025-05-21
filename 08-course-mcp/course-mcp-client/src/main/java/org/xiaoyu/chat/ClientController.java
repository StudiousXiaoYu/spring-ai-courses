package org.xiaoyu.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>类的作用说明</p>
 *
 * @version 1.0
 * @since 2025/03/20 13:42:33
 */
@RestController
public class ClientController {

    @Autowired
    ChatClient.Builder chatClientBuilder;
    @Autowired
    ToolCallbackProvider tools;
    @Autowired
    InMemoryChatMemory memory;


    @GetMapping("/client")
    public String predefinedQuestions(String userInput) {
        var chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultTools(tools)
                .build();
        String content = chatClient.prompt().user(userInput).advisors(new MessageChatMemoryAdvisor(memory)).system("你是一位专业的数据分析师").call().content();
        System.out.println("\n>>> ASSISTANT: " + content);
        return content;
    }
}
