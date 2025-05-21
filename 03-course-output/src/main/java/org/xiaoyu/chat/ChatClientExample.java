package org.xiaoyu.chat;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
    public String chat() {
        String systemPrompt = "你是是电影检索助手，你会返回用户查询的电影演员以及其作品供用户查看，你的响应必须是JSON格式。";
        String content = this.chatClient.prompt()
                .system(systemPrompt)
                .user("给我关于成龙以及其5个作品信息")
                .call()
                .content();
        log.info("content: {}", content);
        return content;
    }

    @PostMapping("/chat-jsontemplate")
    public String chatJsonTemplate() {
        String systemPrompt = """
                你是是电影检索助手，你会返回用户查询的电影演员以及其作品供用户查看，你的响应必须是JSON格式。JSON格式严格按照以下格式返回： 
                {"name":"成龙","movies":["警察故事"]}
                """;
        String content = this.chatClient.prompt()
                .system(systemPrompt)
                .user("给我关于李连杰以及其5个作品信息")
                .call()
                .content();
        log.info("content: {}", content);
        return content;
    }

    @PostMapping("/chat-entityTemplate")
    public String entityTemplate() {
        String systemPrompt = """
                你是是电影检索助手，你会返回用户查询的电影演员以及其作品供用户查看。
                """;
        Actor content = this.chatClient.prompt()
                .system(systemPrompt)
                .user("给我关于吴京以及其5个作品信息")
                .call()
                .entity(Actor.class);
        log.info("content: {}", content);
        return JSONUtil.toJsonStr(content);
    }
    @PostMapping("/chat-entityTemplate-lower")
    public String entityTemplateLower() {
        BeanOutputConverter<Actor> beanOutputConverter = new BeanOutputConverter<>(Actor.class);

        String format = beanOutputConverter.getFormat();

        String systemPrompt = """
                你是是电影检索助手，你会返回用户查询的电影演员以及其作品供用户查看。
                """;
        String content = this.chatClient.prompt()
                .system(systemPrompt+format)
                .user("给我关于吴京以及其5个作品信息")
                .call()
                .content();
        Actor actorsFilms = beanOutputConverter.convert(content);
        log.info("content: {}", actorsFilms);
        return JSONUtil.toJsonStr(actorsFilms);
    }

    @PostMapping("/chat-listEntityTemplate")
    public String listEntityTemplate() {
        String systemPrompt = """
                你是是电影检索助手，你会返回用户查询的电影演员以及其作品供用户查看。
                """;
        List<Actor> content = this.chatClient.prompt()
                .system(systemPrompt)
                .user("给我关于5个动作演员以及其5个作品信息")
                .call()
                .entity(new ParameterizedTypeReference<>(){});
        log.info("content: {}", content);
        return JSONUtil.toJsonStr(content);
    }

    @PostMapping("/chat-listEntityTemplate-lower")
    public String listEntityTemplateLower() {
        BeanOutputConverter<List<Actor>> outputConverter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<>() { });

        String format = outputConverter.getFormat();

        String systemPrompt = """
                你是是电影检索助手，你会返回用户查询的电影演员以及其作品供用户查看。
                """;
        String result = this.chatClient.prompt()
                .system(systemPrompt+format)
                .user("给我关于5个动作演员以及其5个作品信息")
                .call()
                .content();
        List<Actor> content = outputConverter.convert(result);
        log.info("content: {}", content);
        return JSONUtil.toJsonStr(content);
    }

    @PostMapping("/chat-mapTemplate")
    public String mapTemplate() {
        String systemPrompt = """
                你是是电影检索助手，你会返回用户查询的电影演员以及其作品供用户查看。
                """;
        Map<String, Object>  content = this.chatClient.prompt()
                .system(systemPrompt)
                .user("给我关于5个动作演员以及其5个作品信息")
                .call()
                .entity(new ParameterizedTypeReference<>(){});
        log.info("content: {}", content);
        return JSONUtil.toJsonStr(content);
    }

    @PostMapping("/chat-mapTemplate-lower")
    public String mapTemplateLower() {
        MapOutputConverter mapOutputConverter = new MapOutputConverter();

        String format = mapOutputConverter.getFormat();
        String systemPrompt = """
                你是是电影检索助手，你会返回用户查询的电影演员以及其作品供用户查看。
                """;
        String  result = this.chatClient.prompt()
                .system(systemPrompt+format)
                .user("给我关于5个动作演员以及其5个作品信息")
                .call()
                .content();
        Map<String, Object> content = mapOutputConverter.convert(result);
        log.info("content: {}", content);
        return JSONUtil.toJsonStr(content);
    }

record Actor(String name, List<String> movies) {
}
}
