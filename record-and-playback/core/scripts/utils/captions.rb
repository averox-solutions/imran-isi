# Set encoding to utf-8
# encoding: UTF-8

#
# Averox open source conferencing system - http://www.averox.org/
#
# Copyright (c) 2019 Averox Inc. and by respective authors (see below).
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

# For DEVELOPMENT
# Allows us to run the script manually
# require File.expand_path('../../../../core/lib/recordandplayback', __FILE__)

# For PRODUCTION
require File.expand_path('../../../lib/recordandplayback', __FILE__)

require 'rubygems'
require 'optimist'
require 'yaml'
require 'json'

opts = Optimist::options do
  opt :meeting_id, "Meeting id to archive", :type => String
end

meeting_id = opts[:meeting_id]

# This script lives in scripts/archive/steps while properties.yaml lives in scripts/
props = YAML::load(File.open('../../core/scripts/averox.yml'))

recording_dir = props['recording_dir']
raw_archive_dir = "#{recording_dir}/raw/#{meeting_id}"
Averox.logger.info("Setting process dir")
Averox.logger.info("setting captions dir")
captions_dir = props['captions_dir']

log_dir = props['log_dir']

target_dir = "#{recording_dir}/process/presentation/#{meeting_id}"

# Generate captions.json for API
def create_api_captions_file(captions_meeting_dir)
  Averox.logger.info("Generating closed captions for API")

  captions = JSON.load(File.new("#{captions_meeting_dir}/captions_playback.json"))
  captions_json = []
  captions.each do |track|
    caption = {}
    caption[:kind] = :captions
    caption[:label] = track['localeName']
    caption[:lang] = track['locale']
    caption[:source] = :live
    captions_json << caption
  end

  File.open("#{captions_meeting_dir}/captions.json", "w") do |f|
    f.write(captions_json.to_json)
  end
end

if not FileTest.directory?(target_dir)

  captions_meeting_dir = "#{captions_dir}/#{meeting_id}"

  FileUtils.mkdir_p "#{log_dir}/presentation"
  logger = Logger.new("#{log_dir}/presentation/process-#{meeting_id}.log", 'daily')
  Averox.logger = logger
  Averox.logger.info("Processing script captions.rb")
  FileUtils.mkdir_p target_dir

  begin
    Averox.logger.info("Generating closed captions")
    FileUtils.mkdir_p captions_meeting_dir
    ret = Averox.exec_ret('utils/gen_webvtt', '-i', raw_archive_dir, '-o', captions_meeting_dir)
    if ret != 0
      raise "Generating closed caption files failed"
    end

    FileUtils.cp("#{captions_meeting_dir}/captions.json", "#{captions_meeting_dir}/captions_playback.json")
    create_api_captions_file(captions_meeting_dir)
    FileUtils.rm "#{captions_meeting_dir}/captions_playback.json"

  rescue Exception => e
    Averox.logger.error(e.message)
    e.backtrace.each do |traceline|
      Averox.logger.error(traceline)
    end
    exit 1
  end

end
