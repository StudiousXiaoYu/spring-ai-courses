package org.xiaoyu.chat.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.HashMap;
import java.util.Map;

public class ReReadingAdvisor implements BaseAdvisor {

    private static final String DEFAULT_USER_TEXT_ADVISE = """
      {re2_input_query}
      Read the question again: {re2_input_query}
      """;
    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String inputQuery = chatClientRequest.prompt().getUserMessage().getText(); //original user query
        chatClientRequest.context().put("lastBefore", getName());
        String augmentedSystemText = PromptTemplate.builder().template(DEFAULT_USER_TEXT_ADVISE).build()
                .render(Map.of("re2_input_query", inputQuery));
        ChatClientRequest processedChatClientRequest = chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentUserMessage(augmentedSystemText))
                .build();
        return processedChatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        //我们不做任何处理
        return chatClientResponse;
    }
}