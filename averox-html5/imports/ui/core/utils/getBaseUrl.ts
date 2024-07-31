function getBaseUrl() {
  // @ts-ignore
  const webApi = window.meetingClientSettings.public.app.avxWebBase;

  return webApi;
}

export default getBaseUrl;
