package com.hmall.trade.listeners;


import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayStatusListener {

    private final IOrderService orderService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "mark.order.pay.queue", durable = "true"),
            exchange = @Exchange(name = "pay.topic", type = ExchangeTypes.TOPIC),
            key = "pay.success"
    ))
    //TODO 还是监听器问题，这里的监听器由于无法获得更新
    public void listenOrderPay(Long orderId){
  /*      //1. 查询订单
        Order order = orderService.getById(orderId);
        System.out.println("监听器生效");
        //2.判断订单状态是否为未支付
        if (order == null || order.getStatus() != 1){
            // 订单不存在或者状态异常
            return;
        }
        //3. 如果未支付，标记订单状态为已支付
        orderService.markOrderPaySuccess(orderId);*/

        //考虑到并发问题，会出现多次修改status的问题
        //使用乐观锁,重复执行只会执行一次
        //update order set status = 2 where id = ？ AND status = 1
       /* orderService.lambdaUpdate().set(Order::getStatus, 2)
        .eq(Order::getId, orderId)
                .eq(Order::getPayTime, LocalTime.now())
                .eq(Order::getStatus, 1)
                .update();*/
        log.warn("PayStatusListener 收到到的信息" + "/n" +
                "需要更改信息的订单号:{}", orderId);
    }
}
