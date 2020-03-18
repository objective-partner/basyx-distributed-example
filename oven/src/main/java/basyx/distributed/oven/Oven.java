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
 * Oven containing a heater and a temperature sensor
 */
public class Oven {
	private Heater heater;
	private TemperatureSensor sensor;
 
	private static Oven singletonInstance; 
 
	private Oven() {
		heater = new Heater();
		sensor = new TemperatureSensor(heater);
	}
	
	public static synchronized Oven getInstance() {
		if (Oven.singletonInstance == null) {
			Oven.singletonInstance = new Oven();
		}
		return Oven.singletonInstance;
	}
	
 
	public Heater getHeater() {
		return heater;
	}
 
	public TemperatureSensor getSensor() {
		return sensor;
	}
}
