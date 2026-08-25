package com.cartflow.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Sends order lifecycle events to Kafka topics. */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    static final String TOPIC_ORDER_PLACED  = "order.placed";
    static final String TOPIC_STATUS_CHANGED = "order.status.changed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderPlaced(OrderPlacedEvent event) {
        kafkaTemplate.send(TOPIC_ORDER_PLACED, String.valueOf(event.getOrderId()), event);
        log.info("Published order.placed for orderId={}", event.getOrderId());
    }

    public void publishStatusChanged(OrderStatusChangedEvent event) {
        kafkaTemplate.send(TOPIC_STATUS_CHANGED, String.valueOf(event.getOrderId()), event);
        log.info("Published order.status.changed orderId={} → {}", event.getOrderId(), event.getNewStatus());
    }
}
