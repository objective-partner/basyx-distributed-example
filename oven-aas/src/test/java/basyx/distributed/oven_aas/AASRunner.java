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

import java.util.Map;
import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.connected.ConnectedAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.aas.registration.api.IAASRegistryService;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.ISubModel;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IDataElement;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.property.ISingleProperty;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;
import org.eclipse.basyx.vab.protocol.api.IConnectorProvider;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AASRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(AASRunner.class);

  public static void main(String[] args) throws Exception {
    // Return a AASHTTPRegistryProxy for the registry on localhost at port 4000
    IAASRegistryService registry = new AASRegistryProxy("http://localhost:8080/handson/registry/api/v1/registry/");
//    IAASRegistryService registry = new AASRegistryProxy("http://oven-aas-basyx-distributed-example.apps-crc.testing/handson/registry/api/v1/registry/");
//    IAASRegistryService registry = new AASRegistryProxy("http://basyx-distributed-example.apps-crc.testing/handson/registry/api/v1/registry/");

    // Create a ConnectedAssetAdministrationShell using a
    // ConnectedAssetAdministrationShellManager
    IConnectorProvider connectorProvider = new HTTPConnectorProvider();
    ConnectedAssetAdministrationShellManager manager =
        new ConnectedAssetAdministrationShellManager(registry, connectorProvider);

    // The ID of the oven AAS
    ModelUrn aasURN = new ModelUrn("de.FHG", "devices.es.iese", "AAS", "1.0", "1", "oven01", "001");
    ConnectedAssetAdministrationShell connectedAAS = manager.retrieveAAS(aasURN);


    // Connect to the AAS and read the current temperature
    // Either Create a connected property using the connected facades
    Map<String, ISubModel> submodels = connectedAAS.getSubModels();

    
    ISubModel xmlConverterSM = submodels.get("XmlExporter");
    IOperation aasToXmlOperation = xmlConverterSM.getOperations().get("aasToXml");
    Object result = aasToXmlOperation.invoke(aasURN.getURN());
    LOGGER.debug("AasToXml-Result: " + result);
    

    ISubModel connectedControlSM = submodels.get("Control");
    Map<String, IOperation> operations = connectedControlSM.getOperations();
    IOperation operation = operations.get("controlTemperature");
    operation.invoke();

    ISingleProperty ovenControlAlias = (ISingleProperty) connectedControlSM.getDataElements().get("alias");
    ovenControlAlias.set("heater-in-office");
    ovenControlAlias.get();


    ISubModel connectedSensorSM = submodels.get("Sensor");
    Map<String, IDataElement> properties = connectedSensorSM.getDataElements();
    ISingleProperty temperatureProperty = (ISingleProperty) properties.get("currentTemperature");
    double temperature = (double) temperatureProperty.get();
    // Or get a VABElementProxy to directly query the VAB path of the property
    /*
     * IModelProvider providerProxy = connectedAAS.getProxy(); String temperatureValuePath =
     * "/submodels/Sensor/dataElements/currentTemperature/value"; Map<String, Object> ret = (Map<String,
     * Object>) providerProxy.getModelPropertyValue(temperatureValuePath); double temperature = (double)
     * ret.get(Property.VALUE);
     */

    // Connect to the AAS and read the current temperature
    // Either use the connected variants:
    ISingleProperty unitProperty = (ISingleProperty) properties.get("temperatureUnit");
    String temperatureUnit = (String) unitProperty.get();
    // Or get a VABElementProxy to directly query the VAB path of the property
    /*
     * String temperatureUnitPath = "/submodels/Sensor/dataElements/temperatureUnit/value"; ret =
     * (Map<String, Object>) providerProxy.getModelPropertyValue(temperatureUnitPath); String
     * temperatureUnit = (String) ret.get(Property.VALUE);
     */

    // Now depending on the semantics of the temperature, calculate the value in °C
    // Usually, these semantics will be stored in a context dictionary that is
    // referenced by the semantic attributes
    // of the property. But this HandsOn demonstrates a simplified scenario.
    if (temperatureUnit.equals("Fahrenheit")) {
      temperature = (temperature - 32.0d) * 5.0d / 9.0d;
    }

    LOGGER.info("The sensor temperature is " + temperature + "°C");
  }
}
