/**
 * Averox open source conferencing system - http://www.averox.org/
 * 
 * Copyright (c) 2015 Averox Inc. and by respective authors (see below).
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

package org.averox.presentation.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PdfFontType3DetectorHandler extends AbstractCommandHandler {

  private static Logger log = LoggerFactory
      .getLogger(PdfFontType3DetectorHandler.class);

  /**
   *
   * @return If pdf page contains one or more texts with font Type 3.
   */
  public boolean hasFontType3() {
    if (stdoutEquals("1")) {
      return true;
    }

    return false;
  }

}