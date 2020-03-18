package basyx.distributed.directory_vab;

/*-
 * #%L
 * basyx-distributed-example-directory-vab
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


import org.eclipse.basyx.vab.directory.proxy.VABDirectoryProxy;
import org.eclipse.basyx.vab.manager.VABConnectionManager;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.basyx.connector.BaSyxConnectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
public class VABDirectoryRunner implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(VABDirectoryRunner.class);

  @Option(names = {"-dc", "--directoryContextRoot"},
      description = "ContextRoot of directory / registry, like '/handson' or blank")
  private String directoryContextRoot = "";

  @Option(names = {"-dh", "--directoryHostname"}, description = "Hostname of directory / registry like 'localhost'")
  private String directoryHostname = "localhost";

  @Option(names = {"-dp", "--directoryPort"}, description = "Port of directory / registry like 4000")
  private int directoryPort = 4000;

  @Override
  public void run() {
    // At the connected site, no direct access to the model is possible
    // Every access is done through the network infrastructure

    // The Virtual Automation Bus hides network details to the connected site. Only the endpoint of the
    // directory has to be known:
    VABDirectoryProxy directoryProxy = new VABDirectoryProxy(
        String.format("http://%s:%d/%s", this.directoryHostname, this.directoryPort, this.directoryContextRoot));

    // The connection manager is responsible for resolving every connection attempt
    // For this, it needs:
    // - The directory at which all models are registered
    // - A provider for different types of network protocols (in this example, only HTTP-REST)
    VABConnectionManager connectionManager = new VABConnectionManager(directoryProxy, new BaSyxConnectorProvider());


    // It is now one line of code to retrieve a model provider for any registered
    // model in the network
    // IModelProvider connectedOven = connectionManager.connectToVABElement("oven").get();
    IModelProvider connectedOven = connectionManager.connectToVABElement("oven");

    try {
      // Now, implement a simple a simple bang-bang controller as it has been done in the first HandsOn
      for (int i = 0; i < 100; i++) {
        // Pause for 100ms
        Thread.sleep(100);

        // Retrieve the current temperature from the model provider
        double temperature = (double) connectedOven.getModelPropertyValue("/properties/temperature");
        LOGGER.info("Current temperature: " + temperature);

        // Turn the oven on/off, depending on the defined temperature range
        if (temperature > 40) {
          connectedOven.invokeOperation("/operations/deactivateOven");
        } else if (temperature < 30) {
          connectedOven.invokeOperation("/operations/activateOven");
        }
      }
    } catch (Exception e) {
      LOGGER.error("Processing failed", e);
      throw new RuntimeException("Processing failed", e);
    }
  }

  public static void main(String... args) {
    new CommandLine(new VABDirectoryRunner()).execute(args);
  }

}
