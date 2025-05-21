package org.xiaoyu.chat;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.EQ;

@Slf4j
@RestController
public class RetrievalAugmentationExample {

    @Autowired
    VectorStore vectorStore;

    private final ChatClient chatClient;

    public RetrievalAugmentationExample(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultAdvisors(new SimpleLoggerAdvisor()).build();
    }

    @PostMapping("/retrievalAugmentationAdvisor1")
    public String retrievalAugmentationAdvisor1(String userText) {
        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(vectorStore)
                        .build())
                .build();

        String response = chatClient.prompt()
                .advisors(retrievalAugmentationAdvisor)
                .user(userText)
                .call()
                .content();
        return response;
    }

    @PostMapping("/retrievalAugmentationAdvisor2")
    public String retrievalAugmentationAdvisor2(String userText) {
        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(vectorStore)
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .promptTemplate(new PromptTemplate("You are a helpful assistant that answers questions about the following documents:\n{documents}\n\n{query}"))
                        .build())
                .build();

        String response = chatClient.prompt()
                .advisors(retrievalAugmentationAdvisor)
                .user(userText)
                .call()
                .content();
        return response;
    }

    @PostMapping("/retrievalAugmentationAdvisor3")
    public String retrievalAugmentationAdvisor3(String userText) {
        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.70)
                        .vectorStore(vectorStore)
                        .build())
                .build();

        String answer = chatClient.prompt()
                .advisors(retrievalAugmentationAdvisor)
                .advisors(a -> a.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION, "createtime == '2025-04-13'"))
                .user(userText)
                .call()
                .content();
        return answer;
    }

    @PostMapping("/retrievalAugmentationAdvisor4")
    public String retrievalAugmentationAdvisor4(String userText) {
        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(RewriteQueryTransformer.builder()
                        .chatClientBuilder(chatClient.mutate())
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(vectorStore)
                        .build())
                .build();

        String answer = chatClient.prompt()
                .advisors(retrievalAugmentationAdvisor)
                .user(userText)
                .call()
                .content();
        return answer;
    }

    @PostMapping("/queryTransformer")
    public String queryTransformer() {
        String text = "And what is its second largest city?";
        JSONObject obj = JSONUtil.createObj();
        obj.append("origin-text:", text);
        Query query = Query.builder()
                .text(text)
                .history(new UserMessage("What is the capital of Denmark?"),
                        new AssistantMessage("Copenhagen is the capital of Denmark."))
                .build();
        QueryTransformer queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .build();
        Query transformedQuery = queryTransformer.transform(query);
        text = transformedQuery.text();
        obj.append("RewriteQueryTransformer:", text);
        queryTransformer = CompressionQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .build();

        transformedQuery = queryTransformer.transform(query);
        text = transformedQuery.text();
        obj.append("CompressionQueryTransformer:", text);

        queryTransformer = TranslationQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .targetLanguage("中文")
                .build();

        transformedQuery = queryTransformer.transform(query);
         text = transformedQuery.text();
         obj.append("TranslationQueryTransformer:", text);
        return obj.toString();
    }

    @PostMapping("/queryExpander")
    public String queryExpander(String userText) {
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClient.mutate())
                .numberOfQueries(3)
                .build();
        List<Query> queries = queryExpander.expand(new Query(userText));
        return queries.stream().map(query -> query.text()).collect(Collectors.joining(";"));
    }


    @PostMapping("/documentRetriever")
    public String documentRetriever(String userText) {
        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.73)
                .topK(5)
                .filterExpression(new FilterExpressionBuilder()
                        .eq("createtime", "2025-04-13")
                        .build())
                .build();
        List<Document> documents = retriever.retrieve(new Query(userText));
        return documents.stream().map(document -> document.getText()).collect(Collectors.joining(";"));
    }


}
