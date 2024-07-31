import { useContext, useEffect } from 'react';
import { HookEventWrapper, SubscribedEventDetails } from 'averox-html-plugin-sdk/dist/cjs/core/types';
import { HookEvents } from 'averox-html-plugin-sdk/dist/cjs/core/enum';
import { DomElementManipulationHooks } from 'averox-html-plugin-sdk/dist/cjs/dom-element-manipulation/enums';
import {
  ChatMessageDomElementsArguments,
} from 'averox-html-plugin-sdk/dist/cjs/dom-element-manipulation/chat/message/types';
import { PluginsContext } from '/imports/ui/components/components-data/plugin-context/context';

const ChatMessageDomElementManipulationHookManager = () => {
  const {
    setDomElementManipulationMessageIds,
  } = useContext(PluginsContext);

  useEffect(() => {
    const subscribeHandler: EventListener = (
      (event: HookEventWrapper<void>) => {
        let hookArguments: ChatMessageDomElementsArguments | undefined;
        if (event.detail.hook === DomElementManipulationHooks.CHAT_MESSAGE) {
          const detail = event.detail as SubscribedEventDetails;
          hookArguments = detail.hookArguments as ChatMessageDomElementsArguments;
          setDomElementManipulationMessageIds(hookArguments.messageIds);
        }
      }) as EventListener;
    const unsubscribeHandler: EventListener = (
      (event: HookEventWrapper<void>) => {
        let hookArguments: ChatMessageDomElementsArguments | undefined;
        if (event.detail.hook === DomElementManipulationHooks.CHAT_MESSAGE) {
          const detail = event.detail as SubscribedEventDetails;
          hookArguments = detail.hookArguments as ChatMessageDomElementsArguments;
          setDomElementManipulationMessageIds(hookArguments.messageIds);
        }
      }) as EventListener;

    window.addEventListener(HookEvents.SUBSCRIBED, subscribeHandler);
    window.addEventListener(HookEvents.UNSUBSCRIBED, unsubscribeHandler);
    return () => {
      window.removeEventListener(HookEvents.SUBSCRIBED, subscribeHandler);
      window.removeEventListener(HookEvents.UNSUBSCRIBED, unsubscribeHandler);
    };
  }, []);
  return null;
};

export default ChatMessageDomElementManipulationHookManager;
