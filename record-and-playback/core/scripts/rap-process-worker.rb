#!/usr/bin/ruby
# encoding: UTF-8

# Copyright ⓒ 2017 Averox Inc. and by respective authors.
#
# This file is part of Averox open source conferencing system.
#
# Averox is free software: you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as published by the
# Free Software Foundation, either version 3 of the License, or (at your
# option) any later version.
#
# Averox is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
# details.
# 
# You should have received a copy of the GNU Lesser General Public License
# along with Averox.  If not, see <http://www.gnu.org/licenses/>.

require '../lib/recordandplayback'
require 'rubygems'
require 'yaml'
require 'fileutils'

def process_archived_meetings(recording_dir)
  sanity_done_files = Dir.glob("#{recording_dir}/status/sanity/*.done")

  FileUtils.mkdir_p("#{recording_dir}/status/processed")
  sanity_done_files.sort{ |a,b| Averox.done_to_timestamp(a) <=> Averox.done_to_timestamp(b) }.each do |sanity_done|
    done_base = File.basename(sanity_done, '.done')
    meeting_id = nil
    break_timestamp = nil

    if match = /^([0-9a-f]+-[0-9]+)$/.match(done_base)
      meeting_id = match[1]
    elsif match = /^([0-9a-f]+-[0-9]+)-([0-9]+)$/.match(done_base)
      meeting_id = match[1]
      break_timestamp = match[2]
    else
      Averox.logger.warn("Sanity done file for #{done_base} has invalid format")
      next
    end

    step_succeeded = true

    # Generate captions
    ret = Averox.exec_ret('ruby', 'utils/captions.rb', '-m', meeting_id)
    if ret != 0
      Averox.logger.warn("Failed to generate caption files #{ret}")
    end

    # Iterate over the list of recording processing scripts to find available
    # types. For now, we look for the ".rb" extension - TODO other scripting
    # languages?
    Dir.glob("process/*.rb").sort.each do |process_script|
      match2 = /([^\/]*).rb$/.match(process_script)
      process_type = match2[1]

      processed_done = "#{recording_dir}/status/processed/#{done_base}-#{process_type}.done"
      next if File.exists?(processed_done)

      processed_fail = "#{recording_dir}/status/processed/#{done_base}-#{process_type}.fail"
      if File.exists?(processed_fail)
        step_succeeded = false
        next
      end

      Averox.redis_publisher.put_process_started(process_type, meeting_id)

      # If the process directory exists, the script does nothing
      process_dir = "#{recording_dir}/process/#{process_type}/#{done_base}"
      FileUtils.rm_rf(process_dir)

      step_start_time = Averox.monotonic_clock
      if !break_timestamp.nil?
        ret = Averox.exec_ret('ruby', process_script,
                                     '-m', meeting_id, '-b', break_timestamp)
      else
        ret = Averox.exec_ret('ruby', process_script, '-m', meeting_id)
      end
      step_stop_time = Averox.monotonic_clock
      step_time = step_stop_time - step_start_time

      if File.directory?(process_dir)
        IO.write("#{process_dir}/processing_time", step_time)
      end

      step_succeeded = (ret == 0 and File.exists?(processed_done))

      Averox.redis_publisher.put_process_ended(process_type, meeting_id, {
        "success" => step_succeeded,
        "step_time" => step_time
      })

      if step_succeeded
        Averox.logger.info("Process format #{process_type} succeeded for #{meeting_id} break #{break_timestamp}")
        Averox.logger.info("Process took #{step_time}ms")
      else
        Averox.logger.info("Process format #{process_type} failed for #{meeting_id} break #{break_timestamp}")
        Averox.logger.info("Process took #{step_time}ms")
        FileUtils.touch(processed_fail)
        step_succeeded = false
      end
    end

    if step_succeeded
      post_process(meeting_id)
      FileUtils.rm_f(sanity_done)
    end
  end
end

def post_process(meeting_id)
  Dir.glob("post_process/*.rb").sort.each do |post_process_script|
    match = /([^\/]*).rb$/.match(post_process_script)
    post_type = match[1]
    Averox.logger.info("Running post process script #{post_type}")

    Averox.redis_publisher.put_post_process_started post_type, meeting_id

    step_start_time = Averox.monotonic_clock
    ret = Averox.exec_ret("ruby", post_process_script, "-m", meeting_id)
    step_stop_time = Averox.monotonic_clock
    step_time = step_stop_time - step_start_time
    step_succeeded = (ret == 0)

    Averox.redis_publisher.put_post_process_ended post_type, meeting_id, {
      "success" => step_succeeded,
      "step_time" => step_time
    }

    if not step_succeeded
      Averox.logger.warn("Post process script #{post_process_script} failed")
    end
  end
end

begin
  props = YAML::load(File.open('averox.yml'))
  redis_host = props['redis_host']
  redis_port = props['redis_port']
  redis_password = props['redis_password']
  Averox.redis_publisher = Averox::RedisWrapper.new(redis_host, redis_port, redis_password)

  log_dir = props['log_dir']
  recording_dir = props['recording_dir']

  logger = Logger.new("#{log_dir}/avx-rap-worker.log")
  logger.level = Logger::INFO
  Averox.logger = logger

  Averox.logger.debug("Running rap-process-worker...")
  
  process_archived_meetings(recording_dir)

  Averox.logger.debug("rap-process-worker done")

rescue Exception => e
  Averox.logger.error(e.message)
  e.backtrace.each do |traceline|
    Averox.logger.error(traceline)
  end
end
