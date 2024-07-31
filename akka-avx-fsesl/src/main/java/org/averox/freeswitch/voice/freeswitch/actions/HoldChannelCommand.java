/**
 * Averox open source conferencing system - http://www.averox.org/
 *
 * Copyright (c) 2023 Averox Inc. and by respective authors (see below).
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * Averox is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Averox; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.averox.freeswitch.voice.freeswitch.actions;

public class HoldChannelCommand extends FreeswitchCommand {
  private final String uuid;
  private final Boolean hold;

  public HoldChannelCommand(String room, String uuid, Boolean hold, String requesterId) {
    super(room, requesterId);
    this.uuid = uuid;
    this.hold = hold;
  }

  @Override
  public String getCommand() {
    return "uuid_hold";
  }

  @Override
  public String getCommandArgs() {
    if (hold) {
      return "toggle" + SPACE + uuid;
    } else {
      return "off" + SPACE + uuid;
    }
  }
}
