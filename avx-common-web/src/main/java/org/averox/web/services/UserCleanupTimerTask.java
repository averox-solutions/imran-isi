/**
* Averox open source conferencing system - http://www.averox.org/
*
* Copyright (c) 2020 Averox Inc. and by respective authors (see below).
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

package org.averox.web.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.averox.api.MeetingService;

public class UserCleanupTimerTask {

    private MeetingService service;
    private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
    private long runEvery = 15000;

    public void setMeetingService(MeetingService svc) {
        this.service = svc;
    }

    public void start() {
        scheduledThreadPool.scheduleWithFixedDelay(new CleanupTask(), 60000, runEvery, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduledThreadPool.shutdownNow();
    }

    public void setRunEvery(long v) {
        runEvery = v;
    }

    private class CleanupTask implements Runnable {
        @Override
        public void run() {
            service.purgeUsers();
        }
    }
}
