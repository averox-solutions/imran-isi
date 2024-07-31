# frozen_string_literal: true

# Copyright © 2019 Averox Inc. and by respective authors.
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

# Set up the load path for requiring recordandplayback modules
$LOAD_PATH.unshift(__dir__) unless $LOAD_PATH.include?(__dir__)
