package org.xiaoyu.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoyu.chat.advisor.ReReadingAdvisor;

import java.util.List;

@Slf4j
@RestController
public class ChatClientExample {


    @Autowired
    VectorStore vectorStore;
    @Autowired
    ChatMemoryRepository chatMemoryRepository;

    private final ChatClient chatClient;

    private final ChatMemory chatMemory;

    public ChatClientExample(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultAdvisors(new SimpleLoggerAdvisor()).build();
        this.chatMemory = MessageWindowChatMemory.builder().chatMemoryRepository(chatMemoryRepository).build();
    }

    @PostMapping("/messageChatMemoryAdvisor")
    public String messageChatMemoryAdvisor(String userText) {
        String response = this.chatClient.prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                // Set advisor parameters at runtime
                .advisors(advisor -> advisor.param("chat_memory_conversation_id", "678")
                        .param("chat_memory_response_size", 20))
                .user(userText)
                .call()
                .content();
        return response;
    }
    @PostMapping("/messageChatMemoryAdvisor-clear")
    public void messageChatMemoryAdvisor() {
        chatMemory.clear("678");
    }

    @PostMapping("/promptChatMemoryAdvisor")
    public String promptChatMemoryAdvisor(String userText) {
        String response = this.chatClient.prompt()
                .advisors(PromptChatMemoryAdvisor.builder(chatMemory).build())
                // Set advisor parameters at runtime
                .advisors(advisor -> advisor.param("chat_memory_conversation_id", "678")
                        .param("chat_memory_response_size", 20))
                .user(userText)
                .call()
                .content();
        return response;
    }

    @PostMapping("/questionAnswerAdvisorBefore")
    public void questionAnswerAdvisorBefore(String userText) {
        List<Document> documents = List.of(new Document(userText));

        vectorStore.add(documents);
    }

    @PostMapping("/questionAnswerAdvisor")
    public String questionAnswerAdvisor(String userText) {
        String response = this.chatClient.prompt()
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .promptTemplate(PromptTemplate.builder().template("请根据下面的提示回答用户的问题：{question_answer_context}").build()).build())
                .user(userText)
                .call()
                .content();
        return response;
    }

    @PostMapping("/safeGuardAdvisor")
    public String safeGuardAdvisor(String userText) {
        String response = this.chatClient.prompt()
                .advisors(new SafeGuardAdvisor(List.of("暴力"),"对不起，我拒绝回答此类问题",0))
                .user(userText)
                .call()
                .content();
        return response;
    }

    @PostMapping("/vectorStoreChatMemoryAdvisor")
    public String vectorStoreChatMemoryAdvisor(String userText) {
        String response = this.chatClient.prompt()
                .advisors(VectorStoreChatMemoryAdvisor.builder(vectorStore).build())
                .user(userText)
                .call()
                .content();
        return response;
    }

    @PostMapping("/reReadingAdvisor")
    public String ReReadingAdvisor(String userText) {
        String response = this.chatClient.prompt()
                .advisors(new ReReadingAdvisor())
                .user(userText)
                .call()
                .content();
        return response;
    }






}
