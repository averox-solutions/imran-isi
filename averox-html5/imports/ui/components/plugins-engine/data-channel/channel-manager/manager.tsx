import React from 'react';
import * as PluginSdk from 'averox-html-plugin-sdk';

import { createChannelIdentifier } from 'averox-html-plugin-sdk/dist/cjs/data-channel/utils';
import { DataChannelTypes } from 'averox-html-plugin-sdk/dist/cjs/data-channel/enums';

import { DataChannelItemManagerReader } from './reader-manager';
import DataChannelItemManagerWriter from './writer-manager';

export interface DataChannelItemManagerProps {
  identifier: string;
  pluginName: string;
  channelName: string;
  subChannelName: string;
  pluginApi: PluginSdk.PluginApi;
  dataChannelTypes: DataChannelTypes[];
}

export const DataChannelItemManager: React.ElementType<DataChannelItemManagerProps> = (
  props: DataChannelItemManagerProps,
) => {
  const {
    identifier,
    pluginName,
    channelName,
    pluginApi,
    dataChannelTypes,
    subChannelName,
  } = props;

  const dataChannelIdentifier = createChannelIdentifier(channelName, subChannelName, pluginName);

  return (
    <>
      <DataChannelItemManagerWriter
        {...{
          pluginName,
          channelName,
          pluginApi,
          dataChannelTypes,
          subChannelName,
          dataChannelIdentifier,
        }}
      />
      {
        dataChannelTypes.map((type) => (
          <DataChannelItemManagerReader
            {...{
              key: identifier?.concat('::')?.concat(type),
              pluginName,
              channelName,
              dataChannelType: type,
              subChannelName,
              dataChannelIdentifier,
            }}
          />
        ))
      }
    </>
  );
};
