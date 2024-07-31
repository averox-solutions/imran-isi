package org.averox.api.messaging;

import java.util.Set;

import org.averox.api.messaging.messages.IMessage;

public class MessageDistributor {
    private ReceivedMessageHandler handler;
    private Set<MessageListener> listeners;

    public void setMessageListeners(Set<MessageListener> listeners) {
        this.listeners = listeners;
    }

    public void setMessageHandler(ReceivedMessageHandler handler) {
        this.handler = handler;
        if (handler != null) {
            handler.setMessageDistributor(this);
        }
    }

    public void notifyListeners(IMessage message) {
        for (MessageListener listener : listeners) {
            listener.handle(message);
        }
    }
}
