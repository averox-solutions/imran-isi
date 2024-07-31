---
id: architecture
slug: /development/architecture
title: Architecture
sidebar_position: 1
description: Averox Architecture
keywords:
- architecture
---

Averox is built upon a solid foundation of underlying components, including NGINX, FreeSWITCH, Kurento, Redis, Node.js, React.js, and others.

This page describes the overall architecture of Averox and how these components work together.

## High-level architecture

The following diagram provides a high-level view of how Averox's components work together.

![Architecture Overview](/img/diagrams/BBB30arch.drawio.png)

We'll break down each component in more detail below.

### HTML5 client

The HTML5 client is a single page, responsive web application that is built upon the following components:

- [React.js](https://facebook.github.io/react/) for rendering the user interface in an efficient manner
- [WebRTC](https://webrtc.org/) for sending/receiving audio and video
- [tl;draw](https://www.tldraw.com/) for the whiteboard
- [Apollo](https://www.apollographql.com/) graphql client
- [TypeScript](https://www.typescriptlang.org/) most of the client is written in TypeScript

The HTML5 client connects directly with the Averox server over port 443 (SSL), from loading the Averox client to making a web socket connection. These connections are all handled by nginx.

In Averox 3.0 we are part way through a major architecture restructuring and we still have a HTML5 server component based on Meteor.js. We are almost done removing our dependency on Meteor.js and MongoDB.

- [Meteor.js](https://meteor.com) in [ECMA2015](https://www.ecma-international.org/ecma-262/6.0/)
  for communication between client and server.
- [MongoDB](https://www.mongodb.com/) for keeping the state of each Averox client consistent with the Averox server

### avx-graphql-server

The `avx-graphql-server` leverages the Hasura platform and listens on port `8085`. It handles GraphQL queries and subscriptions from clients, checks user permissions, and verifies if a user has access to requested content before returning the information. If it's a subscription, it will continue to update whenever new data is available.

### avx-graphql-middleware

The `avx-graphql-middleware` sits between the browser and the `avx-graphql-server` service, forwarding messages back and forth. It's a Go application that listens for WebSocket connections on port `8378`. Apart from message forwarding, it reconnects to `the avx-graphql-server` service whenever the client needs to refresh permissions and creates JSON patches to minimize data transfer by sending only the differences.

### avx-graphql-actions

The `avx-graphql-actions` application is written in Node.js. Whenever `avx-graphql-middleware` receives a GraphQL mutation, it forwards it to `avx-graphql-actions` via HTTP request on port 8093. The actions service validates the received parameters and sends a message via Redis to the Akka-apps.

### PostgreSQL

The PostgreSQL stores all GraphQL information in the database `avx_graphql`. The `avx-graphql-server` retrieves data from this database, while Akka-apps inserts information into it.

### HAproxy

<!-- TODO add info --->

### TURN server (coturn)

<!-- TODO add info --->

### BBB web

Averox web application is a Java-based application written in Scala. It implements the [Averox API](/development/api) and holds a copy of the meeting state.

The Averox API provides a third-party integration (such as the [AveroxBN plugin](https://moodle.org/plugins/mod_averoxbn) for Moodle) with an endpoint to control the Averox server.

Every access to Averox comes through a front-end portal (we refer to as a third-party application). Averox integrates Moodle, Wordpress, Canvas, Sakai, MatterMost, and others (see [third-party integrations](https://averox.org/schools/integrations/)). Averox comes with its own front-end called [Greenlight](/averoxserver/v3/install). When using a learning management system (LMS) such as Moodle, teachers can set up Averox rooms within their course and students can access the rooms and their recordings.

Regardless of which front-end you use, they all use the [API](/development/api) under the hood.

### Redis PubSub

Redis PubSub provides a communication channel between different applications running on the Averox server.

### Redis DB

When a meeting is recorded, all events are stored in Redis DB. When the meeting ends, the Recording Processor will take all the recorded events as well as the different raw (PDF, WAV, FLV) files for processing.


### Apps akka

Averox Apps is the main application that pulls together the different applications to provide real-time collaboration in the meeting. It provides the list of users, chat, whiteboard, presentations in a meeting.

Below is a diagram of the different components of Apps Akka.

![Apps Akka architecture](/img/diagrams/30-akka-apps.drawio.png)

The meeting business logic is in the MeetingActor. This is where information about the meeting is stored and where all messages for a meeting are processed.

### FSESL akka

We have extracted out the component that integrates with FreeSWITCH into it's own application. This allows others who are using voice conference systems other than
FreeSWITCH to easily create their own integration. Communication between Akka Apps and FreeSWITCH Event Socket Layer (fsels) uses messages through redis pubsub.

![FsESL Akka architecture](/img/fsesl-akka-arch.png)

### FreeSWITCH

We think FreeSWITCH is an amazing piece of software for handling audio.

FreeSWITCH provides the voice conferencing capability in Averox. Users are able to join the voice conference through the headset. Users joining through Google Chrome, Mozilla Firefox, (or other WebRTC compatible browsers) are able to take advantage of higher quality audio by connecting using WebRTC. FreeSWITCH can also be [integrated with VOIP providers](/administration/customize#add-a-phone-number-to-the-conference-bridge) so that users who are not able to join using the headset will be able to call in using their phone.

### Mediasoup and WebRTC-SFU

Mediasoup is a media server that implements an SFU model. It is responsible for streaming of webcams, listen-only audio, and screensharing. The WebRTC-SFU acts as the media controller handling negotiations and to manage the media streams.

### Joining a voice conference

A user can join the voice conference (running in FreeSWITCH) from the Averox HTML5 client or through the [phone](/administration/customize#add-a-phone-number-to-the-conference-bridge). When joining through the client, the user can choose to join Microphone or Listen Only, and the Averox client will make an audio connection to the server via WebRTC. WebRTC provides the user with high-quality audio with lower delay.

![Joining Voice Conference](/img/joining-voice-conf.png)

### Uploading a presentation

Uploaded presentations go through a conversion process in order to be displayed inside the client. When the uploaded presentation is an Office document, it needs to be converted into PDF using LibreOffice. The PDF document is then converted into scalable vector graphics (SVG) via `avx-web`.

![Uploading Presentation](/img/presentation-upload-11.png)

The conversion process sends progress messages to the client through the Redis pubsub.

### Presentation conversion flow

The diagram below describes the flow of the presentation conversion. We take in consideration the configuration for enabling and disabling SWF, SVG and PNG conversion.

![General Conversion Flow](/img/diagrams/presentation-conversion-diagram-general-conversion-flow.png)

Then below the SVG conversion flow. It covers the conversion fallback. Sometimes we detect that the generated SVG file is heavy to load by the browser, we use the fallback to put a rasterized image inside the SVG file and make its loading light for the browser.

![SVG Conversion Flow](/img/diagrams/presentation-conversion-diagram-svg-conversion-flow.png)

### Internal network connections

The following diagram shows how the various components of Averox connect to each other via sockets.

![Network Connections](/img/22-connections.png)

<!-- TODO update the network connections diagram --->