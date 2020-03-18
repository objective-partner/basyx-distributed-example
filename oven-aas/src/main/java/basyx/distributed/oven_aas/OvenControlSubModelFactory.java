package basyx.distributed.oven_aas;

/*-
 * #%L
 * basyx-distributed-example-oven-aas
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

import java.util.function.Function;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.submodel.metamodel.map.SubModel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation;
import org.eclipse.basyx.vab.directory.proxy.VABDirectoryProxy;
import org.eclipse.basyx.vab.manager.VABConnectionManager;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OvenControlSubModelFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(OvenControlSubModelFactory.class);

  public static SubModel createInstance(VABDirectoryProxy directoryProxy) {

    VABConnectionManager connectionManager = new VABConnectionManager(directoryProxy, new HTTPConnectorProvider());
    VABElementProxy element = connectionManager.connectToVABElement("ovenController");
    for (int i = 0; i < 10 && element == null; i++) {
      element = connectionManager.connectToVABElement("ovenController");
      try {
        Thread.sleep(250);
      } catch (InterruptedException e) {
        // Doesn't change anything - ignore it...
      }
    }
    IModelProvider connectedOvenControlComponent = element;

    SubModel heaterSubModel = new SubModel();
    heaterSubModel.setIdShort("Control");
    // Create an operation that uses the control component to set a temperature value
    Function<Object[], Object> heatInvokable = (params) -> {
      // Select the operation from the control component
      try {
        connectedOvenControlComponent.setModelPropertyValue("status/opMode",
            "HEAT"/* OvenControlComponent.OPMODE_HEAT */);

        // Start the control component operation asynchronous
        connectedOvenControlComponent.invokeOperation("/operations/service/start");

        // Wait until the operation is completed
        while (!connectedOvenControlComponent.getModelPropertyValue("status/exState")
            .equals(ExecutionState.COMPLETE.getValue())) {
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
          }
        }

        connectedOvenControlComponent.invokeOperation("operations/service/reset");
      } catch (Exception e) {
        LOGGER.error("Something failed - giving up", e);
      }
      // Then return -> synchronous
      return null;
    };

    // Create the Operation
    Operation operation = new Operation();
    operation.setIdShort("controlTemperature");
    operation.setInvocable(heatInvokable);
    heaterSubModel.addSubModelElement(operation);
    return heaterSubModel;
  }
}
