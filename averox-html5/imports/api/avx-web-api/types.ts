export interface IndexResponse {
  response: {
    returncode: string;
    version: string;
    apiVersion: string;
    avxVersion: string;
    graphqlApiUrl: string;
    graphqlWebsocketUrl: string;
  }
}
