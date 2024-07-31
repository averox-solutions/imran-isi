import { useEffect, useState } from 'react';
import useCurrentPresentation from '/imports/ui/core/hooks/useCurrentPresentation';
import * as PluginSdk from 'averox-html-plugin-sdk';
import {
  HookEvents,
} from 'averox-html-plugin-sdk/dist/cjs/core/enum';
import { SubscribedEventDetails } from 'averox-html-plugin-sdk/dist/cjs/core/types';
import { DataConsumptionHooks } from 'averox-html-plugin-sdk/dist/cjs/data-consumption/enums';

import { CurrentPresentation } from '/imports/ui/Types/presentation';
import formatCurrentPresentation from './utils';

const CurrentPresentationHookContainer = () => {
  const [sendSignal, setSendSignal] = useState(false);

  const currentPresentation = useCurrentPresentation(
    (currentPresentationData: Partial<CurrentPresentation>) => currentPresentationData,
  );

  const updatePresentationForPlugin = () => {
    const formattedCurrentPresentation:
      PluginSdk.GraphqlResponseWrapper<PluginSdk.CurrentPresentation> = formatCurrentPresentation(
        currentPresentation,
      );

    window.dispatchEvent(
      new CustomEvent(
        HookEvents.UPDATED,
        {
          detail: {
            data: formattedCurrentPresentation,
            hook: DataConsumptionHooks.CURRENT_PRESENTATION,
          },
        },
      ),
    );
  };

  useEffect(() => {
    updatePresentationForPlugin();
  }, [currentPresentation, sendSignal]);

  useEffect(() => {
    const updateHookUseCurrentPresentation = ((event: CustomEvent<SubscribedEventDetails>) => {
      if (event.detail.hook === DataConsumptionHooks.CURRENT_PRESENTATION) setSendSignal((signal) => !signal);
    }) as EventListener;
    window.addEventListener(
      HookEvents.SUBSCRIBED, updateHookUseCurrentPresentation,
    );
    return () => {
      window.removeEventListener(
        HookEvents.SUBSCRIBED, updateHookUseCurrentPresentation,
      );
    };
  }, []);

  return null;
};

export default CurrentPresentationHookContainer;
