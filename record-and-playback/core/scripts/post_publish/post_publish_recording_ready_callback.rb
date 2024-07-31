#!/usr/bin/ruby
# encoding: UTF-8

#
# Averox open source conferencing system - http://www.averox.org/
#
# Copyright (c) 2012 Averox Inc. and by respective authors (see below).
#
# This program is free software; you can redistribute it and/or modify it under
# the terms of the GNU Lesser General Public License as published by the Free
# Software Foundation; either version 3.0 of the License, or (at your option)
# any later version.
#
# Averox is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
# details.
#
# You should have received a copy of the GNU Lesser General Public License along
# with Averox; if not, see <http://www.gnu.org/licenses/>.
#

require "optimist"
require 'net/http'
require "jwt"
require "java_properties"
require File.expand_path('../../../lib/recordandplayback', __FILE__)

logger = Logger.new("/var/log/averox/post_publish.log", 'weekly' )
logger.level = Logger::INFO
Averox.logger = logger

opts = Optimist::options do
  opt :meeting_id, "Meeting id to archive", :type => String
  opt :format, "Playback format name", :type => String
end
meeting_id = opts[:meeting_id]

avx_web_properties = "/etc/averox/avx-web.properties"
events_xml = "/var/averox/recording/raw/#{meeting_id}/events.xml"

def get_metadata(key, meeting_metadata)
  meeting_metadata.key?(key) ? meeting_metadata[key].value : nil
end

def get_callback_url(events_xml)
  meeting_metadata = Averox::Events.get_meeting_metadata(events_xml)

  meta_avx_rec_ready_url = "avx-recording-ready-url"

  callback_url = get_metadata(meta_avx_rec_ready_url, meeting_metadata)

  # For compatibility with some 3rd party implementations, look up for
  # bn-recording-ready-url or canvas-recording-ready, when avx-recording-ready
  # is not included.
  meta_bn_rec_ready_url = "bn-recording-ready-url"
  meta_canvas_rec_ready_url = "canvas-recording-ready-url"

  callback_url ||= get_metadata(meta_bn_rec_ready_url, meeting_metadata)
  callback_url ||= get_metadata(meta_canvas_rec_ready_url, meeting_metadata)

  callback_url
end

#
# Main code
#
Averox.logger.info("Recording Ready Notify for [#{meeting_id}] starts")

begin
  callback_url = get_callback_url(events_xml)

  unless callback_url.nil?
    Averox.logger.info("Making callback for recording ready notification")

    props = JavaProperties::Properties.new(avx_web_properties)
    secret = props[:securitySalt]
    external_meeting_id = Averox::Events.get_external_meeting_id(events_xml)

    payload = { meeting_id: external_meeting_id, record_id: meeting_id }
    payload_encoded = JWT.encode(payload, secret)

    uri = URI.parse(callback_url)
    http = Net::HTTP.new(uri.host, uri.port)
    http.use_ssl = (uri.scheme == 'https')

    Averox.logger.info("Sending request to #{uri.scheme}://#{uri.host}#{uri.request_uri}")
    request = Net::HTTP::Post.new(uri.request_uri)
    request.set_form_data({ signed_parameters: payload_encoded })

    response = http.request(request)
    code = response.code.to_i

    if code == 410
      Averox.logger.info("Notified for deleted meeting: #{meeting_id}")
      # TODO: should we automatically delete the recording here?
    elsif code == 404
      Averox.logger.info("404 error when notifying for recording: #{meeting_id}, ignoring")
    elsif code < 200 || code >= 300
      Averox.logger.info("Callback HTTP request failed: #{response.code} #{response.message} (code #{code})")
    else
      Averox.logger.info("Recording notifier successful: #{meeting_id} (code #{code})")
    end
  end

rescue => e
  Averox.logger.info("Rescued")
  Averox.logger.info(e.to_s)
end

Averox.logger.info("Recording Ready notify ends")

exit 0
