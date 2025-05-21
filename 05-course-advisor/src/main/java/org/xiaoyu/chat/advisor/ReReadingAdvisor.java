package org.xiaoyu.chat.advisor;

import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

import java.util.HashMap;
import java.util.Map;

public class ReReadingAdvisor implements BaseAdvisor {

    private static final String DEFAULT_USER_TEXT_ADVISE = """
      {re2_input_query}
      Read the question again: {re2_input_query}
      """;

    @Override
    public AdvisedRequest before(AdvisedRequest advisedRequest) {
        String inputQuery = advisedRequest.userText(); //original user query
        advisedRequest = advisedRequest.updateContext(context -> {
            context.put("lastBefore", getName());  // Add a single key-value pair
            return context;
        });
        Map<String, Object> params = new HashMap<>(advisedRequest.userParams());
        params.put("re2_input_query", inputQuery);

        return AdvisedRequest.from(advisedRequest)
                .userText(DEFAULT_USER_TEXT_ADVISE)
                .userParams(params)
                .build();
    }

    @Override
    public AdvisedResponse after(AdvisedResponse advisedResponse) {
        //我们不做任何处理
        return advisedResponse;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}