import * as PluginSdk from 'averox-html-plugin-sdk';
import { DataChannelTypes } from 'averox-html-plugin-sdk/dist/cjs/data-channel/enums';

export interface PluginDataChannelManagerProps {
  pluginApi: PluginSdk.PluginApi;
}

export interface MapInformation {
  totalUses: number;
  subChannelName: string;
  channelName: string;
  types: DataChannelTypes[];
}

export interface SubscriptionResultFromGraphqlStream {
  pluginDataChannelMessage_stream: object[]
}

export interface SubscriptionResultFromGraphql {
  pluginDataChannelMessage: object[]
}
