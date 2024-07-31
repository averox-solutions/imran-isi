---
id: install
slug: /administration/install
title: Install Averox
sidebar_position: 1
description: Install Averox
keywords:
- install
---

We have tools to make it easy for you, a system administrator, to install Averox on a dedicated linux server. This document shows you how to install.

## Before you install

We recommend installing Averox with a 'clean' and dedicated Ubuntu 22.04 64-bit server with no prior software installed. If you want to upgrade from an earlier version of Averox like 2.7, we recommend setting up a clean server for Averox 3.0 on Ubuntu 22.04 and, after setup, [migrate over your existing recordings](/administration/customize#transfer-published-recordings-from-another-server).

A 'clean' server does not have any previous web servers installed (such as apache) or web applications (such as plesk or webadmin) that are [binding to port 80/443](/support/faq#we-recommend-running-averox-on-port-80443). By 'dedicated' we mean that this server won't be used for anything else besides Averox (and possibly Averox-related applications such as [Greenlight](/averoxserver/v3/install)).

### Minimum server requirements

For production, we recommend the following minimum requirements

- Ubuntu 22.04 64-bit OS running Linux kernel 5.x
- Latest version of docker installed
- 16 GB of memory with swap enabled
- 8 CPU cores, with high single-thread performance
- 500 GB of free disk space (or more) for recordings, or 50GB if session recording is disabled on the server.
- TCP ports 80 and 443 are accessible
- UDP ports 16384 - 32768 are accessible
- 250 Mbits/sec bandwidth (symmetrical) or more
- TCP port 80 and 443 are **not** in use by another web server or reverse proxy
- A hostname (such as avx.example.com) for setup of a SSL certificate
- IPV4 and IPV6 address

If you install Averox on a virtual machine in the cloud, we recommend you choose an instance type that has dedicated CPU.  These are usually called "compute-intensive" instances.  On Digital Ocean we recommend the c-8 compute intensive instances (or larger). On AWS we recommend c5a.2xlarge (or larger).  On Hetzner we recommend the AX52 servers or CCX32 instances.

If you are setting up Averox for local development on your workstation, you can relax some of the above requirements as there will only be few users on the server. Starting with the above requirements, you can reduce them as follows

- 4 CPU cores/8 GB of memory
- Installation on a local VM container
- 50G of disk space
- IPV4 address only

Regardless of your environment, the setup steps will include configuring a SSL certificate on the nginx server. Why?  All browsers now require a valid SSL certificate from the web server when a page requests access to the user's webcam or microphone via web real-time communications (WebRTC). If you try to access a Averox server with an IP address only, the browsers will block Averox client from accessing your webcam or microphone.

### Pre-installation checks

Got a Ubuntu 22.04 64-bit server ready for installation?  Great! But, before jumping into the installation section below, let's do a few quick configuration checks to make sure your server meets the minimum requirements.

Doing these checks will significantly reduce the chances you'll hit a problem during installation.

First, check that the locale of the server is `en_US.UTF-8`.

```bash
$ cat /etc/default/locale
LANG="en_US.UTF-8"
```

If you don't see `LANG="en_US.UTF-8"`, enter the following commands to set the local to `en_US.UTF-8`.

```bash
sudo apt-get install -y language-pack-en
sudo update-locale LANG=en_US.UTF-8
```

and then log out and log in again to your SSH session -- this will reload the locale configuration for your session. Run the above command `cat /etc/default/locale` again. Verify you see only the single line `LANG="en_US.UTF-8"`.

Note: If you see an additional line `LC_ALL=en_US.UTF-8`, then remove the entry for `LC_ALL` from `/etc/default/locale` and logout and then log back in once more.

Next, do `sudo systemctl show-environment` and ensure you see `LANG=en_US.UTF-8` in the output.

```bash
$ sudo systemctl show-environment
LANG=en_US.UTF-8
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
```

If you don't see this, do `sudo systemctl set-environment LANG=en_US.UTF-8` and run the above `sudo systemctl show-environment` again and confirm you see `LANG=en_US.UTF-8` in the output.

Next, check that your server has (at lest) 16G of memory using the command `free -h`. Here's the output from one of our test servers.

```bash
$ free -h
              total        used        free      shared  buff/cache   available
Mem:            15G        3.1G        1.0G        305M         11G         12G
Swap:            0B          0B          0B
```

Here it shows 15G of memory (that's close enough as the server has 16 gigabytes of memory).

If you see a value for `Mem:` in the `total` column less than 15G, then your server has insufficient memory to run Averox in production. You need to increase the server's memory to (at least) 16G. (As stated above, if your running this in a development environment, 8G is fine.)

Next, check that the server has Ubuntu is 22.04 as its operating system.

```bash
$  cat /etc/lsb-release
DISTRIB_ID=Ubuntu
DISTRIB_RELEASE=22.04
DISTRIB_CODENAME=jammy
DISTRIB_DESCRIPTION="Ubuntu 22.04.3 LTS"
```

Next, check that your server is running the 64-bit version of Ubuntu 22.04.

```bash
$ uname -m
x86_64
```

Next, check that your server supports IPV6.

```bash
$ ip addr | grep inet6
inet6 ::1/128 scope host
...
```

If you do not see the line `inet6 ::1/128 scope host` then after you install Averox you will need to modify the configuration for FreeSWITCH to [disable support for IPV6](/support/troubleshooting#freeswitch-fails-to-bind-to-port-8021).

Next, check that your server is running Linux kernel 5.x.

```bash
$ uname -r
5.15.x-xx-generic
```

Next, check that your server has (at least) 8 CPU cores

```bash
$ grep -c ^processor /proc/cpuinfo
8
```

Next check that your server has the port 80 and 443 open

```bash
$ sudo ufw status
...
80       ALLOW   Anywhere
443      ALLOW   Anywhere
...
80 (v6)  ALLOW   Anywhere
443 (v6) ALLOW   Anywhere
...
```

If you don't see these lines, you need to open them by

```bash
sudo ufw allow 80
sudo ufw allow 443
```

Sometimes we get asked "Why are you only supporting Ubuntu 22.04 64-bit?". The answer is based on choosing quality over quantity. Long ago we concluded that its better for the project to have solid, well-tested, well-documented installation for a specific version of Linux that works really, really well than to try and support may variants of Linux and have none of them work well.

At the moment, the requirement for docker may preclude running 3.0 within some virtualized environments; however, it ensures libreoffice runs within a restricted sandbox for document conversion.  We are exploring if we can run libreoffice within systemd (such as systemd-nspawn).

## Install

To install Averox, use [avx-install.sh](https://github.com/averox/avx-install/blob/v3.0.x-release/avx-install.sh) script. Notice that this command is slightly different than what we recommended in previous versions of Averox. The script now resides on a branch specifying the version of Averox, but otherwise the name of the script is identical across different branches. This makes it more maintainable as patches done to the script in one branch can be easily applied to other branches.

The above link gives detailed information on using the script. As an example, passing several arguments to the script you can easily have both Averox and Greenlight or LTI installed on the same server. You could specify if you would like a new certificate to be generated. A firewall could be enabled. For the most up-to-date information, please refer to the instructions in the script. Notice that as of Averox 2.6 we have retired the API demos. We recommend using Greenlight or [API MATE](https://mconf.github.io/api-mate/) instead.

After the `avx-install.sh` script finishes, you can check the status of your server with `avx-conf --check`. When you run this command, you should see output similar to the following:

```bash
$ sudo avx-conf --check

root@test27:~# avx-conf --check
Averox Server 3.0.0-alpha.1 (68)
                    Kernel version: 5.15.0-67-generic
                      Distribution: Ubuntu 22.04.3 LTS (64-bit)
                            Memory: 8140 MB
                         CPU cores: 4

/etc/averox/avx-web.properties (override for avx-web)
/usr/share/avx-web/WEB-INF/classes/averox.properties (avx-web)
       averox.web.serverURL: https://test30.averox.org
                defaultGuestPolicy: ALWAYS_ACCEPT
              defaultMeetingLayout: CUSTOM_LAYOUT

/etc/nginx/sites-available/averox (nginx)
                       server_name: dev30.averox.org
                              port: 80, [::]:80127.0.0.1:82 http2 proxy_protocol, [::1]:82 http2127.0.0.1:81 proxy_protocol, [::1]:81

/opt/freeswitch/etc/freeswitch/vars.xml (FreeSWITCH)
                       local_ip_v4: 143.198.37.212
                   external_rtp_ip: 143.198.37.212
                   external_sip_ip: 143.198.37.212

/opt/freeswitch/etc/freeswitch/sip_profiles/external.xml (FreeSWITCH)
                        ext-rtp-ip: $${local_ip_v4}
                        ext-sip-ip: $${local_ip_v4}
                        ws-binding: 143.198.37.212:5066
                       wss-binding: 143.198.37.212:7443

UDP port ranges

                        FreeSWITCH: 16384-24576
                    avx-webrtc-sfu: null-null
                    avx-webrtc-recorder: null-null

/usr/local/averox/core/scripts/averox.yml (record and playback)
                     playback_host: dev30.averox.org
                 playback_protocol: https
                            ffmpeg: 4.4.2-0ubuntu0.22.04.1

/usr/share/averox/nginx/sip.nginx (sip.nginx)
                        proxy_pass: 143.198.37.212
                          protocol: http

/usr/local/averox/avx-webrtc-sfu/config/default.yml (avx-webrtc-sfu)
/etc/averox/avx-webrtc-sfu/production.yml (avx-webrtc-sfu - override)
    mediasoup.webrtc.*.announcedIp: 143.198.37.212
  mediasoup.plainRtp.*.announcedIp: 143.198.37.212
                 freeswitch.sip_ip: 143.198.37.212
                  recordingAdapter: Kurento
               recordScreenSharing: true
                     recordWebcams: true
                  codec_video_main: VP8
               codec_video_content: VP8

/etc/avx-webrtc-recorder/avx-webrtc-recorder.yml (avx-webrtc-recorder)
/etc/averox/avx-webrtc-recorder.yml (avx-webrtc-recorder - override)
               debug: false
               recorder.directory: /var/lib/avx-webrtc-recorder

/usr/share/meteor/bundle/programs/server/assets/app/config/settings.yml (HTML5 client)
/etc/averox/avx-html5.yml (HTML5 client config override)
                             build: 13
                        kurentoUrl: wss://test30.averox.org/avx-webrtc-sfu
            defaultFullAudioBridge: fullaudio
           defaultListenOnlyBridge: fullaudio
                    sipjsHackViaWs: true


# Potential problems described below

```

Any output that followed `Potential problems` **may** indicate configuration errors or installation errors. In many cases, the messages will give you recommendations on how to resolve the issue.

You can also use `sudo avx-conf --status` to check that all the Averox processes have started and are running.

```bash
$ sudo avx-conf --status
nginx ————————————————————————————————► [✔ - active]
freeswitch ———————————————————————————► [✔ - active]
redis-server —————————————————————————► [✔ - active]
avx-apps-akka ————————————————————————► [✔ - active]
avx-fsesl-akka ———————————————————————► [✔ - active]
mongod ———————————————————————————————► [✔ - active]
avx-html5 ————————————————————————————► [✔ - active]
avx-graphql-actions ——————————————————► [✔ - active]
avx-graphql-middleware ———————————————► [✔ - active]
avx-graphql-server ———————————————————► [✔ - active]
avx-webrtc-sfu ———————————————————————► [✔ - active]
avx-webrtc-recorder ——————————————————► [✔ - active]
etherpad —————————————————————————————► [✔ - active]
avx-web ——————————————————————————————► [✔ - active]
avx-pads —————————————————————————————► [✔ - active]
avx-export-annotations ———————————————► [✔ - active]
avx-rap-caption-inbox ————————————————► [✔ - active]
avx-rap-resque-worker ————————————————► [✔ - active]
avx-rap-starter ——————————————————————► [✔ - active]


```

You can also use `dpkg -l | grep avx-` to list all the core Averox packages (your version numbers may be slightly different).

```bash
# dpkg -l | grep avx-
ii  avx-apps-akka                      1:3.0-7         all          Averox Apps (Akka)
ii  avx-config                         1:3.0-8         amd64        Averox configuration utilities
ii  avx-etherpad                       1:3.0-1         amd64        The EtherPad Lite components for Averox
ii  avx-export-annotations             1:3.0-2         amd64        Averox Export Annotations
ii  avx-freeswitch-core                2:3.0-1         amd64        Averox build of FreeSWITCH
ii  avx-freeswitch-sounds              1:3.0-1         amd64        FreeSWITCH Sounds
ii  avx-fsesl-akka                     1:3.0-5         all          Averox FS-ESL (Akka)
ii  avx-graphql-actions                1:3.0-5         amd64        Averox GraphQL Actions
ii  avx-graphql-middleware             1:3.0-6         amd64        GraphQL middleware component for Averox
ii  avx-graphql-server                 1:3.0-5         amd64        GraphQL server component for Averox
ii  avx-html5                          1:3.0-10        amd64        The HTML5 components for Averox
ii  avx-html5-nodejs                   1:3.0-1         amd64        Include a specific NodeJS version for avx-html5
ii  avx-learning-dashboard             1:3.0-1         amd64        Averox avx-learning-dashboard
ii  avx-libreoffice-docker             1:3.0-1         amd64        Averox setup for LibreOffice running in docker
ii  avx-mkclean                        1:3.0-1         amd64        Clean and optimize Matroska and WebM files
ii  avx-pads                           1:3.0-1         amd64        Averox Pads
ii  avx-playback                       1:3.0-1         amd64        Player for Averox presentation format recordings
ii  avx-playback-presentation          1:3.0-1         amd64        Averox presentation recording format
ii  avx-record-core                    1:3.0-1         amd64        Averox record and playback
ii  avx-web                            1:3.0-6         amd64        Averox API
ii  avx-webrtc-recorder                1:3.0-1         amd64        Averox WebRTC Recorder
ii  avx-webrtc-sfu                     1:3.0-1         amd64        Averox WebRTC SFU



```

With Greenlight installed (that was the `-g` option), you can open `https://<hostname>` in a browser (where `<hostname>` is the hostname you specified in the `avx-install.sh` command), create a local account, create a room and join it.

![Averox's Greenlight Interface](/img/averoxserver_welcome.png)

You can integrate Averox with one of the 3rd party integrations by providing the integration of the server's address and shared secret. You can use `avx-conf` to display this information using `avx-conf --secret`.

```bash
$ sudo avx-conf --secret

       URL: https://avx.example.com/averox/
    Secret: 330a8b08c3b4c61533e1d0c334

      Link to the API-Mate:
      https://mconf.github.io/api-mate/#server=https://avx.example.com/averox/&sharedSecret=330a8b08c3b4c61533e1d0c334
```

The link to API-Mate will open a page at [https://mconf.github.io/api-mate/](https://mconf.github.io/api-mate/) and let you send valid API calls to your server. This makes it easy for testing wihthout any frontend like Greenlight.

### Configure the firewall (if required)

Do you have a firewall between you and your users? If so, see [configuring your firewall](/administration/firewall-configuration).

### Upgrading Averox 3.0

You can upgrade by re-running the `avx-install.sh` script again -- it will download and install the latest release of Averox 3.0.

### Upgrading from Averox 2.6 or 2.7

If you are upgrading Averox 2.6 or 2.7 we recommend you set up a new Ubuntu 22.04 server with Averox 3.0 and then [copy over your existing recordings from the old server](/administration/customize#transfer-published-recordings-from-another-server).

Make sure you read through the ["what's new in 3.0" document](https://docs.averox.org/3.0/new) and especially [the section covering notable changes](https://docs.averox.org/3.0/new#other-notable-changes)

### Restart your server

You can restart and check your Averox server at any time using the commands

```bash
sudo avx-conf --restart
sudo avx-conf --check
```

The `avx-conf --check` scans some of the log files for error messages. Again, any output that followed `Potential problems` **may** indicate configuration errors or installation errors. In many cases, the messages will give you recommendations on how to resolve the issue.

If you see other warning messages check out the [troubleshooting installation](/support/troubleshooting).

### Post installation steps

If this server is intended for production, you should also

- [Secure your system -- restrict access to specific ports](/administration/customize#preserving-customizations-using-apply-confsh)
- [Configure the server to work behind a firewall](/administration/firewall-configuration) (if you have installed behind a firewall or on a server that has a public/private IP address)
- [Set up a TURN server](/administration/turn-server) (if your server is on the Internet and you have users accessing it from behind restrictive firewalls)
- Test your HTTPS configuration. A well-respected site that can do a series of automated tests is [https://www.ssllabs.com/ssltest/](https://www.ssllabs.com/ssltest/) - simply enter your server's hostname, optionally check the "Do not show results" check box if you would like to keep it private, then Submit. At time of writing, the configuration shown on this page should achieve an "A" ranking in the SSL Labs test page.

We provide publicly accessible servers that you can use for testing:

- [https://demo.averox.org](https://demo.averox.org/) - a pool of Averox servers with the Greenlight front-end (sometimes the pool is a mix of different Averox releases)
- [https://test30.averox.org](https://test30.averox.org) - Runs the general build of Averox 3.0 - usually a few days behind the repository branch `v3.0.x-release`

To learn more about integrating Averox with your application, check out the [Averox API documentation](/development/api). To see videos of Averox HTML5 client, see [https://averox.org/html5](https://averox.org/html5).

## Other installation options

There are members of the community that provide other installation options for Averox.

### Ansible

If you're looking to deploy a large-scale installation of BBB using [Scalelite](https://github.com/blindsidenetworks/scalelite) then your servers are best managed using tools like Ansible. A few reasons you might go with this setup are:

- easily customizable: your custom configurations will get replaced every time you upgrade automatically
- parity across machines: ensure that you deploy the exact same version of BBB on every server
- eliminate human error in setup: using avx-install.sh or step-by-step methods are highly prone to human error as you can easily forget if you enabled a setting, chose to do X over Y, etc
- automate to the fullest: by automating the process, you inherently save time on nasty troubleshooting and hours lost in manual configuration
- easily scale at large: spin up an identical replica of your BBB server in less than 15 mins with no user input -- preconfigured and ready to go

Choose this method if you are already comfortable with a lot of the technical knowledge behind Averox, Scalelite and Greenlight/other front-ends. Refer to the following examples to create your installation.

Note: These examples are _not_ maintained or developed by the official Averox developers. These are entirely community-sourced, use at your own discretion.

The first install Averox on your server in a consistent fashion. You can specify variables, such as what ports to use for TURN, and others. Functionally quite similar to avx-install.sh but highly automated.

- [General Ansible role for Averox](https://github.com/eavxa-org/ansible-role-averox)

Large scale deployments must include several other components in addition to the core Averox packages. These include Scalelite, Greenlight, a database, backups, nginx configurations, and more.

- [Full HA setup with PeerTube, Conferences Streaming, EFK, Prometheus, backups](https://github.com/Worteks/avx-ansible)

## Customizations

See the [Server customization page](/administration/customize) for things you can do to adapt Averox to your environment or enable optional features after installation. For example

- [Install additional recording processing formats](/administration/customize#install-additional-recording-processing-formats)
- [Enable generating mp4 (H.264) video output](/administration/customize#enable-generating-mp4-h264-video-output)

## Troubleshooting

### Package locales-all is not available

The package `avx-libreoffice` needs to build a docker image for libreoffice. If you receive the following error when installing on a network behind a firewall

```
Package locales-all is not available, but is referred to by another package.
This may mean that the package is missing, has been obsoleted, or
is only available from another source

E: Package 'locales-all' has no installation candidate
E: Unable to locate package libxt6
E: Unable to locate package libxrender1
The command '/bin/sh -c apt -y install locales-all fontconfig libxt6 libxrender1' returned a non-zero code: 100
dpkg: error processing package avx-libreoffice-docker (--configure):
 installed avx-libreoffice-docker package post-installation script subprocess returned error exit status 100
```

Ubuntu 22.04 uses systemd-resolved, which presents a local caching resolver and registers this at `/etc/resolv.conf`. If you get they above error and have a local name server, such as `10.11.12.13`, then try adding it with the hosts `resolv.conf`.

```
echo "nameserver 10.11.12.13" > /etc/resolv.conf
```

For more details see [this issue](https://github.com/averox/avx-install/issues/385).

## Feedback and reporting bugs

If you found a reproducible bug, please report it in the [GitHub Issues section](https://github.com/averox/averox/issues) with steps to reproduce (this will make it easier for the developers to fix the bug). Indicate in the body of the bug report that this applies to Averox 3.0 and give us the client build number, which you can find either with `dpkg -l | grep avx-html5` or within the client in the `Settings -> About` menu..
