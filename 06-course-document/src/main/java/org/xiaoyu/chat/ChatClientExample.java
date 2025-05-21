package org.xiaoyu.chat;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.jsoup.config.JsoupDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class ChatClientExample {

    private final ChatClient chatClient;

    private final ChatModel chatModel;

    private final SummaryMetadataEnricher enricher;


    public ChatClientExample(ChatClient.Builder chatClientBuilder, ChatModel chatModel, SummaryMetadataEnricher enricher) {
        this.chatClient = chatClientBuilder.defaultAdvisors(new SimpleLoggerAdvisor()).build();
        this.chatModel = chatModel;
        this.enricher = enricher;
    }

    @PostMapping("/tikaDocumentReader")
    public List<Document> tikaDocumentReader() {
        var resource = new ClassPathResource("/file/word.docx");
//        var resource = new ClassPathResource("/file/RestClient.pdf");
//        ExtractedTextFormatter formatter = ExtractedTextFormatter.builder()
//                .overrideLineSeparator("\n") //设置换行符
//                .withNumberOfTopTextLinesToDelete(10) //删除前10行
//                .withLeftAlignment(true) // 左对齐
//                .withNumberOfBottomTextLinesToDelete(10) //删除后10行
//                .withNumberOfTopPagesToSkipBeforeDelete(1) //跳过前1页
//                .build();3
//        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource, formatter);
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
        return tikaDocumentReader.read();
    }
    @PostMapping("/getDocsFromPdf")
    public List<Document> getDocsFromPdf() {
        var resource = new ClassPathResource("/file/RestClient.pdf");

        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource,
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0) //设置页边距
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)  //删除前10行
                                .build())
                        .withPagesPerDocument(1) // 1页1个文档
                        .build());

        return pdfReader.read();
    }
    @PostMapping("/getDocsFromPdfWithCatalog")
    public List<Document> getDocsFromPdfWithCatalog() {
        ParagraphPdfDocumentReader pdfReader = new ParagraphPdfDocumentReader("classpath:/file/kes.pdf");

        return pdfReader.read();
    }

    @PostMapping("/getDocsFromHTML")
    public List<Document> getDocsFromHTML() {

        var resource = new ClassPathResource("/file/ETL.html");

        JsoupDocumentReaderConfig config = JsoupDocumentReaderConfig.builder()
                .selector("p")
                .charset("ISO-8859-1")
                .includeLinkUrls(true) // 包括链接
                .metadataTags(List.of("generator", "version")) // 从页面中获取generator、version元数据信息
                .additionalMetadata("source", "ETL.html") //  添加自定义元数据信息
                .build();

        JsoupDocumentReader reader = new JsoupDocumentReader(resource, config);
        return reader.get();
    }

    @PostMapping("/getDocsFromJson")
    public List<Document> getDocsFromJson() {

        var resource = new ClassPathResource("/file/info.json");

        JsonReader jsonReader = new JsonReader(resource, "commentContent", "personalIntroduction");
        return jsonReader.get();
    }



    @PostMapping("/splitCustomized")
    public List<Document> splitCustomized() {
        var resource = new ClassPathResource("/file/kes.pdf");

        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource);
        // 分词器
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(100) // 拆分长度
                .withMinChunkSizeChars(40) // 最小拆分长度
                .withMinChunkLengthToEmbed(10) // 最小拆分长度
                .withMaxNumChunks(500) // 最大拆分数量
                .withKeepSeparator(true) // 保留分隔符
                .build();
        return splitter.apply(pdfReader.read());
    }

    @PostMapping("/enrichDocumentsByKeyword")
    public List<Document> enrichDocumentsByKeyword() {
        var resource = new ClassPathResource("/file/kes.pdf");

        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource);
//        TokenTextSplitter splitter = new TokenTextSplitter(100, 40, 10, 500, true);
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(this.chatModel, 5);
//        return enricher.apply(splitter.apply(pdfReader.read()));
        return enricher.apply(pdfReader.read());
    }

    @PostMapping("/enrichDocumentsBySummary")
    public List<Document> enrichDocumentsBySummary() {
        var resource = new ClassPathResource("/file/ETL.html");

        JsoupDocumentReaderConfig config = JsoupDocumentReaderConfig.builder()
                .selector("p")
                .charset("UTF-8")
                .metadataTags(List.of("generator", "version")) // Extract author and date meta tags
                .build();

        JsoupDocumentReader reader = new JsoupDocumentReader(resource, config);
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> documentList = enricher.apply(splitter.apply(reader.read()));
        System.out.println(JSONUtil.toJsonStr(documentList));
        return documentList;
    }

}
