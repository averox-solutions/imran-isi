---
id: recording
slug: /development/recording
title: Recording
sidebar_position: 5
description: Averox Recording
keywords:
- recording
- rap
---

This document assumes the reader understands the current [Averox architecture](/development/architecture).

## Overview

Averox records all the events and media data generated during a Averox session for later playback.

If you want to see the Record and Playback (RaP) feature in action, use Greenlight on the demo server and record a session [demo](https://demo.averox.org/gl). You can then play it back after it is listed under "Recorded Sessions" on the same page. Processing and publishing the media for playback after the end of your session may take a few minutes.

Like Averox sessions, the management of recordings should be handled by [third-party software](https://averox.org/schools/integrations/). Third-party software consumes the [Averox API](/development/api) to accomplish that. As a user, you may want to use third-party software which sets the right value to the parameter "record". As a developer, you may want to use a (not official) library that implements the API calls in your preferred language or implement it by yourself.

From a technical point of view, in the Averox API, when you pass the parameter `record=true` with [create](/development/api#create), Averox will create a session that has recording enabled. In this case, it will add a new button to the toolbar at the top of the window with a circle icon which a moderator in the session can use to indicate sections of the meeting to be recorded.

In a session with recording enabled, Averox will save the slides, chat, audio, desktop sharing (deskshare), whiteboard events, shared notes, captions, poll results, and webcams for later processing. This is the unique way to record a meeting, because it provides the ability for different workflows to create recordings with different properties, combining the media in unique ways.

After the session finishes, the Averox server will run an archive script that copies all of the related files to a single directory. It then checks to see if the moderator has clicked the "Record" button during the session to indicate a section of the meeting that should be turned into a recording. If the recording button was not clicked during the session, the files are queued to be deleted after two weeks. (You can override this and force a recording to be processed; see the `avx-record --rebuild` command below.)

After the recording is archived, Averox will run one (or more) ingest and processing scripts, named workflows, that will _process_ and _publish_ the captured data into a format for _playback_.

![Record and Playback - Overview](/img/diagrams/record-and-playback-service-diagram-rap-overview.png)

## Record and Playback Phases

Averox processes the recordings in the following order:

1. Capture
2. Archive
3. Sanity
4. Process
5. Publish
6. Playback

### Capture

The Capture phase involves enabling the Averox modules (chat, presentation, video, voice, etc.) to emit events over an event bus for capture on the Averox server. Components that generate media (webcam, voice, deskshare) must also store their data streams on the server.

Whiteboard, cursor, chat and other events are stored on Redis. Webcam videos (.flv) and deskshare videos (.flv) are recorded by Red5. The audio conference file (.wav) is recorded by FreeSWITCH. Shared notes and captions are taken from Etherpad.

### Archive

The Archive phase involves placing the captured media and events into a **raw** directory. That directory contains ALL the necessary media and files to work with.

![Record and Playback - Archive](/img/diagrams/record-and-playback-service-diagram-rap-archive.png)

### Sanity

The Sanity phase involves checking that all the archived files are _valid_ for processing. For example
that media files have not zero length and events were archived.

![Record and Playback - Sanity](/img/diagrams/record-and-playback-service-diagram-rap-sanity.png)

### Process

The Process phase involves processing the valid archived files of the recording according to the workflow (e.g., presentation). Usually, it involves parsing the archived events, converting media files to other formats or concatenating them, etc.

![Record and Playback - Process](/img/diagrams/record-and-playback-service-diagram-rap-process.png)

### Publish

The Publish phase involves generating metadata and taking many or all the processed files and placing them in a directory exposed publicly for later playback.

![Record and Playback - Publish](/img/diagrams/record-and-playback-service-diagram-rap-publish.png)

### Post-Scripts

_Post-scripts_ allow you to perform site-specific actions after each of the Archive, Process, and Publish steps of the Recording processing.

Some examples of things you might use the post-scripts to do:

- Send you an email after a recording is published.
- Back up a recording to another server after your recording is archived or published.
- Send a text message after a recording is published.
- Compress media files and make them publicly available for download after it is published.
- Delete raw media files after the recording processing completes.

### Playback

The Playback phase involves taking the published files (audio, webcam, deskshare, chat, events, notes, captions, polls, metadata) and playing them in the browser.

Using the workflow **presentation**, playback is handled by HTML, CSS, and Javascript libraries;
it is fully available in Mozilla Firefox and Google Chrome (also on Android devices). In other
browsers like Opera or Safari the playback will work without all its functionality, e.g.,
thumbnails won't be shown.

There is not a unique video file for playback in the **presentation** workflow.
However, the **video** workflow can be used to generate that unique video file.
This workflow is not enabled by default. [See section below](#deploying-other-formats) to
learn how to enable it.

## Media storage

Some Record and Playback phases store the media they handle in different directories:

### Captured files

- AUDIO: `/var/freeswitch/meetings`
- WEBCAM: `/var/lib/avx-webrtc-recorder/recordings/<meetingid>`
- SCREEN SHARING: `/var/lib/avx-webrtc-recorder/screenshare/<meetingid>`
- SLIDES: `/var/averox/<meetingid>`
- NOTES: `http://localhost:9002/p`
- EVENTS: `Redis`

#### Archived files

The archived file are located in `/var/averox/recording/raw/<internal-meeting-id>/`.

#### Sanity checked files

The sanity files are store in the same place as the archived files.

#### Processed files

The processed files can be found in `/var/averox/recording/process/presentation/<internal-meeting-id>/`.

#### Published files

The published files are in `/var/averox/recording/publish/presentation/<internal-meeting-id>/`.

#### Playback files

The playback files are found in `/var/averox/published/presentation/<internal-meeting-id>/`.

## Manage recordings

Averox does not have an administrator web interface to control the sessions or recordings as in both cases they are handled by 3rd party software, but it has a useful tool to monitor the state and control your recordings through the phases described above.

In the terminal of your server you can execute `avx-record`, which will show you each option with its description:

```
Averox Recording Diagnostic Utility (Averox Version 2.5.N)

   avx-record [options]

Reporting:
   --list                              List all recordings
   --list-recent                       List recent recordings
   --list-recent --withDesc            List recent recordings and show their description
   --list-workflows                    List available recording workflows

Monitoring:
   --watch                             Watch processing of recordings
   --watch --withDesc                  Watch processing of recordings and show their description

Administration:
   --rebuild <internal meetingID>      rebuild the output for the given internal meetingID
   --rebuildall                        rebuild every recording
   --delete <internal meetingID>       delete one meeting and recording
   --deleteall                         delete all meetings and recordings
   --debug                             check for recording errors
   --check                             check for configuration errors
   --enable <workflow>                 enable a recording workflow
   --disable <workflow>                disable a recording workflow
   --tointernal <external meetingId>   get the internal meeting ids for the given external meetingId
   --toexternal <internal meetingId>   get the external meeting id for the given internal meetingId
   --republish <internal meetingID>    republish the recording for meetingID.
                                       (Only for Matterhorn Integration)
```

#### Useful settings

| Name                    | Path (file)                                                 | Description |
| :-----------------------| :-----------------------------------------------------------| :-----------|
| webcamsOnlyForModerator | /usr/share/avx-web/WEB-INF/classes/averox.properties | If true, only the moderator's cam will be showed in playback depending on how the `show_moderator_viewpoint`. It can be changed in the ongoing meeting|
| show_moderator_viewpoint | /usr/local/averox/core/scripts/averox.yml | If false, the viewer's point of view will be showed, meaning that if the above property is set to true, only the moderator's camera will be displayed. |


#### Useful terms

- workflow - is the way a recording is processed, published, and played. In Averox 2.4 the unique workflow out of the box is the "presentation" format.
- internal meetingId - is an alphanumeric string that internally identifies your recorded meeting. It is created internally by Averox. For example "183f0bf3a0982a127bdb8161e0c44eb696b3e75c-1379693236230".
- external meetingID - is the id you set to the meeting, like "English 201" or "My Awesome class", "Chemistry 2". It is passed through the create API call.
- recording - is recorded meeting in Averox.

In Averox you can use the same external meeting ID (for example "English 101") in many recordings, but each recording will have a different internal meeting id. One external meeting id is associated with **one or many** internal meeting ids.

#### List recordings

```bash
$ avx-record --list
```

will list all your recordings.

#### List recent recordings

```bash
$ avx-record --list-recent
```

will list the 10 most recent recordings.

#### List workflows

Lists the enabled recording processing steps and the available processing scripts.
This may provide valuable insights in case you're trying to debug why recordings aren't being processed in a specific format.

```bash
$ avx-record --list-workflows

## Enabled recording processing steps:
#
## archive
## sanity
## captions
## process:presentation
## publish:presentation
#
## Available processing scripts:
#
## presentation
```

#### Watch recordings

```bash
$ avx-record --watch
```

Lists your 10 latest recordings, refreshing the output every 2 seconds. As Averox processes a recording, you'll see progress updates from the background services.

```bash
Every 2.0s: avx-record --list-recent
Internal MeetingID                   Time   APVD APVDE RAS Slides Processed Published Ext. Meeting ID
------------------------------------------- ---- ----- --- ------ --------- --------- ---------------
29173583...1508b2efd7-1647630316965  Mar 18  X    XX X XX      7  pres.     pres.     English 102
6e35e3b2...f5db637d7a-1538942881350  Oct 7   X    X  X         6            pres.     English 101
9e468g2k...hij43b5d8b-1538941988186  Oct 7  XX   XX  X         9            pres.     Demo Meeting

--
0 timers listed.
--
● avx-rap-starter.service - Averox recording processing starter
   Loaded: loaded (/usr/lib/systemd/system/avx-rap-starter.service; enabled; vendor preset: enabled)
   Active: active (running) since Tue 2022-03-01 18:27:19 CET; 2 weeks 0 days ago
 Main PID: 3509 (rap-starter.rb)
    Tasks: 2 (limit: 4915)
   CGroup: /avx_record_core.slice/avx-rap-starter.service
           └─3509 /usr/bin/ruby /usr/local/averox/core/scripts/rap-starter.rb

● avx-rap-resque-worker.service - Averox resque worker for recordings
   Loaded: loaded (/usr/lib/systemd/system/avx-rap-resque-worker.service; enabled)
   Active: active (running) since Tue 2022-03-01 18:27:19 CET; 2 weeks 0 days ago
 Main PID: 3847 (sh)
    Tasks: 7 (limit: 4915)
   CGroup: /system.slice/avx-rap-resque-worker.service
           ├─ /usr/bin/rake -f ../Rakefile resque:workers >> /var/log/averox/avx-rap-worker.log
           ├─ /usr/bin/ruby /usr/bin/rake -f ../Rakefile resque:workers
           └─ Waiting for rap:archive,rap:publish,rap:process,rap:sanity,rap:captions,rap:events
--
```

#### Rebuild a recording

This options makes a recording go through the Process and Publish phases again.

If you run `avx-record --rebuild` on a recording where the process and publish scripts were not run because the moderator of the session did not click the record button, this will force the meeting to be processed as long as the raw files are still available and `record` was set to `true`. In this case, the entire length of the meeting will be included in the recording.

```bash
$ sudo avx-record --rebuild 29173583cf1ca21508b2efd7db566090bbefb36a-1647630316965
```

#### Rebuild every recording

This option goes through the Process and Publish phases again for every recording in your server.
As a result, it may take a long time to finish.

```bash
$ sudo avx-record --rebuildall
```

#### Delete a meeting and its recording

Removes the meeting's data (such as uploaded files) and its recording.

```bash
$ sudo avx-record --delete 29173583cf1ca21508b2efd7db566090bbefb36a-1647630316965
```

Warning -- this also wipes running meetings! To only remove a single published recording and its raw data, delete 

- `/var/averox/recording/raw/<meetingId>`
- `/var/averox/published/presentation/<meetingId>`

#### Delete all meetings and their recordings

Deletes all meeting-related data (such as uploaded files) and their recordings.

```bash
$ sudo avx-record --deleteall
```

Warning -- this also wipes running meetings! To only remove the published recordings and their raw data, delete all entries under

- `/var/averox/recording/raw/`
- `/var/averox/published/`

#### Debug recordings

Check recording log files, looking for errors since the Archive phase.

```bash
$ sudo avx-record --debug
```

#### Check recording configuration

Check for configuration errors in Averox 2.5 and later that may be preventing the recording service from running properly, such as permission and ownership checks.

```bash
$ sudo avx-record --check
```

#### Enable a workflow

```bash
$ sudo avx-record --enable presentation
```

#### Disable a workflow

```bash
$ sudo avx-record --disable presentation
```

#### Get internal meeting ids

```bash
$ sudo avx-record --tointernal "English 102"
```

will show

```
Internal meeting ids related to the given external meeting id:
-------------------------------------------------------------
29173583cf1ca21508b2efd7db566090bbefb36a-1647630316965
```

Use double quotes for the external meeting id.

#### Get external meeting ids

```bash
$ sudo avx-record --toexternal 29173583cf1ca21508b2efd7db566090bbefb36a-1647630316965
```

```
External meeting id related to the given internal meeting id:
-------------------------------------------------------------
English 102" meetingName="English 102
-------------------------------------------------------------
```

#### Republish recordings

Republish recordings.

```bash
$ sudo avx-record --republish 29173583cf1ca21508b2efd7db566090bbefb36a-1647630316965
```

### For Developers

Here you will find more details about the Record and Playback scripts.

The way to start a recorded session in Averox is setting the "record" parameter value to "true" in the [create API](/development/api#create) call, which usually is handled by third party software.

#### Capture phase

The Capture phase is handled by many components.

To understand how it works, you should have basic, intermediate, or advanced understanding about tools like FreeSWITCH, Flex, Red5, and Redis. Dig into the [Averox source code](https://github.com/averox/averox) and search for information in the [Averox mailing list for developers](https://groups.google.com/forum/#!forum/averox-dev) if you have more questions.

#### Archive, Sanity, Process and Publish

These phases are handled by Ruby scripts. The directory for those files is `/usr/local/averox/core/`

```
/usr/local/averox/core/
├── Gemfile
├── Gemfile.lock
├── Rakefile
├── lib
│   ├── boot.rb
│   ├── custom_hash.rb
│   ├── recordandplayback
│   │   ├── edl
│   │   │   ├── audio.rb
│   │   │   └── video.rb
│   │   ├── edl.rb
│   │   ├── events_archiver.rb
│   │   ├── generators
│   │   │   ├── audio.rb
│   │   │   ├── audio_processor.rb
│   │   │   ├── captions.rb
│   │   │   ├── events.rb
│   │   │   ├── presentation.rb
│   │   │   └── video.rb
│   │   ├── workers
│   │   │   ├── archive_worker.rb
│   │   │   ├── base_worker.rb
│   │   │   ├── captions_worker.rb
│   │   │   ├── events_worker.rb
│   │   │   ├── process_worker.rb
│   │   │   ├── publish_worker.rb
│   │   │   └── sanity_worker.rb
│   │   └── workers.rb
│   └── recordandplayback.rb
└── scripts
    ├── README
    ├── archive
    │   └── archive.rb
    ├── avx-0.9-beta-recording-update
    ├── avx-0.9-recording-size
    ├── avx-1.1-meeting-tag
    ├── averox.yml
    ├── caption
    │   └── presentation
    ├── cleanup.rb
    ├── post_archive
    │   └── post_archive.rb.example
    ├── post_events
    │   ├── post_events.rb.example
    │   └── post_events_analytics_callback.rb
    ├── post_process
    │   └── post_process.rb.example
    ├── post_publish
    │   ├── post_publish.rb.example
    │   ├── post_publish_recording_ready_callback.rb
    ├── presentation.yml
    ├── process
    │   ├── README
    │   └── presentation.rb
    ├── publish
    │   ├── README
    │   └── presentation.rb
    ├── rap-caption-inbox.rb
    ├── rap-enqueue.rb
    ├── rap-process-worker.rb
    ├── rap-starter.rb
    ├── sanity
    │   └── sanity.rb
    └── utils
        ├── captions.rb
        ├── gen_poll_svg
        └── gen_webvtt
```

The main file is `rap-worker.rb`, it executes all the Record and Playback phases

1. Detects when new captured media from a session is available.

2. Go through the Archive phase (`/usr/local/averox/core/scripts/archive/archive.rb`)

3. Go through the Sanity phase executing (`/usr/local/averox/core/scripts/sanity/sanity.rb`)

4. Go through the Process phase executing all the scripts under `/usr/local/averox/core/scripts/process/`

5. Go through the Publish phase executing all the scripts under `/usr/local/averox/core/scripts/publish/`

- Files ending with "archiver.rb" contain scripts with logic to archive media.

- Files under `/usr/local/averox/core/lib/generators/` contains scripts with logic, classes and methods used by other scripts which archive or process media.

- .yml files contain information used by the process and publish scripts.

#### Writing Post-Scripts

In your server, there are separate "drop-in" directories under `/usr/local/averox/core/scripts` for each of the `post_archive`, `post_process`, and `post_publish` steps. Each of these directories can contain Ruby scripts (`.rb` extension required), which will be run in alphabetical order after the corresponding recording step.

The scripts take the argument `-m`, the meeting id as a parameter, and `-f`, the format name calling the script.

A set of example scripts is provided to give you a framework to build your custom scripts from.

```
|-- post_archive
|   `-- post_archive.rb.example
|-- post_process
|   `-- post_process.rb.example
`-- post_publish
    `-- post_publish.rb.example
```

The example files give the file paths where the files from the corresponding step are located, and include code for accessing the metadata variables from the meeting. For example, if you passed a variable named

```ruby
meta_postpublishemail=user@example.com
```

when creating the meeting, you can access it by doing

```ruby
email = meeting_metadata['postpublishemail']
```

in the script.

You are free to do anything you like inside the post-scripts, including modifying recording files from a previous step before the next step occurs.

#### Playback phase

Playback works with [avx-playback](https://github.com/averox/avx-playback), a standalone React app that uses Video.js' media player.
Slides with annotations, captions, chat, cursor, poll results, deskshares, and webcams are shown according to the current time played in the audio file.
Playback files are located in `/var/averox/playback/presentation/` and used to play any published recording.

#### Manually executing recording scripts

If you intend on developing recording scripts, manually running them through each of the RaP steps will enable you to improve existing workflows or add new ones.
The following example is based on the presentation format.

First, stop the recording service and create the required temporary directories:

```bash
systemctl stop avx-rap-starter
systemctl stop avx-rap-resque-worker
```

```bash
mkdir -p ~/temp/log/presentation ~/temp/recording/{process,publish,raw} ~/temp/recording/status/{recorded,archived,processed,sanity} ~/temp/published
```

Then, edit `~/dev/averox/record-and-playback/core/scripts/averox.yml` and comment the `PRODUCTION` directories while uncommenting the `DEVELOPMENT` folders. The path should match the ones created above.

```bash
## For PRODUCTION
## log_dir: /var/log/averox
events_dir: /var/averox/events
## recording_dir: /var/averox/recording
## published_dir: /var/averox/published
captions_dir: /var/averox/captions
## playback_host: develop.distancelearning.cloud
playback_protocol: https

## For DEVELOPMENT
## This allows us to run the scripts manually
scripts_dir: /home/ubuntu/dev/averox/record-and-playback/core/scripts
log_dir: /home/ubuntu/temp/log
recording_dir: /home/ubuntu/temp/recording
published_dir: /home/ubuntu/temp/published
playback_host: 127.0.0.1
```

Now, create a recording using Averox. After ending the session, there should be a `<meeting-id>.done` file in the
`/var/averox/recording/status/recorded`. Make note of the latest internal meeting ID, as we'll use it
to tell our scripts which recording to process.

Before running the scripts, we have to make sure our scripts have the paths set up correctly.
In your development environment (`~/dev/averox/record-and-playback`), edit

  - /core/scripts/archive/archive.rb
  - /core/scripts/sanity/sanity.rb
  - presentation/scripts/process/presentation.rb
  - presentation/scripts/publish/presentation.rb
  - presentation/scripts/presentation.yml

and ensure the scripts are using the `DEVELOPMENT PATH`. We need to do this so the script will be able to find the core library.

```ruby
## For DEVELOPMENT
## Allows us to run the script manually
require File.expand_path('../../../../core/lib/recordandplayback', __FILE__)

## For PRODUCTION
## require File.expand_path('../../../lib/recordandplayback', __FILE__)
```

```bash
## For PRODUCTION
## publish_dir: /var/averox/published/presentation
video_formats:
- webm
## - mp4

## For DEVELOPMENT
publish_dir: /home/ubuntu/temp/published/presentation
```

Now we run the archive step. Go to `record-and-playback/core/scripts` and type

```ruby
ruby archive/archive.rb -m <meeting-id>
```

If everything went well, you should now have the raw files in `~/temp/recording/raw/<meeting-id>`.
You can also check the logs at `~/temp/log/archive-<meeting-id>.log`, and have a `.done` entry in `~/temp/recording/status/archived`.

Now we need to do a sanity check to ensure the raw recordings are complete. Again in the scripts folder, type

`ruby sanity/sanity.rb -m <meeting-id>`

Confirm everything went as intended by checking the logs at `~/temp/log/sanity.log` and
that a `.done` entry in `~/temp/recording/status/sanity` exists.

Assuming the recording passed the sanity check, it's time to process the recording.

Set the path of the captions generation script:

```ruby
## ret = Averox.exec_ret('utils/gen_webvtt', '-i', raw_archive_dir, '-o', target_dir)
ret = Averox.exec_ret(
   '/home/ubuntu/dev/averox/record-and-playback/core/scripts/utils/gen_webvtt',
   ...)
```

Now, the presentation processing script can run without throwing an error.

```bash
cd ~/dev/averox/record-and-playback/presentation/scripts
ruby process/presentation.rb -m <meeting-id>
```

You can monitor the progress by tailing the log at `~/temp/log/presentation/process-<meeting-id>.log`.

If everything went well, we can now run the publish script. However, we need to cheat a little bit.
The publish script will be looking for a `processing_time` file which contains information on how long the
processing took. Unfortunately, that file is created by the `rap-worker.rb`, a script which we don't run.

Manually create the file at

`vi ~/temp/recording/process/presentation/<meeting-d>/processing_time`,

saving it after entering any number (e.g. 46843).

Then, once again, the path to the poll generation utility needs to be updated:

```ruby
#ret = Averox.exec_ret('utils/gen_poll_svg', ...)
ret = Averox.exec_ret(
   '/home/ubuntu/dev/averox/record-and-playback/core/scripts/utils/gen_webvtt',
   ...)
```

Finally, run the publish script:

```bash
ruby publish/presentation.rb -m <meeting-id>-presentation
```

Notice we appended "presentation" to the meetingId, this will tell the script to publish using the "presentation" format.

### Running record-and-playback as a service

You can deploy your changes by running `deploy.sh` and restarting the recording-related services:
```
systemctl restart avx-rap-starter
systemctl restart avx-rap-resque-worker
```

#### Deploying other formats

When running `deploy.sh`, it will only set the presentation workflow by default. However it is possible to add other formats too such as video, screenshare, etc. In order to do that, you just have to change `deploy_format "presentation"` in the `deploy.sh` script, adding whatever formats you want, so, in case of `video` and `screenshare`, it would be:

```bash
deploy_format "presentation video screenshare"
```

Another adaptation to deploy other formats is to follow the steps in [additional recording formats](/administration/customize#install-additional-recording-processing-formats) to enable these new workflows you just set. For example, in your `averox.yml`, you may have:

```yml
steps:
  archive: 'sanity'
  sanity: 'captions'
  captions:
    - 'process:presentation'
    - 'process:video'
  'process:presentation': 'publish:presentation'
  'process:video': 'publish:video'
```

At last, if it is the first time you set one other recording format, you may have to reload nginx in addition to the other commands mentioned previously in this section, like so:

```
systemctl restart avx-rap-starter
systemctl restart avx-rap-resque-worker
systemctl reload nginx
```

If you are not changing the nginx files for each format, it is not mandatory to reload nginx every time you run `deploy.sh`.

### Troubleshooting

Apart from the command

```bash
$ sudo avx-record --debug
```

You can use the output from

```bash
$ sudo avx-record --watch
```

to detect problems with your recordings.

Starting from BBB 2.5,

```bash
$ sudo avx-record --check
```

will additionally run a recording diagnostic utility with permission and ownership checks that may help in circumstances where recording processing is not starting.

To investigate the processing of a particular recording, you can look at the following log files:

The `avx-rap-worker` log is a general log file that can be used to find which section of the recording processing is failing. It also logs a message if a recording process is skipped because the moderator did not push the record button.

```
/var/log/averox/avx-rap-worker.log
```

To investigate an error for a particular recording, check the following log files:

```
/var/log/averox/archive-<recordingid>.log
/var/log/averox/<workflow>/process-<recordingid>.log
/var/log/averox/<workflow>/publish-<recordingid>.log
```

Restarting the recording-related services may also help:

```bash
systemctl restart avx-rap-starter
systemctl restart avx-rap-resque-worker
```

#### Understanding output from avx-record --watch

![watch](/img/avx-record-watch-explanation.png)

This section is intended to help you find and solve problems in the
Record and Playback component of Averox, by watching the output of the command

```bash
$ avx-record --watch
```

##### RAS ( RECORDED - ARCHIVED - SANITY CHECKED )

Below **RAS** you won't see any **_X_** until the meeting has finished.

Once the meeting has finished, an **_X_** should appear under **R**. If it doesn't, ensure that:

- all users left the meeting.

- the parameter `meetingExpireWhenLastUserLeftInMinutes` in `/usr/share/avx-web/WEB-INF/classes/averox.properties` is not set to a big value (defaults to 1).

- the parameter `record=true` was passed in the `create` API call; if not, the meeting was not recorded.

Once the recording is archived, an **_X_** should appear under **A**. Otherwise, verify that:

- The following file exists:

`/var/averox/recording/status/recorded/<internal meeting id>.done`

that means that the meeting was recorded.

Once the recording passed the sanity check, an **_X_** should appear under **S**. In case it doesn't,

- A media file was not properly archived. Find the cause of the problem in the sanity log

```bash
$ grep <internal meeting id> /var/log/averox/sanity.log
```

##### APVD (Audio, Presentation, Video, Deskshare)

This section is related to recorded media. By default, Audio and Presentation are recorded. If you don't see any **_X_** under **V** or **D**, then you either didn't
share your webcam or desktop, or you haven't enabled them to be recorded.

##### APVDE (Audio, Presentation, Video, Deskshare, Events)

This section is related to archived media. If you don't see an **_X_** under media you are sure was recorded, check out
the sanity log. Execute this command to find the problem:

```bash
$ grep <internal meeting id> /var/log/averox/sanity.log
```

##### Processed

If a script was applied to process your recording, its name should be listed under the column 'Processed.' By default, you should see
'presentation,' if you don't see one of them, find the problem in the log file of the processed recording:

```bash
$ grep -B 3 "status: 1" /var/log/averox/presentation/process-<internal meeting id>.log | grep ERROR
```

The problem should then be shown. If there is no output, tail the file to see which task was executed last.
Its error message describes the issue.

##### Published

If a script is applied to publish your recording, its name should be listed under the column 'Published.'

```bash
$ grep -B 3 "status: 1" /var/log/averox/presentation/publish-<internal meeting id>.log | grep ERROR
```

The problem should then be shown. If there is no output, tail the file to see which task was executed last.
Its error message describes the issue.

## FAQs

### How do I change the Start/Stop recording marks?

Note: In Averox 2.6.9 we [made a change](https://github.com/averox/averox/pull/18044) which ensures that by default media files are not being saved to file unless the meeting is actively being recorded. If you are using Averox 2.6.9 or later, the instructions below will only work if you set `recordFullDurationMedia=true` in `/etc/averox/avx-web.properties`.

In a scenario where a user forgot to press the Start/Stop recording button 30 minutes into a session, resulting in the playback missing that initial segment, its content can still be included by editing the intervals to be processed in the `events.xml` file.

First, use `avx-record --list` to find the internal `meetingId` for the recording. For example, to get the last three recordings, execute

```bash
$ sudo avx-record --list | head -n 5

Internal MeetingID                   Time   APVD APVDE RAS Slides Processed Published Ext. Meeting ID
------------------------------------------- ---- ----- --- ------ --------- --------- ---------------
29173583...1508b2efd7-1647630316965  Mar 18  X    XX X XX      7  pres.     pres.     English 102
6e35e3b2...f5db637d7a-1538942881350  Oct 7   X    X  X         6            pres.     English 101
6e35e3b2...f5db637d7a-1538941988186  Oct 7  XX   XX  X         6            pres.     English 101
```

The first recording has an internal `meetingID` of `29173583cf1ca21508b2efd7db566090bbefb36a-1647630316965`. If this is the recoding you want to edit, you'll find `events.xml` in the location `/var/averox/recording/raw/29173583cf1ca21508b2efd7db566090bbefb36a-1647630316965/events.xml`. Edit this file and look for the first `RecordingStatusEvent`, such as:

```xml
<event timestamp="7610895212" module="PARTICIPANT" eventname="RecordStatusEvent">
   <timestampUTC>1647630325790</timestampUTC>
   <date>2022-03-18T20:05:25.790+01</date>
   <status>true</status>
   <userId>w_j0ke0updyniq</userId>
</event>
```

Next, find when the first moderator joined, and then move the `RecordStatusEvent` after the moderator join event. Also, edit the `timestamp` for `RecordStatusEvent` so it occurs **after** the moderator's `timestamp`. For example:

```xml
<event timestamp="7610890308" module="PARTICIPANT" eventname="ParticipantJoinEvent">
    <name>User 8828229</name>
    <timestampUTC>1647630320886</timestampUTC>
    <role>MODERATOR</role>
    <date>2022-03-18T20:05:20.886+01</date>
    <externalUserId>w_j0ke0updyniq</externalUserId>
    <userId>w_j0ke0updyniq</userId>
</event>

<event timestamp="7610890309" module="PARTICIPANT" eventname="RecordStatusEvent">
    <timestampUTC>1647630325790</timestampUTC>
    <date>2022-03-18T20:05:25.790+01</date>
    <status>true</status>
    <userId>w_j0ke0updyniq</userId>
</event>
```

This is equivalent to the first moderator clicking the Start/Stop Record button. Save the modified `events.xml` and regenerate recording using the `avx-record --rebuild` command, as in

```bash
$ sudo avx-record --rebuild 29173583cf1ca21508b2efd7db566090bbefb36a-1647630316965
```

If there are no other recordings processing, you should see the recording re-process using the `sudo avx-record --watch` command. After the processing is finished, your users can view the recording and see all the content from the time the first moderator joined.

### Is the recording activated automatically?

No. When creating the meeting, the API parameters must include `record=true` to enable recording. Raw files of the entire meeting are then stored on the server, and trimmed by the presentation format according to the Start/Stop marks found in `events.xml` for playback.

In Averox 0.9.1, to have a recorded session create a playback file, a moderator must click the Start/Stop Record button during the session. Otherwise, Averox will not create a playback file and delete the recorded session.

### How to delete recordings before storage device is full?

`/etc/cron.daily/averox` deletes the archived media. Edit the file and add more rules if you need to delete others like raw files or processed files.

## Events outline

### Overview

The sections that follow cover the types of events you will encounter in `events.xml` (in case you want to parse them with your own scripts).

### Chat

| Event           | Attributes                                             |
| :-------------- | :----------------------------------------------------- |
| PublicChatEvent | - timestampUTC<br/>- senderId<br/>- date<br/>- message |


### Participant

| Event                        | Attributes                                                                         |
| ---------------------------- | ---------------------------------------------------------------------------------- |
| AssignPresenterEvent         | - name<br/>- timestampUTC<br/>- userid<br/>  - assignedBy<br/> - date              |
| ParticipantJoinEvent         | - name<br/>- timestampUTC<br/>- role<br/> - date<br/> externalUserID<br/> - userId |
| EndAndKickAllEvent           | - timestampUTC<br/>- reason<br/>- date<br/>                                        |
| RecordStatusEvent            | - timestampUTC<br/>- date<br/>-status<br/> -userId                                 |
| ParticipantStatusChangeEvent | - timestampUTC<br/>- status<br/>- userId<br/>- date<br/>- value                    |

### Presentation

| Event                      | Attributes                                                                                                                                |
| -------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| ConversionCompletedEvent   | - originalFilename<br/>- timestampUTC<br/> - presentationName<br/>- podId<br/> -share<br/>- date                                          |
| ResizeAndMoveSlideEvent    | - timestampUTC<br/>- xOffset<br/>- presentationName<br/>- podId<br/>- heightRatio<br/>- id<br/>- date<br/>- yOffset<br/>- widthRatio      |
| SharePresentationEvent     | - timestampUTC<br/>- presentationName<br/>- podId<br/>- share<br/>- date                                                                  |
| GotoSlideEvent             | - timestampUTC<br/>- presentationName<br/>- podId<br/>- id<br/>- slide<br/>- date                                                         |
| CreatePresentationPodEvent | - currentPresenter<br/>- timestampUTC<br/>- podID<br/>- date                                                                              |
| SetPresentationDownloadable| - timestampUTC<br/>- presentationName<br/>- podId<br/>- date<br/>- downloadable                                                           |
| SetPresentarInPodEvent     | - timestampUTC<br/>- podId<br/> - date<br/>- nextPresenterId

### Whiteboard

| Event                     | Attributes                                                                                                                                                                                                                                           |
| ------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| AddShapeEvent (shape)     | - timestampUTC<br/>- color<br/>- thickness<br/>- id<br/>- adte<br/>- whiteboardId<br/>- fill<br/>- status<br/>- pageNumber<br/>- position<br/>- userId<br/>- dataPoints<br/>- type<br/>- shapeId<br/>- presentation                                  |
| AddShapeEvent (text)      | - x <br/>- timestampUTC<br/>- y<br/>- fontColor<br/>- calcedFontSize<br/>- textBoxWidth<br/>- id<br/>- date<br/>- whiteboardId<br/>- status<br/>- pageNumber<br/>- fontSize<br/>- position<br/>- userId<br/>- dataPoints<br/>- type<br/>- shapeID    |
| AddShapeEvent (poll)      | - num_responders <br/>- status<br/>- userId<br/>- result<br/>- whiteboardId<br/>- position<br/>- num_respondents<br/>- id<br/>- pageNumber<br/>- presentation<br/>- dataPoints<br/>- timestampUTC<br/>- date<br/>- pollType<br/>- type<br/>- shapeId |
| UndoAnnotationEvent       | - timestampUTC<br/>- date<br/>- whiteboardID<br/>- pageNumber<br/>- userId<br/>- shapeId<br/>- presentation                                                                                                                                          |
| WhiteboardCursorMoveEvent | - timestampUTC<br/>- xOffset<br/>- date<br/>- whiteboardId<br/>- pageNumber<br/>- userId<br/>- yOffset<br/>- presentation                                                                                                                            |

### Voice

| Event                   | Attributes                                                                                                                   |
| ----------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| StartRecordingEvent     | - bridge <br/>- timestampUTC<br/>- filename <br/>- date <br/>- recordingTimestamp                                            |
| ParticipantJoinedEvent  | - participant <br/>- timestampUTC <br/>- bridge <br/>- callername <br/>- talking <br/>- callernumber <br/>- date<br/>- muted |
| ParticipantTalkingEvent | - participant<br/>- timestampUTC<br/>- bridge<br/>- talking<br/>- date                                                       |
| ParticipantMutedEvent   | - participant<br/>- timestampUTC<br/>- bridge<br/>- date<br/>- muted                                                         |
| ParticipantLeftEvent    | - participant<br/>- timestampUTC<br/>- bridge<br/>- date                                                                     |

### Pads (for shared notes and captions)

| Event                        | Attributes                                             |
| :--------------------------- | :----------------------------------------------------- |
| PadCreatedEvent              | - timestampUTC<br/>- date<br/>- padId                  |

### Deskshare

| Event                        | Attributes                                             |
| :--------------------------- | :----------------------------------------------------- |
| DeskshareStartedEvent        | - file<br/>- stream                                    |
| DeskshareStoppedEvent        | - file<br/>- duration                                  |
| DeskShareStartRTMP           | - timestampUTC<br/>- startIstreamPathndex<br/> - date  |
| DeskShareStopRTMP            | - timestampUTC<br/>- startIstreamPathndex<br/> - date  |
| StartWebRTCDesktopShareEvent | - timestampUTC<br/>- filename<br/>- meetingId          |
| StartWebRTCDesktopShareEvent | - timestampUTC<br/>- filename<br/>- meetingId          |

### External video

| Event                         | Attributes                                                                   |
| :---------------------------- | :--------------------------------------------------------------------------- |
| StartExternalVideoRecordEvent | - timestampUTC<br/>- date<br/> - externalVideoURL                            |
| UpdateExternalVideoRecordEvent| - rate<br/>- timestampUTC<br/> - state<br/> - date<br/> - status<br/> - time |
| StopExternalVideoRecordEvent  |                                                                              |

### Poll

| Event                          | Attributes                                                                                                       |
| ------------------------------ | ---------------------------------------------------------------------------------------------------------------- |
| PollStartedRecordEvent         | - secretPoll<br/>- timestampUTC<br/>- pollId<br/>- date<br/> - type<br/> - question<br/> - answers              |
| UserRespondedToPollRecordEvent |                                                                                                                  |
| PollStoppedRecordEvent         | - timestampUTC<br/> - pollId<br/> - date                                                                         |
| PollPublishedRecordEvent       | - numResponders<br/>- numRespondents<br/>- timestampUTC<br/>- pollId<br/>- date<br/>- question<br/>- answers     |

### Webcam

| Event                     | Attributes                                                |
| :-------------------------| :---------------------------------------------------------|
| StartWebcamShareEvent     | - stream                                                  |
| StopWebcamShareEvent      | - stream <br/>- duration                                  |
| StartWebRTCShareEvent     | - timestampUTC<br/> - filename<br/> - meetingId           |
| StopWebRTCShareEvent      | - timestampUTC<br/> - filename<br/> - meetingId           |
| MeetingConfigurationEvent | - timestampUTC<br/> - date<br/> - webcamsOnlyForModerator |