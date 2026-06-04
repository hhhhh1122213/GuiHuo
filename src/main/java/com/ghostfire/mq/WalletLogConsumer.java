package com.ghostfire.mq;

import com.ghostfire.config.RabbitConfig;
import com.ghostfire.entity.UserStat;
import com.ghostfire.entity.UserWalletLog;
import com.ghostfire.service.UserStatService;
import com.ghostfire.service.UserWalletLogService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletLogConsumer {

    private final UserWalletLogService userWalletLogService;
    private final UserStatService userStatService;

    @RabbitListener(queues = RabbitConfig.WALLET_LOG_QUEUE)
    public void handle(Map<String, Object> msg, Channel channel,
                       @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            Object userIdObj = msg.get("userId");
            Object amountObj = msg.get("amount");
            if (userIdObj == null || amountObj == null) {
                log.error("钱包流水消息字段缺失，丢弃: {}", msg);
                channel.basicAck(deliveryTag, false);
                return;
            }
            Long userId = ((Number) userIdObj).longValue();
            Long amount = ((Number) amountObj).longValue();
            String type = (String) msg.get("type");
            Long refId = msg.get("refId") != null ? ((Number) msg.get("refId")).longValue() : null;

            // 从 DB 读取当前余额（此时 coin 已提交）
            UserStat stat = userStatService.getById(userId);
            long currentBalance = stat != null ? stat.getCoin() : 0L;

            UserWalletLog log = new UserWalletLog();
            log.setUserId(userId);
            log.setAmount(amount);
            log.setCurrentBalance(currentBalance);
            log.setType(type);
            log.setRefId(refId);
            userWalletLogService.save(log);

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("钱包流水写入失败: {}", msg, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
