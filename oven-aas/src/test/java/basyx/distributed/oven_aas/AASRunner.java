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
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IProperty;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;
import org.eclipse.basyx.vab.protocol.api.IConnectorFactory;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AASRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(AASRunner.class);

  public static void main(String[] args) throws Exception {
    // Return a AASHTTPRegistryProxy for the registry on localhost at port 4000
    IAASRegistry registry = new AASRegistryProxy("http://localhost:8080/handson/registry/");
    // IAASRegistryService registry = new
    // AASRegistryProxy("http://oven-aas-basyx-distributed-example.apps-crc.testing/handson/registry/api/v1/registry/");
    // IAASRegistryService registry = new
    // AASRegistryProxy("http://basyx-distributed-example.apps-crc.testing/handson/registry/api/v1/registry/");

    // Create a ConnectedAssetAdministrationShell using a
    // ConnectedAssetAdministrationShellManager
    IConnectorFactory connectorFactory = new HTTPConnectorFactory();
    ConnectedAssetAdministrationShellManager manager =
        new ConnectedAssetAdministrationShellManager(registry, connectorFactory);

    // The ID of the oven AAS
    ModelUrn aasURN = new ModelUrn("de.FHG", "devices.es.iese", "AAS", "1.0", "1", "oven01", "001");
    ConnectedAssetAdministrationShell connectedAAS = manager.retrieveAAS(aasURN);


    // Connect to the AAS and read the current temperature
    // Either Create a connected property using the connected facades
    Map<String, ISubmodel> submodels = connectedAAS.getSubmodels();


    ISubmodel xmlConverterSM = submodels.get("XmlExporter");
    IOperation aasToXmlOperation = xmlConverterSM.getOperations().get("aasToXml");
    Object result = aasToXmlOperation.invoke(aasURN.getURN());
    LOGGER.debug("AasToXml-Result: " + result);


    ISubmodel connectedControlSM = submodels.get("Control");
    Map<String, IOperation> operations = connectedControlSM.getOperations();
    IOperation operation = operations.get("controlTemperature");
    operation.invoke();

    // IProperty ovenControlAlias = (IProperty) connectedControlSM.getSubmodelElements().get("alias");
    // ovenControlAlias.set("heater-in-office");
    // ovenControlAlias.get();


    ISubmodel connectedSensorSM = submodels.get("Sensor");
    Map<String, ISubmodelElement> properties = connectedSensorSM.getSubmodelElements();
    IProperty temperatureProperty = (IProperty) properties.get("currentTemperature");
    double temperature = (double) temperatureProperty.getValue();
    // Or get a VABElementProxy to directly query the VAB path of the property
    /*
     * IModelProvider providerProxy = connectedAAS.getProxy(); String temperatureValuePath =
     * "/submodels/Sensor/dataElements/currentTemperature/value"; Map<String, Object> ret = (Map<String,
     * Object>) providerProxy.getModelPropertyValue(temperatureValuePath); double temperature = (double)
     * ret.get(Property.VALUE);
     */

    // Connect to the AAS and read the current temperature
    // Either use the connected variants:
    IProperty unitProperty = (IProperty) properties.get("temperatureUnit");
    String temperatureUnit = (String) unitProperty.getValue();
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
