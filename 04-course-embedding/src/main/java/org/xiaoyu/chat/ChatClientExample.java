package org.xiaoyu.chat;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class ChatClientExample {

    @Autowired
    VectorStore vectorStore;

    @PostMapping("/add")
    public void add() {
        List<Document> documents = List.of(
                new Document("有人不看好黄金还想再等等", Map.of("createtime", "2025-04-13")),
                new Document("黄金价格漂浮不定，有人已经开始疯狂进货"),
                new Document("黄金近日价格趋势", Map.of("remark", "黄金价格已经从去年的600+一致涨价到700+了")));

        vectorStore.add(documents);
    }

    @PostMapping("/findByQuery")
    public String findByQuery() {
        List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query("黄金今日价格").topK(1).build());
        return JSONUtil.toJsonStr(results);
    }

    @PostMapping("/delete")
    public void delete(String id) {
        vectorStore.delete(List.of(id));
    }

//    @PostMapping("/add")
//    public void add() {
//        var entity = MysqlEntity.builder().id("1").text("有人不看好黄金还想再等等").map(Map.of("createtime", "2025-04-13")).build();
//        List<Document> documents = List.of(
//                new Document(entity.getId(), entity.getText(),entity.getMap()));
//        mysqlMapper.insert(entity);
//        vectorStore.add(documents);
//    }

}
