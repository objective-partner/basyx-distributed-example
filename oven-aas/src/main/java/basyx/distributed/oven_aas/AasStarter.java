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

import javax.servlet.http.HttpServlet;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.registration.api.IAASRegistryService;
import org.eclipse.basyx.aas.registration.memory.InMemoryRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.aas.registration.restapi.DirectoryModelProvider;
import org.eclipse.basyx.aas.restapi.AASModelProvider;
import org.eclipse.basyx.aas.restapi.VABMultiSubmodelProvider;
import org.eclipse.basyx.submodel.metamodel.map.SubModel;
import org.eclipse.basyx.submodel.restapi.SubModelProvider;
import org.eclipse.basyx.vab.directory.proxy.VABDirectoryProxy;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.http.server.AASHTTPServer;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import basyx.distributed.BasyxStarter;
import picocli.CommandLine;

public class AasStarter extends BasyxStarter {


  // Initializes a logger for the output
  private static final Logger LOGGER = LoggerFactory.getLogger(AasStarter.class);

  public AasStarter() {
    this.contextRoot = "/handson";
    this.port = 8080;
  }

  @Override
  public void run() {

    String directoryUrl = String.format("http://%s:%d%s/", this.directoryHostname, this.directoryPort, this.directoryContextRoot);
    LOGGER.info("Connecting to directory: {}", directoryUrl);
    VABDirectoryProxy directory = new VABDirectoryProxy(directoryUrl);

    // For this HandsOn, create an InMemoryRegistry for registering the AAS
    IAASRegistryService registry = new InMemoryRegistry();
    IModelProvider registryProvider = new DirectoryModelProvider(registry);
    HttpServlet registryServlet = new VABHTTPInterface<IModelProvider>(registryProvider);

    IAASRegistryService registryProxy =
        new AASRegistryProxy("http://" + this.hostname + ":" + this.port + this.contextRoot + "/registry/api/v1/registry/");

    SubModel sensorSubModel = TemperatureSensorSubModelFactory.createInstance(directory);
    SubModel heaterSubModel = OvenControlSubModelFactory.createInstance(directory);
    SubModel aasXmlConverterSubModel = AasXmlProviderSubModelFactory.createInstance(registryProxy);

    /**
     * Minimal AAS Information
     */
    AssetAdministrationShell aas = new AssetAdministrationShell();
    // Add a unique Identification and a name (== short ID)
    ModelUrn aasURN = new ModelUrn("de.FHG", "devices.es.iese", "AAS", "1.0", "1", "oven01", "001");
    aas.setIdentification(aasURN);
    aas.setIdShort("oven");
    // Note: The submodels are not directly integrated into the AAS model. This makes it possible to
    // distribute
    // submodels to different nodes
    // The header contains references to the previously created submodels.
    // Here, the submodel endpoints are not yet known. They can be specified as soon as the real
    // endpoints are known

    /**
     * Again: Wrap the model in an IModelProvider (now specific to the AAS and submodel)
     */
    // AASModelProvider and SubModelProvider implement the IModelProvider interface
    AASModelProvider aasProvider = new AASModelProvider(aas);
    SubModelProvider sensorSMProvider = new SubModelProvider(sensorSubModel);
    SubModelProvider heaterSMProvider = new SubModelProvider(heaterSubModel);
    SubModelProvider xmlSMProvider = new SubModelProvider(aasXmlConverterSubModel);

    // Add the independent providers to the MultiSubmodelProvider that can be deployed on a single node
    VABMultiSubmodelProvider fullProvider = new VABMultiSubmodelProvider();
    fullProvider.setAssetAdministrationShell(aasProvider);
    fullProvider.addSubmodel("Sensor", sensorSMProvider);
    fullProvider.addSubmodel("Control", heaterSMProvider);
    fullProvider.addSubmodel("XmlExporter", xmlSMProvider);

    // Although the providers for aas/submodels implement the AAS API, they are still IModelProviders!
    // IModelProvider aasIModelProvider = fullProvider;

    /**
     * Deployment
     */
    // Now, the IModelProvider is given to a HTTP servlet that gives access to the model in the next
    // steps
    // => The model will be published using an HTTP-REST interface
    HttpServlet aasServlet = new VABHTTPInterface<IModelProvider>(fullProvider);


    // now add the references of the submodels to the AAS header
    AASDescriptor aasDescriptor =
        new AASDescriptor(aas, String.format("http://%s:%d%s/oven/aas", this.hostname, this.port, this.contextRoot));
    aasDescriptor.addSubmodelDescriptor(new SubmodelDescriptor(sensorSubModel,
        String.format("http://%s:%d%s/oven/aas/submodels/Sensor/submodel", this.hostname, this.port, this.contextRoot)));
    aasDescriptor.addSubmodelDescriptor(new SubmodelDescriptor(heaterSubModel,
        String.format("http://%s:%d%s/oven/aas/submodels/Control/submodel", this.hostname, this.port, this.contextRoot)));
    aasDescriptor.addSubmodelDescriptor(new SubmodelDescriptor(aasXmlConverterSubModel,
        String.format("http://%s:%d%s/oven/aas/submodels/XmlExporter/submodel", this.hostname, this.port, this.contextRoot)));

    // Register the VAB model at the directory (locally in this case)
    registry.register(aasDescriptor);

    // Deploy the AAS on a HTTP server
    BaSyxContext context = new BaSyxContext(this.contextRoot, "", this.hostname, this.port);
    context.addServletMapping("/oven/*", aasServlet);
    context.addServletMapping("/registry/*", registryServlet);
    AASHTTPServer httpServer = new AASHTTPServer(context);

    httpServer.start();

    // To get registry content listing, open this URL:
    // http://localhost:4000/handson/registry/api/v1/registry/

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        httpServer.shutdown();
      }
    });

  }

  public static void main(String... args) {
    new CommandLine(new AasStarter()).execute(args);
  }

}
