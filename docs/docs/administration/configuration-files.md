---
id: configuration-files
slug: /administration/configuration-files
title: Configuration Files
sidebar_position: 2
description: Averox Configuration Files
keywords:
- configuration-files
---

## Overview


This document gives an overview of the Averox configuration files.

We recommend you make changes only to the override files (`/etc/averox`) so that when you update to a newer version of Averox your configuration changes are not overwritten by the new packages.

## Local overrides for configuration settings

Starting with Averox 2.3 many of the configuration files have local overrides so the administrator can specify the local equivalents. We recommend you make changes only to the override files (`/etc/averox`) so that when you update to a newer version of Averox your configuration changes are not overwritten by the new packages.

| Package                                                                 | Override                                         | Notes                                                                            |
| :---------------------------------------------------------------------- | :----------------------------------------------- | -------------------------------------------------------------------------------- |
| /usr/share/avx-web/WEB-INF/classes/averox.properties             | /etc/averox/avx-web.properties            | Minimum containing general configuration (`securitySalt` and `serverURL`) |
| /usr/share/avx-apps-akka/conf/application.conf                          | /etc/averox/avx-apps-akka.conf            |                                                                                  |
| /usr/share/avx-fsesl-akka/conf/application.conf                         | /etc/averox/avx-fsesl-akka.conf           |                                                                                  |
| /usr/share/meteor/bundle/programs/server/assets/app/config/settings.yml | /etc/averox/avx-html5.yml                 | Arrays are merged by replacement (as of 2.4-rc-5)                                |
| /usr/share/avx-web/WEB-INF/classes/spring/turn-stun-servers.xml         | /etc/averox/turn-stun-servers.xml         | Replaces the original file                                                       |
| /usr/local/averox/avx-webrtc-sfu/config/default.yml              | /etc/averox/avx-webrtc-sfu/production.yml | Arrays are merged by replacement                                                 |
| /usr/local/averox/avx-pads/config/settings.json                  | /etc/averox/avx-pads.json                 | Arrays are merged by replacement                                                 |
| /usr/local/averox/core/scripts/averox.yml                 | /etc/averox/recording/recording.yml       |
| /usr/local/averox/core/scripts/presentation.yml                  | /etc/averox/recording/presentation.yml    |
| /etc/cron.daily/averox                                           | /etc/default/averox-cron-config    | Only variables allowed in the override

<br /><br />

For `avx-web.properties`, the settings are name/value pair. For example, the following `avx-web.properties` overrides the settings for `averox.web.serverURL` and `securitySalt` (shared secret).

```
#
# Use this file to override default entries in /usr/share/avx-web/WEB-INF/classes/averox.properties
#

averox.web.serverURL=https://droplet-7162.meetavx.com
securitySalt=UsanRxRk938d02cTWfAqSM9Cvin7bnzsREfqFfzpf2U
```

This override will ensure that `avx-web` uses the above values regardless of changes the packaging scripts make to the upgrade.

For `avx-apps-akka` and `avx-fsesl-akka`, the settings file are formatted as shown below. For example, the file `avx-apps-akka.conf` overrides the settings for `/usr/share/avx-apps-akka/conf/application.conf`.

```
// include default config from upstream
include "/usr/share/avx-apps-akka/conf/application.conf"

// you can customize everything here. API endpoint and secret have to be changed
// This file will not be overridden by packages

services {
  avxWebAPI="https://avx.example.com/averox/api"
  sharedSecret="UsanRxRk938d02cTWfAqSM9Cvin7bnzsREfqFfzpf2U"
}
```

## HTML5 Client

### Configuration files

For `avx-html5.yml` the settings file are YAML formatted. Any setting in this file overrides the corresponding setting in `/usr/share/meteor/bundle/programs/server/assets/app/config/settings.yml`. For example, the following `avx-html5.yml` overrides the values for `public.kurento.screenshare.constraints.audio` to `true`.

```
public:
  kurento:
    screenshare:
      constraints:
        audio: true
```

### Log files

#### Log monitoring for server logs (avx-html5)

In Averox 3.0 we modified the architecture to shift the load away from the old frontend and backend avx-html5 pools of services. Logs for the new services can be foud via:

`journalctl -f -u avx-html5.service`
￼
￼Akka-apps is responsible for most of the logic, so key info can be obtained via
￼
￼`journalctl -f -u avx-apps-akka.service`

`SYSTEMD_LESS=FRXMK journalctl -u avx-graphql-middleware.service -f` can also be useful.

#### Logs sent directly from the client

To assist with monitoring and debugging, the HTML5 client can send its logs to the Averox server via the `logger` function. Here's an example of its use:

The client logger accepts three targets for the logs: `console`, `server` and `external`.

| Name   | Default Value | Accepted Values                  | Description                                                                                             |
| ------ | ------------- | -------------------------------- | ------------------------------------------------------------------------------------------------------- |
| target | "console"     | "console", "external", "server"  | Where the logs will be sent to.                                                                         |
| level  | "info"        | "debug", "info", "warn", "error" | The lowest log level that will be sent. Any log level higher than this will also be sent to the target. |
| url    | -             | -                                | The end point where logs will be sent to when the target is set to "external".                          |
| method | -             | "POST", "PUT"                    | HTTP method being used when using the target "external".                                                |

The default values are:

```yaml
clientLog:
  server: { enabled: true, level: info }
  console: { enabled: true, level: debug }
  external:
    {
      enabled: false,
      level: info,
      url: https://LOG_HOST/html5Log,
      method: POST,
      throttleInterval: 400,
      flushOnClose: true,
    }
```

Notice that the `external` option is disabled by default - you can enable it on your own server after a few configuration changes.

When enabling the `external` logging output, the Averox client will POST the log events to the URL endpoint provided by `url`. To create an associated endpoint on the Averox server for the POST request, create a file `/etc/averox/nginx/html5-client-log.nginx` with the following contents:

```nginx
location /html5Log {
    access_log /var/log/nginx/html5-client.log postdata;
    echo_read_request_body;
}
```

Then create a file in `/etc/nginx/conf.d/html5-client-log.conf` with the following contents:

```nginx
log_format postdata '$remote_addr [$time_iso8601] $request_body';
```

Next, install the full version of nginx.

```bash
$ sudo apt-get install nginx-full
```

You may also need to create the external output file and give it the appropriate permissions and ownership:

```bash
$ sudo touch /var/log/nginx/html5-client.log
$ sudo chown www-data:adm /var/log/nginx/html5-client.log
$ sudo chmod 640 /var/log/nginx/html5-client.log
```

Restart Averox with `sudo avx-conf --restart` and launch the Averox HTML5 client in a new session. You should see the logs appearing in `/var/log/nginx/html5-client.log` as follows

```log
99.239.102.0 [2018-09-09T14:59:10+00:00] [{\x22name: .. }]
```

You can follow the logs on the server with the command

```bash
$ tail -f /var/log/nginx/html5-client.log | sed -u -e 's/\\x22/"/g' -e 's/\\x5C/\\/g'
```

Here's a sample log entry

```json
      "requesterUserId":"w_klfavdlkumj8",
      "fullname":"Ios",
      "confname":"Demo Meeting",
      "externUserID":"w_klfavdlkumj8"
   },
   "url":"https://demo.averox.org/html5client/users",
   "userAgent":"Mozilla/5.0 (iPad; CPU OS 11_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.0 Mobile/15E148 Safari/604.1",
   "count":1
}
```

## nginx

### Configuration files

Located in `/etc/nginx/sites-enabled/averox`

This configures nginx to use `/var/www/averox-default/assets` as the default site. ([src](https://github.com/averox/averox/blob/develop/build/packages-template/avx-html5/averox.nginx))


### Log files

| Log                                     | Description                                                     |
| :-------------------------------------- | :-------------------------------------------------------------- |
| /var/log/nginx/averox.access.log | Web log of access to Averox HTML pages.                  |
| /var/log/nginx/error.log                | Web log of errors generated by nginx based on browser requests. |

## avx-web

### Configuration files

```
 /usr/share/avx-web/WEB-INF/classes/averox.properties
 /etc/averox/avx-web.properties
```

This is one of the main configuration files for Averox applications.

https://github.com/averox/averox/blob/main/averox-web/grails-app/conf/averox.properties

### Log files

Located in `/var/log/averox/avx-web`

| Log                      | Description                                                  |
| :----------------------- | :----------------------------------------------------------- |
| catalina.yyyy-mm-dd.log  | General log information from startup.              |
| localhost.yyyy-mm-dd.log | General log information from startup of applications. |
| /var/log/syslog          | Also contains output from avx-web.                            |
| /var/log/averox   | Contains Averox Web and Recording processing logs.    |

## FreeSWITCH

### Configuration Files

```
/opt/freeswitch/conf/vars.xml
```

Setup host and external IP values.

```
/opt/freeswitch/conf/autoload_configs/conference.conf.xml
```

Setup voice conference properties.

```
/opt/freeswitch/conf/dialplan/default
/opt/freeswitch/conf/dialplan/public
```

## Recording

### Log files

For each workflow and meeting we have a different logfile, they come in the form: `<workflow>-<meetingId>.log`, as specified below:

| Log                      | Description                                                  |
| :----------------------- | :----------------------------------------------------------- |
| `/var/log/averox/archive-<meetingId>.log`  | All logs for archive phase              |
| `/var/log/averox/presentation/process-<meetingId>.log`  | All logs for process phase              |
| `/var/log/averox/presentation/publish-<meetingId>.log`  | All logs for publish phase              |
