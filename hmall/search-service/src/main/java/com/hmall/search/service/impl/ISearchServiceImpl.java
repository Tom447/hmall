package com.hmall.search.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.service.ISearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.Index;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ISearchServiceImpl implements ISearchService {

    private final RestHighLevelClient client;
    private final String INDEX_NAME = "items";
    private final ItemClient itemClient;


    @Override
    public void saveItemById(Long itemId) {
        //根据id查询商品
        List<ItemDTO> items = itemClient.queryItemByIds(List.of(itemId));
        if (items.size() == 0) {
            log.error("查询商品失败");
            return;
        }
        ItemDTO itemDTO = items.get(0);

        //1.准备Request对象
        try {
            IndexRequest request = new IndexRequest(INDEX_NAME).id(itemId.toString());
            //2.准备请求参数
            request.source(JSONUtil.toJsonStr(BeanUtil.copyProperties(itemDTO, ItemDoc.class)), XContentType.JSON);
            //3.发送请求
            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("更新商品失败, 商品id" + itemId, e);
        }
    }

    @Override
    public void deleteById(Long itemId) {
        //1.准备Request对象
        try {
            DeleteRequest request = new DeleteRequest(INDEX_NAME, itemId.toString());
            //2.准备请求参数

            //3.发送请求
            client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("删除商品失败, 商品id" + itemId, e);
        }
    }
}
