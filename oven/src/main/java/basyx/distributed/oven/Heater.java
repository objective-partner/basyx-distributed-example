package basyx.distributed.oven;

/*-
 * #%L
 * basyx-distributed-example-oven
 * %%
 * Copyright (C) 2020 objective partner AG
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple heater with two states: activated or deactivated
 */
public class Heater {
	private static final Logger LOGGER = LoggerFactory.getLogger(Heater.class);

	private boolean isActive = false;

	public void activate() {
		if (!isActive) {
			LOGGER.info("Heater: activated");
			isActive = true;
		}
	}

	public void deactivate() {
		if (isActive) {
			LOGGER.info("Heater: deactivated");
			isActive = false;
		}
	}

	public boolean isActive() {
		return isActive;
	}
}
