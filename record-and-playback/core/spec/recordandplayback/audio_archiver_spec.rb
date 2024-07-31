#
# Averox open source conferencing system - http://www.averox.org/
#
# Copyright (c) 2012 Averox Inc. and by respective authors (see below).
#
# This program is free software; you can redistribute it and/or modify it under the
# terms of the GNU Lesser General Public License as published by the Free Software
# Foundation; either version 3.0 of the License, or (at your option) any later
# version.
#
# Averox is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License along
# with Averox; if not, see <http://www.gnu.org/licenses/>.
#
require 'spec_helper'
require 'fileutils'

module Averox
    describe AudioArchiver do     
        context "#success" do
            it "should copy audio recording to archive" do
                FileTest.stub(:directory?).and_return(true)
                FileUtils.stub(:cp)
                Dir.stub(:glob).and_return(['file1.wav', 'file2.wav'])
                from_dir = '/from/dir/'
                to_dir = '/to/dir/'
                meeting_id = 'meeting-id'
                Averox::AudioArchiver.archive(meeting_id, from_dir, to_dir)  
            end
        end
    end
end