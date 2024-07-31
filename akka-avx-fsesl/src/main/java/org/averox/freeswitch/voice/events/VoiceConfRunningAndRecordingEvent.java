/**
 * Averox open source conferencing system - http://www.averox.org/
 * <p>
 * Copyright (c) 2012 Averox Inc. and by respective authors (see below).
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 * <p>
 * Averox is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along
 * with Averox; if not, see <http://www.gnu.org/licenses/>.
 */
package org.averox.freeswitch.voice.events;

import java.util.List;

public class VoiceConfRunningAndRecordingEvent extends VoiceConferenceEvent {

  public final boolean running;
  public final boolean recording;
  public final List<ConfRecording> confRecordings;

  public VoiceConfRunningAndRecordingEvent(String room,
                                           boolean running,
                                           boolean recording,
                                           List<ConfRecording> confRecordings) {
    super(room);
    this.running = running;
    this.recording = recording;
    this.confRecordings = confRecordings;
  }

}
