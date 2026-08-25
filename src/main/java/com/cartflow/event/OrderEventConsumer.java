package com.cartflow.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes order events — simulates downstream processing such as
 * sending confirmation emails or updating fulfillment systems.
 */
@Component
@Slf4j
public class OrderEventConsumer {

    @KafkaListener(topics = OrderEventProducer.TOPIC_ORDER_PLACED, groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderPlaced(OrderPlacedEvent event) {
        // In production this would trigger email, inventory reservation, etc.
        log.info("Received order.placed: orderId={}, total={}", event.getOrderId(), event.getTotal());
    }

    @KafkaListener(topics = OrderEventProducer.TOPIC_STATUS_CHANGED, groupId = "${spring.kafka.consumer.group-id}")
    public void onStatusChanged(OrderStatusChangedEvent event) {
        log.info("Received order.status.changed: orderId={} {} → {}",
                event.getOrderId(), event.getPreviousStatus(), event.getNewStatus());
    }
}
