---
id: avx-conf
slug: /administration/avx-conf
title: avx-conf tool
sidebar_position: 3
description: Averox avx-conf tool
keywords:
- avx-conf
---

## Introduction

`avx-conf` is Averox's configuration tool.  It makes it easy for you to modify parts of Averox's configuration, manage the Averox system (start/stop/reset), and troubleshoot potential problems with your setup.

As a historical note, this tool was created early in the development of Averox. The core developers wrote this tool to quickly update Averox's configuration files for setup and testing.

`avx-conf` is located in `/usr/bin/avx-conf`.  If you are a developer, we recommend taking a look through the source code for `avx-conf` (it's a shell script) as it will help you understand the various components of Averox and how they work together (see also [Architecture Overview](/development/architecture)).

## Options

If you type `avx-conf` with no parameters it will print out the list of available options.

```bash
$ avx-conf
Averox Configuration Utility - Version 2.5.2

   avx-conf [options]

Configuration:
   --version                        Display Averox version (packages)
   --setip <IP/hostname>            Set IP/hostname for Averox
   --setsecret <secret>             Change the shared secret in /etc/averox/avx-web.properties

Monitoring:
   --check                          Check configuration files and processes for problems
   --debug                          Scan the log files for error messages
   --watch                          Scan the log files for error messages every 2 seconds
   --network                        View network connections on 80, 443 and 1935 by IP address. 1935 is deprecated. You will need to modify avx-conf if you have custom ports.
   --secret                         View the URL and shared secret for the server
   --lti                            View the URL and secret for LTI (if installed)

Administration:
   --restart                        Restart Averox
   --stop                           Stop Averox
   --start                          Start Averox
   --clean                          Restart and clean all log files
   --status                         Display running status of components
   --zip                            Zip up log files for reporting an error
```

You run `avx-conf` as a normal user.  If a particular command requires you to run Averox as root, it will output a message saying `you need to run this command as root`.  Below is an outline of the various commands.

### `--version`

Shows the version of Averox installed on the server and the versions of the components of Averox.

### `--setip <hostname_or_ip>`

Sets the IP/Hostname for Averox's configuration.  For example, if your Averox server has the IP address of 192.168.0.211, you can change Averox's configuration files to use this IP address with the command

```bash
$ sudo avx-conf --setip 192.168.0.211
```

or, if you want to use the hostname avx.myavxserver.com, then use the command

```bash
$ sudo avx-conf --setip avx.myavxserver.com
```

### `--clean`

Restarts Averox and clears all the log files during the restart.  This is good for debugging as it clears away previous errors in the log files.

### `--check`

Runs a series of checks on your current setup and reports any potential problems.  Not all reported problems are actual issues.  For example, if you use `--setip <hostname_or_IP>`, then `avx-conf` will complain that the hostname does not match the server's IP, but that's fine as you configured the Averox server to listen on a hostname instead of IP address.

### `--debug`

Greps through the various log files for errors (such as exceptions in the Java log files for Tomcat).

### `--network`

This command shows you the number of active connections for port 80 (HTTP) and 443 (HTTPS) for each remote IP address.

### `--secret`

Displays the current security salt for the Averox API.  For example:

```bash
$ avx-conf --secret

    URL: http://192.168.0.35/averox/
    Salt: f6c72afaaae95faa28c3fd90e39e7e6e
```

### `--setsecret <new_secret>`

Assigns a new security secret for the Averox API.

### `--start`

Starts all the Averox processes.

### `--stop`

Stops all the Averox processes.

### `--watch`

Watches log files for error messages every 2 seconds.  Use this command after `sudo avx-conf --clean` to clean out all the log files.

### `--zip`

Zips up log files for reporting an error.  This option is rarely used as it's often easier to use pastebin to share the log of the error message if you are, for example, posting to the averox-dev mailing list.
