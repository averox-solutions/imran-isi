/**
* Averox open source conferencing system - http://www.averox.org/
* 
* Copyright (c) 2012 Averox Inc. and by respective authors (see below).
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

package org.averox.freeswitch.voice.freeswitch.response;

/**
 *
 * @author leif
 */
public class ConferenceMemberFlags {
    //private boolean canHear = false;
    private boolean canSpeak = false;
    private boolean talking = false;
    private boolean hold = false;
    //private boolean hasVideo = false;
    //private boolean hasFloor = false;
    //private boolean isModerator = false;
    //private boolean endConference = false;

    boolean getIsSpeaking() {
        return talking;
    }

    boolean getIsMuted() {
        if(canSpeak == true) {
            return false;
        }
        return true;
    }

    void setCanSpeak(String tempVal) {
        canSpeak = tempVal.equals("true") ? true : false;
    }

    void setTalking(String tempVal) {
        talking = tempVal.equals("true") ? true : false;
    }

    void setHold(String tempVal) {
        hold = tempVal.equals("true") ? true : false;
    }

    boolean getHold() {
        return hold;
    }
}