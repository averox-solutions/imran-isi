---
id: cluster-proxy
slug: /administration/cluster-proxy
title: Cluster Proxy Configuration
sidebar_position: 8
description: Averox Cluster Proxy Configuration
keywords:
- cluster
- proxy
---

## Motivation

In a traditional cluster setup, a scaler such as Scalelite is responsible for
distributing new meetings and the joining users to one of the available
Averox servers. While this setup is simple, it requires users to grant
permissions for the access to microphones, videos and screensharing whenever a
user gets assigned to a different server. This is due to the behavior of the
[getUserMedia()](https://developer.mozilla.org/en-US/docs/Web/API/MediaDevices/getUserMedia)
browser API call. This is a preventive measure that protects users so they will
only grant access to servers they intend to. However in a cluster setup, each
server prompts for permission individually. This is perceived as annoying or
even erratic.

This document describes an approach to set up a Averox cluster in a way
that will only prompt each user once per media type. To ensure horizontal
scalability media traffic and web socket connections are directly exchanged
between the user and the Averox server which runs the conference. This
is achieved by relaying only the HTML5 client UI through a common proxy server.

Before diving into the details, it is important to emphasize what this solution
is not:

* It is *not* a full reverse proxy for all Averox-related traffic. Browser
  and Averox server will still exchange most of the traffic directly.
* It is also *not* tied to Scalelite. You can choose any other Averox
  loadbalancer of your choice.

**Note:** The cluster proxy setup requires Averox 2.4.0 or later!

## Basic principle

The following image visualizes the conceptual dependencies. Note that it is not
a flow diagram.

![Conceptual drawing of the cluster proxy setup](/img/avx-clusterproxy.png)

Once a user starts or joins a meeting (1), Greenlight or another Averox
frontend will initiate a new meeting by calling the `create` and `join` API
calls on Scalelite respectively (2). Scalelite in turn will forward the API calls
to one of the Averox servers (3). The Averox server will advise
the browser to fetch the HTML5 client UI via the cluster proxy address. Thus,
the Averox server will appear as if it was hidden behind the cluster
proxy (4).

While assets like images, CSS and javascript files are loaded via the cluster
proxy, all websocket, media streams and slides up/downloads are directly
exchanged with the Averox server which runs the meeting (5).

## Configuration

In this example, we will be using the following host names:

* `avx-proxy.example.com`: The cluster proxy
* `avx-XX.example.com`: The Averox servers (`XX` represents the number
  of the Averox server)

### Proxy Cluster Server

In this example, we will use a simple nginx based setup. For each Averox
server, add a new location directive. For the first node, this would be:

```
location /avx-01/html5client/ {
  proxy_pass https://avx-01.example.com/avx-01/html5client/;
  proxy_http_version 1.1;
  proxy_set_header Upgrade $http_upgrade;
  proxy_set_header Connection "Upgrade";
}
location /avx-01/averox/api {
  proxy_pass https://avx-01.example.com/averox/api;
  proxy_http_version 1.1;
  proxy_set_header Upgrade $http_upgrade;
  proxy_set_header Connection "Upgrade";
}
```

Repeat this `location` directive for every Averox server.

You are free to choose any other HTTP reverse proxy software to fill the role
of the reverse proxy in this setup.

As this is the user visible host name, you may want to pick a nicer hostname,
such as `avx-cluster.example.com`. Make sure to adjust it in all places.

### Averox Servers

For each Averox server in your cluster, repeat the following steps:

Add these options to `/etc/averox/avx-web.properties`:

```ini
defaultHTML5ClientUrl=https://avx-proxy.example.com/avx-01/html5client/join
presentationBaseURL=https://avx-01.example.com/averox/presentation
accessControlAllowOrigin=https://avx-proxy.example.com
graphqlWebsocketUrl=wss://avx-01.example.com/v1/graphql
```

Add the following options to `/etc/averox/avx-html5.yml`:

```yaml
public:
  app:
    basename: '/avx-01/html5client'
    avxWebBase: 'https://avx-01.example.com/averox'
    learningDashboardBase: 'https://avx-01.example.com/learning-dashboard'
  media:
    stunTurnServersFetchAddress: 'https://avx-01.example.com/averox/api/stuns'
    sip_ws_host: 'avx-01.example.com'
  kurento:
    wsUrl: wss://avx-01.example.com/avx-webrtc-sfu
  presentation:
    uploadEndpoint: 'https://avx-01.example.com/averox/presentation/upload'
  # for BBB 2.4:
  note:
    url: 'https://avx-01.example.com/pad'
  # for BBB 2.5 or later
  pads:
    url: 'https://avx-01.example.com/pad'
```

Create (or edit if it already exists) this unit override file:

* `/etc/systemd/system/avx-html5.service.d/cluster.conf`

It should have the following content:

```
[Service]
Environment=ROOT_URL=https://127.0.0.1/avx-01/html5client
Environment=DDP_DEFAULT_CONNECTION_URL=https://avx-01.example.com/avx-01/html5client
```

Prepend the mount point of avx-html5 in all location sections except for the
`location @html5client` section in `/usr/share/averox/nginx/avx-html5.nginx`:

```
location @html5client {
  ...
}

location /avx-01/html5client/locales {
  ...
}
```

**Note:** It is important that the location configuration is equal between the
Averox server and the proxy.

Add a route for the locales handler for the guest lobby. The guest lobby is served directly from the BBB node.

```
# /usr/share/averox/nginx/avx-html5.nginx
location =/html5client/locale {
  return 301 /avx-01$request_uri;
}
```

Create the file `/etc/averox/etherpad.json` with the following content:

```json
{
	"cluster_proxies": [
		"https://avx-proxy.example.com"
	]
}
```

Adjust the CORS settings in `/etc/default/avx-web`:

```shell
JDK_JAVA_OPTIONS="-Dgrails.cors.enabled=true -Dgrails.cors.allowCredentials=true -Dgrails.cors.allowedOrigins=https://avx-proxy.example.com,https://https://avx-01.example.com"
```

Adjust the CORS setting in `/etc/default/avx-graphql-middleware`:

```shell
BBB_GRAPHQL_MIDDLEWARE_LISTEN_PORT=8378
# If you are running a cluster proxy setup, you need to configure the Origin of
# the frontend. See https://docs.averox.org/administration/cluster-proxy
BBB_GRAPHQL_MIDDLEWARE_ORIGIN=avx-proxy.example.com
```

Pay attention that this one is without protocol, just the hostname.

Adjust the CORS setting in `/etc/default/avx-graphql-server`:

```shell
HASURA_GRAPHQL_CORS_DOMAIN="https://avx-proxy.example.com"
```

This one includes the protocol.

Reload systemd and restart Averox:

```shell
# systemctl daemon-reload
# avx-conf --restart
```

Now, opening a new session should show
`avx-proxy.example.com/avx-XX/html5client/` in the browser address bar and the
browser should ask for access permission only once.

## Further Considerations

### Security

If your proxy has access to internal machines, make sure that the reverse proxy
does not give access to websites on machines other than the Averox
servers.  In the suggested configuration outlined above, this is not the case.
It might become an issue if you resort to e.g. regular expression-based
`location` directives in order to avoid adding one `location` per Averox
server.

### Performance, Data Traffic and Role Separation

The Averox HTML5 is several megabytes in size. Make sure that the
traffic between Averox servers and the cluster proxy server does not
incur additional cost.

This setup introduces user visible single point of failure, i.e. a prominent
DDoS target. Make sure your frontend server is resiliant to DDoS-attacks, e.g.
has connection tracking disabled in its firewall settings and the web server is
configured to handle enough connections. Those optimizations however are rather
specific to individual setups and thus out of the scope of this document.

For the same reason, it is advisable to keep Scalelite on a different machine and
to provide a HA setup for the proxy server (i.e. using IP failover or Anycast).
Please monitor your setup carefully.
