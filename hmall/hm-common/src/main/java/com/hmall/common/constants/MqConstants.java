package com.hmall.common.constants;

public interface MqConstants {

    String TRADE_EXCHANGE_NAME = "trade.topic";
    String TRADE_CREATE_KEY = "order.create";

    
    String DELAY_EXCHANGE = "trade.delay.topic";
    String DELAY_ORDER_QUEUE = "trade.order.delay.queue";
    String DELAY_ORDER_ROUTING_KEY = "order.query";

    /**商品上下架*/
    String ITEM_EXCHANGE_NAME = "item.topic";
    String ITEM_UP_KEY = "item.up";
    String ITEM_DOWN_KEY = "item.down";
}
