package org.xiaoyu.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoyu.chat.prompt.PromptConfig;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class ChatClientExample {

    @Value("classpath:/prompts/system-message.st")
    private Resource systemText;

    private final ChatClient chatClient;

    @Autowired
    private PromptConfig promptConfig;

    public ChatClientExample(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    /**
     * 当前用户输入后，返回文本类型的回答
     * @return
     */
    @PostMapping("/chat")
    public String chat(@RequestParam("userInput")  String userInput) {
        String systemPrompt = "你是的名字是小雨助手";
        String content = this.chatClient.prompt()
                .system(systemPrompt)
                .user(userInput)
                .call()
                .content();
        log.info("content: {}", content);
        return content;
    }

    @PostMapping("/chat-promptTemplate")
    public String chatPromptTemplate(@RequestParam("userInput")  String userInput) {
        PromptTemplate promptTemplate = new PromptTemplate("你是的名字是{name},只会回答关于{develop}编程语言的问题");
        Prompt prompt = promptTemplate.create(Map.of("name",  "小雨助手", "develop", "Java"));
        String content = this.chatClient.prompt(prompt)
                .user(userInput)
                .call()
                .content();
        log.info("content: {}", content);
        return content;
    }

    @PostMapping("/chat-systemPromptTemplate")
    public String chatSystemPromptTemplate(@RequestParam("userInput")  String userInput) {
        String systemText = """
                          你是的名字是{name},只会回答关于{develop}编程语言的问题
                          """;
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("name",  "小雨助手", "develop", "Go"));
        Message userMessage = new UserMessage("Java语言如何？");
        Message assistantMessage =  new AssistantMessage("对不起，我只会回答关于Go相关的语言，请询问我Go编程语言相关内容！");
        Prompt prompt = new Prompt(List.of(systemMessage,userMessage, assistantMessage));
        String content = this.chatClient.prompt(prompt)
                .user(userInput)
                .call()
                .content();
        log.info("content: {}", content);
        return content;
    }

    @PostMapping("/chat-systemResource")
    public String chatSystemResource(@RequestParam("userInput")  String userInput) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("name",  "小雨助手", "develop", "Python"));

        Prompt prompt = new Prompt(systemMessage);
        String content = this.chatClient.prompt(prompt)
                .user(userInput)
                .call()
                .content();
        log.info("content: {}", content);
        return content;
    }

    @PostMapping("/chat-systemURLResource")
    public String chatSystemURLResource(@RequestParam("userInput")  String userInput) throws MalformedURLException {
        Resource systemText = new UrlResource("http://localhost/system%2Dmessage.st");
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("name",  "小雨助手", "develop", "HTML"));

        Prompt prompt = new Prompt(systemMessage);
        String content = this.chatClient.prompt(prompt)
                .user(userInput)
                .call()
                .content();
        log.info("content: {}", content);
        return content;
    }

    @PostMapping("/chat-nacosResource")
    public String chatNacosResource(@RequestParam("userInput")  String userInput) throws MalformedURLException {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(promptConfig.getSystemVuePrompt());
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("name",  "小雨助手"));

        Prompt prompt = new Prompt(systemMessage);
        String content = this.chatClient.prompt(prompt)
                .user(userInput)
                .call()
                .content();
        log.info("content: {}", content);
         systemPromptTemplate = new SystemPromptTemplate(promptConfig.getSystemJavaPrompt());
         systemMessage = systemPromptTemplate.createMessage(Map.of("name",  "小红助手"));

        prompt = new Prompt(systemMessage);
        content = this.chatClient.prompt(prompt)
                .user(userInput)
                .call()
                .content();
        log.info("content: {}", content);
        return content;
    }
}
