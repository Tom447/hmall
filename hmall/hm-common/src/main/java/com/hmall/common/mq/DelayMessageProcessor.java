package com.hmall.common.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;


//TODO 暂时没有mq的delay插件

@RequiredArgsConstructor
public class DelayMessageProcessor implements MessagePostProcessor {

    private final int delay;

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        message.getMessageProperties().setDelay(delay);
        return null;
    }
}
