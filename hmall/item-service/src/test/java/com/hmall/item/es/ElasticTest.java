package com.hmall.item.es;


import cn.hutool.db.sql.Order;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.support.json.JSONUtils;
import com.hmall.common.utils.CollUtils;
import com.hmall.item.domain.dto.ItemDTO;
import com.hmall.item.domain.dto.ItemDocDTO;
import io.lettuce.core.ScriptOutputType;
import org.apache.http.HttpHost;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.stats.SearchStats;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Stats;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.print.DocFlavor;
import javax.swing.text.Highlighter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ElasticTest {

    private RestHighLevelClient client;


    @Test
    void testSearchDemo() throws IOException {
        //1.准备request
        SearchRequest request = new SearchRequest("items");
        //2.准备DSL请求参数
        request.source()
                .query(QueryBuilders.matchAllQuery());
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4.结果解析
        SearchHits searchHits = response.getHits();
        //4.1 获取total数据
        long totalValue = searchHits.getTotalHits().value;
        System.out.println("total value: " + totalValue);
        //4.2 取数据
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            //4.4 获取source
            String json = hit.getSourceAsString();
            ItemDocDTO itemDoc = JSONUtil.toBean(json, ItemDocDTO.class);
            System.out.println("itemDoc: " + itemDoc);
        }
    }

    @Test
    void testComplexSearchDemo() throws IOException {
        //1.准备request
        SearchRequest request = new SearchRequest("items");
        //2.准备DSL请求参数
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //2.1关键字搜索
        queryBuilder.must(QueryBuilders.matchQuery("name","脱脂牛奶"));
        //2.2过滤条件
        queryBuilder.filter(QueryBuilders.termQuery("brand","德亚"));
        queryBuilder.filter(QueryBuilders.rangeQuery("price").lt(30000));

        request.source().query(queryBuilder);

        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4.结果解析
        SearchHits searchHits = response.getHits();
        //4.1 获取total数据
        long totalValue = searchHits.getTotalHits().value;
        System.out.println("total value: " + totalValue);
        //4.2 取数据
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            //4.4 获取source
            String json = hit.getSourceAsString();
            ItemDocDTO itemDoc = JSONUtil.toBean(json, ItemDocDTO.class);
            System.out.println("itemDoc: " + itemDoc);
        }
    }

    @Test
    void testSearchPageAndSortDemo() throws IOException {
       int pageNo = 1, pageSize = 10;
        //1.准备request
        SearchRequest request = new SearchRequest("items");
        //2.准备DSL请求参数
        //2.1搜索条件
        request.source().query(QueryBuilders.matchAllQuery());
        //2.2分页条件
        request.source().from((pageNo - 1)*pageSize).size(pageSize);
        //2.3排序条件
        request.source().sort("price", SortOrder.ASC);

        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4.结果解析
        SearchHits searchHits = response.getHits();
        //4.1 获取total数据
        long totalValue = searchHits.getTotalHits().value;
        System.out.println("total value: " + totalValue);
        //4.2 取数据
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            //4.4 获取source
            String json = hit.getSourceAsString();
            ItemDocDTO itemDoc = JSONUtil.toBean(json, ItemDocDTO.class);
            System.out.println("itemDoc: " + itemDoc);
        }
    }

    @Test
    void testSearchHeightDemo() throws IOException {
        //1.准备request
        SearchRequest request = new SearchRequest("items");
        //2.准备DSL请求参数
        //2.1搜索条件
        request.source().query(QueryBuilders.matchQuery("name", "脱脂牛奶"));
        //2.2高亮查询
        request.source().highlighter(
                SearchSourceBuilder.highlight().field("name")
        );
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4.结果解析
        System.out.println("response: " + response);
    }


    @Test
    void testSearchDeHeightDemo() throws IOException {
        //1.准备request
        SearchRequest request = new SearchRequest("items");
        //2.准备DSL请求参数
        //2.1搜索条件
        request.source().query(QueryBuilders.matchQuery("name", "脱脂牛奶"));
        //2.2高亮查询
        request.source().highlighter(
                SearchSourceBuilder.highlight().field("name")
        );
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4.结果解析
        SearchHits hits = response.getHits();
        for (SearchHit hit : hits) {
            //4.1得到原始的source
            String json = hit.getSourceAsString();
            //4.2反序列化
            ItemDocDTO itemDoc = JSONUtil.toBean(json, ItemDocDTO.class);
            //4.3获取高亮结果
            Map<String, HighlightField> hfs = hit.getHighlightFields();
            //4.4提取高亮结果
            if (!CollUtils.isEmpty(hfs)){
                //4.5 有高亮结果，获取name的高亮结果
                HighlightField hf = hfs.get("name");
                if (hf != null){
                    //4.6 获取第一个高亮的结果
                    String hfResult = hf.getFragments()[0].toString();
                    //4.7 替换itemDoc中的name
                    itemDoc.setName(hfResult);
                    //4.8替换完之后输出
                    System.out.println("itemDoc: " + itemDoc.getName());
                }
            }


        }
    }


    @Test
    void testAggs() throws IOException {
        //1.准备request
        SearchRequest request = new SearchRequest("items");
        //2.准备DSL请求参数
        //2.1搜索条件
        request.source().query(QueryBuilders.termQuery("category", "手机"));
        //2.2 设置搜索结果的数量
        request.source().size(0);
        //2.3 对品牌进行聚合(并进行嵌套）
        String aggsName = "brand_aggs";
        request.source().aggregation(
                AggregationBuilders
                        .terms(aggsName)
                        .field("brand")
                        .size(20)
                        .subAggregation(
                                AggregationBuilders
                                        .stats("price_statis")
                                        .field("price")
                        )
        );
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4.结果解析
        Aggregations aggregations = response.getAggregations();
        //4.1 根据名称获取聚合结果
        Terms brandTerms = aggregations.get(aggsName);
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        //4.2 遍历
        for (Terms.Bucket bucket : buckets) {
            //获取key
            String brandName = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            //获取嵌套的stats聚合结果
            Stats priceStats = bucket.getAggregations().get("price_statis");
            double avgPrice = priceStats.getAvg();
            double maxPrice = priceStats.getMax();
            double minPrice = priceStats.getMin();
            long count = priceStats.getCount();
            double sum = priceStats.getSum();

            System.out.printf("品牌: %s, 商品数: %d, 平均价: %.2f, 最高价: %.2f, 最低价: %.2f%n",
                    brandName, docCount, avgPrice, maxPrice, minPrice);
        }
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

    private static final String MAPPING_TEMPLATE = "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\":{\n" +
            "        \"type\": \"long\",\n" +
            "        \"index\": true\n" +
            "      },\n" +
            "      \"name\":{\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_smart\",\n" +
            "        \"index\": true\n" +
            "      },\n" +
            "      \"category\":{\n" +
            "        \"type\": \"keyword\",\n" +
            "        \"index\": true\n" +
            "      },\n" +
            "      \"brand\":{\n" +
            "        \"type\": \"keyword\",\n" +
            "        \"index\": true\n" +
            "      },\n" +
            "      \"price\":{\n" +
            "        \"type\": \"integer\",\n" +
            "        \"index\": true\n" +
            "      },\n" +
            "      \"sold\":{\n" +
            "        \"type\": \"integer\",\n" +
            "        \"index\": true\n" +
            "      },\n" +
            "      \"image\":{\n" +
            "        \"type\": \"keyword\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"commentCount\":{\n" +
            "        \"type\": \"integer\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"isAD\":{\n" +
            "        \"type\": \"boolean\",\n" +
            "        \"index\": true\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

}
