import { useEffect, useState } from 'react';
import { gql } from '@apollo/client';
import logger from '/imports/startup/client/logger';
import { CustomSubscriptionArguments } from 'averox-html-plugin-sdk/dist/cjs/data-consumption/domain/shared/custom-subscription/types';
import { SubscribedEventDetails, UpdatedEventDetails } from 'averox-html-plugin-sdk/dist/cjs/core/types';
import {
  HookEvents,
} from 'averox-html-plugin-sdk/dist/cjs/core/enum';
import { DataConsumptionHooks } from 'averox-html-plugin-sdk/dist/cjs/data-consumption/enums';

import { HookWithArgumentsContainerProps } from './types';
import useDeduplicatedSubscription from '/imports/ui/core/hooks/useDeduplicatedSubscription';

/* eslint-disable @typescript-eslint/no-explicit-any */
const CustomSubscriptionHookContainer = (props: HookWithArgumentsContainerProps) => {
  const { hookArguments } = props;
  const { query: queryFromPlugin, variables } = hookArguments;

  const [sendSignal, setSendSignal] = useState(false);

  let customSubscriptionData: any;
  try {
    const subscriptionResult = useDeduplicatedSubscription(gql`${queryFromPlugin}`, {
      variables,
    });
    customSubscriptionData = subscriptionResult;
  } catch (err) {
    logger.error(
      `Error while querying custom subscriptions for plugins (query: ${queryFromPlugin}) (Error: ${err})`,
    );
    customSubscriptionData = 'Error';
  }

  const updatePresentationForPlugin = () => {
    window.dispatchEvent(
      new CustomEvent<UpdatedEventDetails<any>>(
        HookEvents.UPDATED,
        {
          detail: {
            data: customSubscriptionData,
            hook: DataConsumptionHooks.CUSTOM_SUBSCRIPTION,
            hookArguments: {
              query: queryFromPlugin,
              variables,
            } as CustomSubscriptionArguments,
          } as UpdatedEventDetails<any>,
        },
      ),
    );
  };

  useEffect(() => {
    updatePresentationForPlugin();
  }, [customSubscriptionData, sendSignal]);

  useEffect(() => {
    const updateHookUseCustomSubscription = ((event: CustomEvent<SubscribedEventDetails>) => {
      if (event.detail.hook === DataConsumptionHooks.CUSTOM_SUBSCRIPTION) setSendSignal((signal) => !signal);
    }) as EventListener;
    window.addEventListener(
      HookEvents.SUBSCRIBED, updateHookUseCustomSubscription,
    );
    return () => {
      window.removeEventListener(
        HookEvents.SUBSCRIBED, updateHookUseCustomSubscription,
      );
    };
  }, []);

  return null;
};

export default CustomSubscriptionHookContainer;
