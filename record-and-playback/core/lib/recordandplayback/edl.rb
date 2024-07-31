# encoding: UTF-8

# Averox open source conferencing system - http://www.averox.org/
#
# Copyright (c) 2013 Averox Inc. and by respective authors.
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

require File.expand_path('../edl/video', __FILE__)
require File.expand_path('../edl/audio', __FILE__)

module Averox
  module EDL
    # max_error_rate is set to ignore errors in poorly encoded webcams/deskshare
    FFMPEG = ['ffmpeg', '-y', '-v', 'warning', '-nostats', '-max_error_rate', '1.0']
    FFPROBE = ['ffprobe', '-v', 'warning', '-print_format', 'json', '-show_format', '-show_streams']

    def self.encode(audio, video, format, output_basename, audio_offset = 0)
      output = "#{output_basename}.#{format[:extension]}"
      lastoutput = nil
      format[:parameters].each_with_index do |pass, i|
        Averox.logger.info "Performing video encode pass #{i}"
        lastoutput = "#{output_basename}.encode.#{format[:extension]}"
        ffmpeg_cmd = FFMPEG
        ffmpeg_cmd += ['-i', video] if video
        if audio
          if audio_offset != 0
            ffmpeg_cmd += ['-itsoffset', ms_to_s(audio_offset)]
          end
          # Ensure that the entire contents of freeswitch wav files are read
          if Averox::EDL::Audio.audio_info(audio)[:format][:format_name] == 'wav'
            ffmpeg_cmd += ['-ignore_length', '1']
          end
          ffmpeg_cmd += ['-i', audio]
        end
        ffmpeg_cmd += [*pass, '-passlogfile', output_basename, lastoutput]
        Dir.chdir(File.dirname(output)) do
          exitstatus = Averox.exec_ret(*ffmpeg_cmd)
          if exitstatus != 0
            FileUtils.rm_f(lastoutput) if File.exists?(lastoutput)
            raise "ffmpeg failed, exit code #{exitstatus}"
          end
        end
      end

      # Some formats have post-processing to prepare for streaming
      if format[:postprocess]
        format[:postprocess].each_with_index do |pp, i|
          Averox.logger.info "Performing post-processing step #{i}"
          ppoutput = "#{output_basename}.pp#{i}.#{format[:extension]}"
          cmd = pp.map do |arg|
            case arg
            when ':input'
              lastoutput
            when ':output'
              ppoutput
            else
              arg
            end
          end
          Dir.chdir(File.dirname(output)) do
            exitstatus = Averox.exec_ret(*cmd)
            raise "postprocess failed, exit code #{exitstatus}" if exitstatus != 0
          end
          FileUtils.rm(lastoutput)
          lastoutput = ppoutput
        end
      end

      FileUtils.mv(lastoutput, output)

      return output
    end

    def self.ms_to_s(timestamp)
      s = timestamp / 1000
      ms = timestamp % 1000
      "%d.%03d" % [s, ms]
    end    

  end
end
