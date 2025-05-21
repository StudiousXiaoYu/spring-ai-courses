package org.xiaoyu.chat.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 访问：http://localhost:9149/doc.html
 * 或：http://localhost:9149/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("spring-ai-demo")
                        .contact(new Contact())
                        .description("努力的小雨-API文档")
                        .version("v1")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("外部文档")
                        .url("https://springshop.wiki.github.org/docs"));
    }

    @Bean
    public SummaryMetadataEnricher summaryMetadata(ChatModel aiClient) {
        return new SummaryMetadataEnricher(aiClient,
                List.of(SummaryMetadataEnricher.SummaryType.PREVIOUS, SummaryMetadataEnricher.SummaryType.CURRENT, SummaryMetadataEnricher.SummaryType.NEXT));
    }
}