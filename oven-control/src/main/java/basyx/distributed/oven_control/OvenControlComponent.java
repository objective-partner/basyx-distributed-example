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

import org.eclipse.basyx.models.controlcomponent.ControlComponentChangeListener;
import org.eclipse.basyx.models.controlcomponent.ExecutionMode;
import org.eclipse.basyx.models.controlcomponent.ExecutionState;
import org.eclipse.basyx.models.controlcomponent.OccupationState;
import org.eclipse.basyx.models.controlcomponent.SimpleControlComponent;
import org.eclipse.basyx.vab.manager.VABConnectionManager;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Control Component for controlling the oven. Has an additional operation mode named HEAT. This is
 * a "black-box" example for a control component for the HandsOn.
 */
public class OvenControlComponent extends SimpleControlComponent implements ControlComponentChangeListener {
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(OvenControlComponent.class);

  public static final String OPMODE_BASIC = "BSTATE";
  public static final String OPMODE_HEAT = "HEAT";

  private VABConnectionManager connectionManager;

  public OvenControlComponent(VABConnectionManager connectionManager) {

    this.connectionManager = connectionManager;

    addControlComponentChangeListener(this);
  }

  @Override
  public void onChangedExecutionState(ExecutionState newExecutionState) {
    LOGGER.info("OvenControlComponent: new execution state: " + newExecutionState);
    if (newExecutionState == ExecutionState.EXECUTE) {
      if (this.getOperationMode().equals(OPMODE_HEAT)) {
        controlHeater();
      }
    } else if (newExecutionState == ExecutionState.COMPLETE) {
      this.setOperationMode(OPMODE_BASIC);
    }
  }

  protected void controlHeater() {
    new Thread(() -> {
      LOGGER.info("Starting heater control");
      // IModelProvider connectedOven = connectionManager.connectToVABElement("oven").get();
      IModelProvider connectedOven = connectionManager.connectToVABElement("oven");
      try {
        for (int i = 0; i < 50; i++) {

          // Retrieve the current temperature from the model provider
          double temperature = (double) connectedOven.getModelPropertyValue("/properties/temperature");
          LOGGER.info("Current temperature: " + temperature);

          if (temperature < 30.0d) {
            connectedOven.invokeOperation("/operations/activateOven");
          } else if (temperature > 40) {
            connectedOven.invokeOperation("/operations/deactivateOven");
          }
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            LOGGER.warn("Normal process was interrupted", e);
            break;
          }
        }
        connectedOven.invokeOperation("/operations/deactivateOven");
      } catch (Exception e) {
        LOGGER.error("Communication to oven is broken", e);
      }
      setExecutionState(ExecutionState.COMPLETE.getValue());
    }).start();
  }

  @Override
  public void onVariableChange(String varName, Object newValue) {}

  @Override
  public void onNewOccupier(String occupierId) {}

  @Override
  public void onNewOccupationState(OccupationState state) {}

  @Override
  public void onChangedExecutionMode(ExecutionMode newExecutionMode) {}

  @Override
  public void onChangedOperationMode(String newOperationMode) {}

  @Override
  public void onChangedWorkState(String newWorkState) {}

  @Override
  public void onChangedErrorState(String newWorkState) {}
}
