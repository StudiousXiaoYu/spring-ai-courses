package org.xiaoyu.chat;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>类的作用说明</p>
 *
 * @version 1.0
 * @since 2025/03/20 13:42:33
 */
@RestController
public class ClientController {

    private final ChatMemory chatMemory;
    private final ChatClient chatClient;

    public ClientController(ChatClient.Builder chatClientBuilder,ChatMemoryRepository chatMemoryRepository,List<McpSyncClient> mcpClients){
        this.chatMemory  = MessageWindowChatMemory.builder().chatMemoryRepository(chatMemoryRepository).build();
        var mcpToolProvider = new SyncMcpToolCallbackProvider(mcpClients);
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultToolCallbacks(mcpToolProvider)
                .build();
    }



    @GetMapping("/client")
    public String predefinedQuestions(String userInput) {
        String content = chatClient.prompt().user(userInput)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .system("你是一位专业的数据分析师").call().content();
        System.out.println("\n>>> ASSISTANT: " + content);
        return content;
    }
}
