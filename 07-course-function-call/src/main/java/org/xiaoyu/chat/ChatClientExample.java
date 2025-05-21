package org.xiaoyu.chat;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoyu.chat.tools.WeatherService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @PostMapping("/chat1")
    public String chat1() {
        MapOutputConverter mapOutputConverter = new MapOutputConverter();
        String format = mapOutputConverter.getFormat();
        String systemPrompt = "你是一位个人助理，可以帮助用户解决日常问题，比如：查询天气情况，当用户查询天气情况时，你必须返回经纬度信息固定格式如：{json},key为{key}";
        Map<String, Double> content = this.chatClient.prompt()
                .system(s->s.text(systemPrompt).params(Map.of("json", format, "key","latitude,longitude")))
                .user("帮我看下北京的天气")
                .call()
                .entity(new ParameterizedTypeReference<>(){});
        log.info("content: {}", content);
        WeatherService weatherService = new WeatherService();
        Double latitude = content.get("latitude");
        Double longitude = content.get("longitude");
        String result = weatherService.getAirQuality(latitude, longitude);
        return result;
    }
    @PostMapping("/chat2")
    public String chat2() throws InvocationTargetException, IllegalAccessException {
        MapOutputConverter mapOutputConverter = new MapOutputConverter();
        String format = mapOutputConverter.getFormat();
        String systemPrompt = "你是一位个人助理，可以帮助用户解决日常问题，比如：查询天气情况，当用户查询天气情况时，你必须返回经纬度信息固定格式如：{json},key为{key}";
        Method[] declaredMethods = WeatherService.class.getDeclaredMethods();
        Method GET_AIR_QUALITY_METHOD = null;
        String keyName = "";
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.getDeclaredAnnotations().length>0 && declaredMethod.getDeclaredAnnotations()[0].annotationType().isAssignableFrom(Tool.class)) {
                keyName = Arrays.stream(declaredMethod.getParameterTypes()).map(Class::getName) // 获取类型名称
                        .collect(Collectors.joining(","));
                GET_AIR_QUALITY_METHOD = declaredMethod;
            }
        }
        final String finalKeyName = keyName; // 声明final变量
        Map<String, Double> content = this.chatClient.prompt()
                .system(s->s.text(systemPrompt).params(Map.of("json", format, "key",finalKeyName)))
                .user("帮我看下北京的天气")
                .call()
                .entity(new ParameterizedTypeReference<>(){});
        log.info("content: {}", content);
        // ReflectionUtils.findMethod
        Object[] methodArgs = Stream.of(GET_AIR_QUALITY_METHOD.getParameters()).map(parameter -> {
            Class<?> type = parameter.getType();
            Object rawValue = content.get(parameter.getName());
            if (type == double.class) {
                return Double.parseDouble(rawValue.toString());
            } else {
                return rawValue.toString();
            }
        }).toArray();

        Object response = ReflectionUtils.invokeMethod(GET_AIR_QUALITY_METHOD, new WeatherService() , methodArgs);
        return String.valueOf(response);
    }


    @PostMapping("/chat3")
    public String chat3() {
        String systemPrompt = "你是一位个人助理，可以帮助用户解决日常问题，比如：查询天气情况";
        String content = this.chatClient.prompt()
                .system(s->s.text(systemPrompt))
                .user("帮我看下北京的天气")
                .tools(new WeatherService())
                .call()
                .content();
        log.info("content: {}", content);
        return content;
    }

    @PostMapping("/chat4")
    public String chat4() throws NoSuchMethodException {
        Method getAirQuality = WeatherService.class.getMethod("getAirQuality", double.class, double.class);
        String systemPrompt = "你是一位个人助理，可以帮助用户解决日常问题，比如：查询天气情况";
        String content = this.chatClient.prompt()
                .system(s->s.text(systemPrompt))
                .user("帮我看下北京的天气")
                .tools(MethodToolCallback.builder()
                        .toolDefinition(ToolDefinition.builder(getAirQuality)
                                .description("Get the weather in location")
                                .build())
                        .toolMethod(getAirQuality)
                        .toolObject(new WeatherService())
                        .build())
                .call()
                .content();
        log.info("content: {}", content);
        return content;
    }

    @PostMapping("/chat5")
    public String chat5() {
        String systemPrompt = "你是一位个人助理，可以帮助用户解决日常问题，比如：查询天气情况";
        String content = this.chatClient.prompt()
                .system(s->s.text(systemPrompt))
                .user("帮我看下北京的天气")
                .tools(new WeatherService())
                .toolContext(Map.of("latitude", 1.9))
                .call()
                .content();
        log.info("content: {}", content);
        return content;
    }
}
