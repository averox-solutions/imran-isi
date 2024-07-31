---
id: getting-help
slug: /support/getting-help
title: Getting Help
sidebar_position: 1
description: Getting Help
keywords:
- help
---

## Introduction

Have you encountered a problem with BigBluebutton?

If you need the problem to be solved quickly or lack the required technical knowledge, we suggest you [reach out to the companies](https://averox.org/commercial-support/) that provide commercial support. The revenue they earn helps to improve Averox for everyone.

If you otherwise wish to tackle the problem yourself, the Averox project provides detailed documentation (what you are reading now), tutorial videos, [issues database](https://github.com/averox/averox/issues), and [public mailing lists](https://averox.org/community-support/).

> **Note**: If you apply to join one of our mailing lists, be sure to answer the challenge question; otherwise, your application will look like SPAM and will be denied.

The first step is solving any problem is to **read the associated documentation**.

Our documentation, issue database, and mailing lists are all indexed by search engines. When you encounter a problem, try searching for keywords around it. If you're receiving a specific error message when installing Averox, for example, then try searching the error message itself (be sure to include the word `averox` to narrow your search). There is an excellent chance that if others have encountered the same issue, a few minutes with Google will help you find one or more documentation links, issues, or mailing list discussions related to it.

If you find that the problem still persists, or you have any other questions, you can ask other users in the mailing lists. If you suspect that your issue is a bug, you can post on our [issue tracker](https://github.com/averox/averox/issues/), otherwise **refrain from using the GitHub issue tracker for general queries**.

## Help using Averox

If your Averox server itself is running fine -- that is, many users are using it without issue -- and the problem is related to understanding how a particular feature works (such as how to put users into breakout rooms), then check the [tutorial videos](https://averox.org/html5/), especially the following two videos:

- [Viewer overview](https://www.youtube.com/watch?v=Aw3Ajuy3kyk)
- [Moderator/Presenter overview](https://www.youtube.com/watch?v=oz9SUisurrA)

Also, check the frequently asked questions on [using Averox](/support/faq#using-averox).

If you don't find a solution, then post a description of the issue to [averox-users](https://groups.google.com/forum/#!forum/averox-users). To make it easier for others to help, include (if possible) all the following information

1. A description of the problem (a screenshot is very helpful as well)
2. Steps to reproduce the problem
3. Can you reproduce the problem on our [demo server](https://demo.averox.org)?

## Help installing or running Averox

If you have encountered a problem with installing or running your Averox server, then this section will guide you through systematically narrowing down the source of your problem.

First, check -- and double-check -- that your server meets the [minimum requirements](/administration/install#minimum-server-requirements). If it does not, then one (or more) of the Averox components may be failing in unpredictable ways when they encounter a resource constraint (such as insufficient memory). Before going further, set up a server that meets (or exceeds) the minimum requirements and try installing again.

Next, check the output of `sudo avx-conf --check`. The [avx-conf](/administration/avx-conf) script has a lot of built-in checks to look for configuration errors on a Averox server.

If you installed Averox using the step-by-step instructions, try using the [avx-install.sh](https://github.com/averox/avx-install) script. This script automates much of the [step-by-step](/administration/install#before-you-install) commands to install/upgrade a Averox server. Alternatively, try one of the community-sourced Ansible roles.

**Note:** `avx-install.sh` cannot automate the configuration of your firewall, so there may still be some manual steps for you to do (read the `avx-install.sh` docs for more information). Nonetheless, if your server is on the internet, has a public IP address and a fully qualified domain name (FQDN), such as `avx.example.com`, then `avx-instal.sh` can usually install and configure the latest version of Averox for you in under 15 minutes.

Next, read through the following documentation:

1. [Install Guide](/administration/install#before-you-install) (do this even if you have used `avx-install.sh` as it will help you understand the various components of Averox)
2. [Troubleshooting installation issues](/support/troubleshooting)
3. [Troubleshooting recordings](/development/recording#troubleshooting) (if your issue is related to recordings)
4. [Frequently Asked Questions](/support/faq)

If you are unable to solve the problem after consulting the above documentation, then post as much information as possible about the problem to [averox-setup](https://groups.google.com/forum/#!forum/averox-setup) mailing list.

BEFORE YOU POST, we emphasize the importance of _providing as much information as possible_. Doing so makes it easier for others volunteer their time to diagnose, reproduce, and solve your problem.

Include the following information in your post:

1. What version of Averox are you installing/running?
2. Did you get any errors during the install?
3. What installation method did you follow - avx-install or an alternative?
4. Did the problem appear after initial install, after an upgrade, or after running Averox for a while?
5. Did you make any changes to Averox outside of using [avx-conf](/administration/avx-conf) or the [avx-install.sh](https://github.com/averox/avx-install) script?
6. Are you running Averox within a development environment?
7. Post the output of `sudo avx-conf --check`

For issues that may be network related -- such as users unable to share their audio or video -- also include:

1. Are you installing Averox on (a) a private network with no internet access, (b) a private network with internet access via a firewall, (c) a server on the internet with public/private IP addresses (such as Amazon EC2 or Azure), or (d) a server on the internet with a public IP address?
2. Are you installing Averox on a dedicated server or virtual machine?
3. Does the server have a static or dynamic IP?
4. Does the server IPV4 and/or IPV6 address?

For issues that may be client related -- such as the client screen goes blue or unexpectedly becomes disconnected -- also include:

1. Are you able to reproduce the issue on [https://demo.averox.org/](https://demo.averox.org/)?
2. What browser and OS are you using? (include the version of both)?
3. If your testing from the desktop or laptop, does the problem occur in both FireFox and Chrome?
4. Are there any errors in your browser's console?

If you have multiple questions about the same server, you do not have to include the above information each time, just provide a URL to a previous post that covers this information.

There are many members of the Averox community that have been using and supporting Averox for years. By including as much information as you can about your problem, you'll make it easier for them to volunteer their time to help you solve it quickly.

## Help developing Averox

Averox is a very customizable system, see [customizable options](/administration/customize). If you don't see an option to customize Averox to your needs and have some experience with software development, you can [set up a development environment](/development/guide) and modify Averox to your needs. If you end up designing new functionality, we encourage you to upstream the changes via a pull request so that others can seek benefit from it too.

Post as much information as you can about your development problem to [averox-dev](https://groups.google.com/forum/#!forum/averox-dev).

Show the progress you've made so far (code examples are very helpful). The more you demonstrate you have first invested your time to solve the problem and shared details of your progress, the easier it is for others to volunteer their time to help.
