package org.xiaoyu.chat.prompt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "prompt")
public class PromptConfig {
    private String systemVuePrompt;
    private String systemJavaPrompt;
}