package org.xiaoyu.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoyu.chat.domain.ActorFilms;
import org.xiaoyu.chat.function.DateTimeTools;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@RestController
public class ChatClientExample {

    private final ChatClient chatClient;


    public ChatClientExample(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    /**
     * 当前用户输入后，返回文本类型的回答
     * @return
     */
    @PostMapping("/chat")
    public String chat(@RequestParam("userInput")  String userInput) {
        String content = this.chatClient.prompt()
                .user(userInput)
                .call()
                .content();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 当前用户输入后，返回文本类型的回答，流式回答
     * @return
     */
    @GetMapping("/chat-stream")
    public Flux<ServerSentEvent<String>> chatStream(@RequestParam("userInput") String userInput) {
        return chatClient.prompt()
                .user(userInput)
                .stream()
                .content() // 获取原始Flux<String>
                .map(content -> ServerSentEvent.<String>builder() // 封装为SSE事件
                        .data(content)
                        .build());
    }

    /**
     * 当前用户输入后，返回一个实体类型的回答，
     * @return ActorFilms
     */
    @GetMapping("/ai-Entity")
    public ActorFilms aiEntity() {
        ActorFilms actorFilms = chatClient.prompt()
                .user("Generate the filmography for a random actor.")
                .call()
                .entity(ActorFilms.class);
        return actorFilms;
    }

    /**
     *当前用户输入后，返回列表实体类型的回答，ParameterizedTypeReference是一个泛型，用于指定返回的类型。
     * @return List<ActorFilms>
     */
    @GetMapping("/ai-EntityList")
    List<ActorFilms> generationByEntityList() {
        List<ActorFilms> actorFilms = chatClient.prompt()
                .user("Generate the filmography of 5 movies for Tom Hanks and Bill Murray.")
                .call()
                .entity(new ParameterizedTypeReference<List<ActorFilms>>() {
                });
        return actorFilms;
    }

    @PostMapping("/ai-function")
    String functionGenerationByText(@RequestParam("userInput")  String userInput) {
        String content = this.chatClient
                .prompt()
                .user(userInput)
                .tools(new DateTimeTools())
                .call()
                .content();
        log.info("content: {}", content);
        return content;
    }

}
