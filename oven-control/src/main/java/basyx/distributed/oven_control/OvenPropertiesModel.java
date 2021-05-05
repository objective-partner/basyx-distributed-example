package basyx.distributed.oven_control;

/*-
 * #%L
 * basyx-distributed-example-oven-control
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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.eclipse.basyx.vab.manager.VABConnectionManager;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.modelprovider.lambda.VABLambdaProviderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OvenPropertiesModel {

  private static final Logger logger = LoggerFactory.getLogger(OvenPropertiesModel.class);

  public static Map<String, Object> createModel(VABConnectionManager connectionManager) {
    /*
     * Pseudo-Code for the intended map structure of the oven model: myModel = new HashMap( properties:
     * new HashMap( id: "heater01", temperature: Dynamic *Lambda-Property* that represents the current
     * oven temperature, ), )
     */

    // Create an empty container for custom properties
    Map<String, Object> properties = new HashMap<>();
    // Add a static element
    properties.put("id", "ovenTemperature01");
    // Now we want to create a dynamic property that can resolve its value during runtime
    // 1. Create a supplier function that can determine the oven temperature using the +sensor
    Supplier<Object> lambdaFunction = () -> {
      // IModelProvider connectedOven = connectionManager.connectToVABElement("oven").get();
      IModelProvider connectedOven = connectionManager.connectToVABElement("oven");
      Double temperature = null;
      try {
        temperature = (Double) connectedOven.getValue("/properties/temperature");
      } catch (Exception e) {
        logger.error("Failed to read temperature", e);
      }
      return temperature;
    };
    // 2. Use a VABLambdaProviderHelper in order to create a lambda property out of that supplier
    // NOTE: A setter function is not required (=> null), because a sensor temperature is "read only"
    Map<String, Object> lambdaProperty = VABLambdaProviderHelper.createSimple(lambdaFunction, null);
    // 3. Add that lambda property to the model exactly like the static property before
    properties.put("temperature", lambdaProperty);

    // Create a root map and return a single model with the created operations and properties
    Map<String, Object> myModel = new HashMap<>();
    myModel.put("properties", properties);
    return myModel;
  }
}
