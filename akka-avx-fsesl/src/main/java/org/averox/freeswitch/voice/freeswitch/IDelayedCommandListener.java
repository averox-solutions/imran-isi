package org.averox.freeswitch.voice.freeswitch;

import org.averox.freeswitch.voice.freeswitch.actions.FreeswitchCommand;

public interface IDelayedCommandListener {
  public void runDelayedCommand(FreeswitchCommand command);
}
