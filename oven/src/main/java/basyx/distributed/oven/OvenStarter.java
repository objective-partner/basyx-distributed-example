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

import java.util.Map;
import org.eclipse.basyx.vab.registry.proxy.VABRegistryProxy;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.modelprovider.lambda.VABLambdaProvider;
import org.eclipse.basyx.vab.protocol.basyx.server.BaSyxTCPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import basyx.distributed.BasyxStarter;
import picocli.CommandLine;


public class OvenStarter extends BasyxStarter {

    // Initializes a logger for the output
    private static final Logger logger = LoggerFactory.getLogger(OvenStarter.class);

    public OvenStarter() {
        this.port = 4001;
    }

    @Override
    public void run() {

        // Create the virtual oven specific to this HandsOn
        Oven oven = Oven.getInstance();

        // First, a local model is created that is wrapped by a model provider (see
        // first HandsOn)
        Map<String, Object> model = OvenModel.createModel(oven);
        IModelProvider modelProvider = new VABLambdaProvider(model);
        // Up to this point, everything is known from the previous HandsOn

        // The Virtual Automation Bus hides network details to the connected site. Only
        // the endpoint of the
        // directory has to be known:
        String directoryUrl =
                String.format("http://%s:%d%s", this.directoryHostname, this.directoryPort, this.directoryContextRoot);
        VABRegistryProxy directory = new VABRegistryProxy(directoryUrl);
        logger.info("Connected to VAB directory " + directoryUrl);

        // VABConnectionManager connectionManager = new VABConnectionManager(directory,
        // new BaSyxConnectorProvider());

        // Register the VAB model at the directory (locally in this case)
        String endpointUrl = String.format("basyx://%s:%d%s", this.hostname, this.port, this.contextRoot);
        directory.addMapping("oven", endpointUrl);
        logger.info(String.format("Oven model registered as '%s'!", endpointUrl));

        // Now, define a context to which multiple servlets can be added
        BaSyxTCPServer<IModelProvider> server = new BaSyxTCPServer<>(modelProvider, this.port);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.stop();
            }
        });
    }

    public static void main(String... args) {
        new CommandLine(new OvenStarter()).execute(args);
    }
}
