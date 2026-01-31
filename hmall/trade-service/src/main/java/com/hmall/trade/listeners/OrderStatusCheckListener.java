package com.hmall.trade.listeners;


import com.hmall.common.constants.MqConstants;
import com.hmall.common.domain.MultiDelayMessage;
import com.hmall.common.mq.DelayMessageProcessor;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.mockito.stubbing.ValidableAnswer;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

//@Component
//TODO  暂时没有mq的delay插件
@RequiredArgsConstructor
public class OrderStatusCheckListener {


    private final IOrderService orderService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConstants.DELAY_ORDER_QUEUE, durable = "true"),
            exchange = @Exchange(value = MqConstants.DELAY_EXCHANGE, delayed = "true", type = ExchangeTypes.TOPIC),
            key = MqConstants.DELAY_ORDER_ROUTING_KEY
    ))
    public void listenerOrderDelayMessage(MultiDelayMessage<Long> msg){
        //1.查询订单状态
        Order order = orderService.getById(msg.getData());
        //2. 判断是否已经支付
        if (order == null || order.getStatus() == 2){
            //订单不存在或者已经结束
            return;
        }

        //TODO 3 去支付服务查询真正的支付状态（如果没通知到位）

        boolean isPay = false;
        //3.1 已支付，标记订单状态为已支付
        if(isPay){
            orderService.markOrderPaySuccess(order.getId());
            return;
        }
        //4. 判断是否需要延迟时间
        if (msg.hasNextDelay()){
            //4.1存在，重发延迟消息
            long nextDelayTime = msg.removeFirst();
            rabbitTemplate.convertAndSend(MqConstants.DELAY_EXCHANGE, MqConstants.DELAY_ORDER_ROUTING_KEY,
                    msg, new DelayMessageProcessor((int)nextDelayTime));
            return;
        }else {
            //5 不存在，则取消订单
        /*    orderService.lambdaUpdate()
                    .set(Order::getStatus, 5)
                    .set(Order::getCloseTime, LocalDateTime.now())
                    .eq(Order::getId, order.getId())
                    .update();*/
            //TODO 取消订单和恢复库存由
            //由于取消订单和恢复库存是一个事务，而且是个分布式事务，所以把它放到一个事务
            orderService.cancelOrder(order.getId());

        }


    }
}
