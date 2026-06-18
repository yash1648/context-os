package com.grim.contextos.websocket.event;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class WebSocketEventForwarder {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventForwarder(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleDomainEvent(DomainEvent event) {
        messagingTemplate.convertAndSend("/topic/events", event);
    }
}
