package org.averox.api;

import org.averox.api.messaging.messages.IMessage;

public interface IReceivedOldMessageHandler {
    void handleMessage(IMessage msg);
}
