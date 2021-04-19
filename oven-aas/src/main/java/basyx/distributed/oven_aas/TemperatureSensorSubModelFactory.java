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

import java.lang.reflect.Field;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IdentifierType;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyElements;
import org.eclipse.basyx.submodel.metamodel.api.reference.enums.KeyType;
import org.eclipse.basyx.submodel.metamodel.map.SubModel;
import org.eclipse.basyx.submodel.metamodel.map.reference.Key;
import org.eclipse.basyx.submodel.metamodel.map.reference.Reference;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetypedef.PropertyValueTypeDef;
import org.eclipse.basyx.vab.directory.proxy.VABDirectoryProxy;
import org.eclipse.basyx.vab.manager.VABConnectionManager;
import org.eclipse.basyx.vab.modelprovider.VABElementProxy;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.modelprovider.lambda.VABLambdaProviderHelper;
import org.eclipse.basyx.vab.protocol.basyx.connector.BaSyxConnectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemperatureSensorSubModelFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(TemperatureSensorSubModelFactory.class);

  public static SubModel createInstance(VABDirectoryProxy directoryProxy) {

    VABConnectionManager connectionManager = new VABConnectionManager(directoryProxy, new BaSyxConnectorProvider());
    VABElementProxy element = connectionManager.connectToVABElement("oven");
    for (int i = 0; i < 10 && element == null; i++) {
      element = connectionManager.connectToVABElement("oven");
      try {
        Thread.sleep(250);
      } catch (InterruptedException e) {
        // Doesn't change anything - ignore it...
      }
    }
    if (element == null) {
      LOGGER.error("Failed to connect to oven!");
    }
    IModelProvider connectedOven = element;

    SubModel sensorSubModel = new SubModel();
    sensorSubModel.setIdShort("Sensor");
    sensorSubModel.setIdentification(IdentifierType.CUSTOM, "basyx.distributed.oven:submodel:sensor:v0.0.1");
    // Create a lambda property containing the current sensor temperature
    Property temperatureProperty = new Property();
    temperatureProperty.setIdShort("currentTemperature");
    temperatureProperty.set(VABLambdaProviderHelper.createSimple(() -> {
      LOGGER.debug("Requesting temperature");
      Double temperature = null;
      try {
        temperature = (Double) connectedOven.getModelPropertyValue("/properties/temperature");
      } catch (Exception eRemoteCall) {
        String addr = "<failed to get value by reflection>";
        try {
          Field addrField = VABElementProxy.class.getDeclaredField("addr");
          addrField.setAccessible(true);
          addr = addrField.get(connectedOven).toString();
        } catch (Exception eIgnored) {
          // ignore - debug code...
        }
        LOGGER.error("Failed to get temperature from remote oven: " + addr, eRemoteCall);
      }
      return temperature;
    }, null), PropertyValueTypeDef.Double);
    temperatureProperty.setSemanticID(new Reference(new Key(KeyElements.PROPERTY, false, "0173-1#02-AAV232#002", KeyType.IRDI)));
    sensorSubModel.addSubModelElement(temperatureProperty);

    Property temperatureUnit = new Property("Fahrenheit");
    temperatureUnit.setIdShort("temperatureUnit");
    sensorSubModel.addSubModelElement(temperatureUnit);
    return sensorSubModel;
  }
}
