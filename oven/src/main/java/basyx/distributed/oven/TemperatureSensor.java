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

/**
 * A sensor for reading a temperature value that is dependent on a heater
 */
public class TemperatureSensor {
	private final double maxTemperature = 50;
	private final double minTemperature = 20;
	private final double changeRate = 0.1d;
 
	private double currentTemperature = 20.0;
 
	public TemperatureSensor(final Heater heater) {
		// Start a new Thread that updates the temperature in every tick
		new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				double targetTemperature = minTemperature;
				if (heater.isActive()) {
					targetTemperature = maxTemperature;
				}
				currentTemperature = (1 - changeRate) * currentTemperature + changeRate * targetTemperature;
			}
		}, "TemperatureSensor").start();
	}
 
	public double readTemperature() {
		return currentTemperature;
	}
}
