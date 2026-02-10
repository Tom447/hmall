package com.hmall.item.es;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.item.domain.dto.ItemDocDTO;
import com.hmall.item.domain.po.Item;
import com.hmall.item.service.IItemService;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootTest(properties = "spring.profiles.active=local")
public class ElasticDocumentTest {

    private RestHighLevelClient client;

    @Autowired
    private IItemService itemService;

    //全量修改
    @Test
    void testSaveDocument() throws Exception {
        //从数据库中查询商品数据
        Item item = itemService.getById(317578L);

        //1.准备request
        IndexRequest request = new IndexRequest("items").id(item.getId().toString());
        //2.准备请求参数
        request.source(JSONUtil.toJsonStr(BeanUtil.copyProperties(item, ItemDocDTO.class)), XContentType.JSON);
        //3.发送请求
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println("response: " + response);
    }

    @Test
    void testupdateDocument() throws Exception {
        //从数据库中查询商品数据
        Item item = itemService.getById(317578L);

        //1.准备request
        UpdateRequest request = new UpdateRequest("items", item.getId().toString());
        //2.准备请求参数
        /*  request.source(JSONUtil.toJsonStr(BeanUtil.copyProperties(item, ItemDocDTO.class)), XContentType.JSON);*/
        request.doc(
                "price", 2000,
                "stock", 200
        );
        //3.发送请求
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
        System.out.println("response: " + response);
    }

    @Test
    void testGetDocumentById() throws Exception {
        //从数据库中查询商品数据
        Item item = itemService.getById(317578L);

        //1.准备request
        GetRequest request = new GetRequest("items").id("317578");
        //2.发送请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        //3.解析请求结果
        String json = response.getSourceAsString();
        ItemDocDTO result = JSONUtil.toBean(json, ItemDocDTO.class);
        System.out.println(result);
    }

    @Test
    void testBulk() throws Exception {
        //从数据库中查询商品数据
        int pageNo = 1, pageSize = 1000;
        while (true){
            Page<Item> page = itemService.lambdaQuery().eq(Item::getStatus, 1).page(Page.of(pageNo, pageSize));
            List<Item> records = page.getRecords();
            if (records == null || records.size() == 0) {
                return;
            }
            //1.准备request
            BulkRequest request = new BulkRequest();
            //2.准备请求参数
            /*  request.source(JSONUtil.toJsonStr(BeanUtil.copyProperties(item, ItemDocDTO.class)), XContentType.JSON);*/
            for (Item item : records) {
                request.add(new IndexRequest("items")
                        .id(item.getId().toString())
                        .source(JSONUtil.toJsonStr(BeanUtil.copyProperties(item, ItemDocDTO.class)), XContentType.JSON));
            }
            //3.发送请求
            /*  UpdateResponse response = client.update(request, RequestOptions.DEFAULT);*/
            client.bulk(request, RequestOptions.DEFAULT);
            //4.翻页
            pageNo++;
        }
    }

    @Test
    void testGetDocument() throws Exception {
        //从数据库中查询商品数据
        Item item = itemService.getById(317578L);

        //1.准备request
        GetRequest request = new GetRequest("items").id("317578");
        //2.准备请求参数
        /* request.source(JSONUtil.toJsonStr(item),XContentType.JSON);*/
        //3.发送请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        System.out.println("response: " + response);
    }

    @Test
    void testDeleteDocument() throws Exception {
        //从数据库中查询商品数据
        Item item = itemService.getById(317578L);

        //1.准备request
        DeleteRequest request = new DeleteRequest("items").id(item.getId().toString());

        //2.准备请求参数
        /* request.source(JSONUtil.toJsonStr(item),XContentType.JSON);*/

        //3.发送请求
        DeleteResponse delete = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.toString());
    }


    @BeforeEach
        //每个单元测试前，先初始化
    void setUp() throws Exception {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://localhost:9201")
        ));
    }

    @AfterEach
        //执行完了之后进行关闭资源
    void tearDown() throws Exception {
        client.close();
    }


}
