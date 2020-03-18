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

import javax.servlet.http.HttpServlet;
import org.eclipse.basyx.vab.directory.proxy.VABDirectoryProxy;
import org.eclipse.basyx.vab.manager.VABConnectionManager;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.modelprovider.map.VABMapProvider;
import org.eclipse.basyx.vab.protocol.basyx.connector.BaSyxConnectorProvider;
import org.eclipse.basyx.vab.protocol.http.server.AASHTTPServer;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import basyx.distributed.BasyxStarter;
import picocli.CommandLine;

public class OCCStarter extends BasyxStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OCCStarter.class);

    public OCCStarter() {
        this.port = 8082;
    }

    @Override
    public void run() {
        // At the connected site, no direct access to the model is possible
        // Every access is done through the network infrastructure

        // The Virtual Automation Bus hides network details to the connected site. Only
        // the endpoint of the
        // directory has to be known:
        String directoryUrl =
                String.format("http://%s:%d%s/", this.directoryHostname, this.directoryPort, this.directoryContextRoot);
        VABDirectoryProxy directoryProxy = new VABDirectoryProxy(directoryUrl);
        LOGGER.info("Connected to directory {}", directoryUrl);

        // The connection manager is responsible for resolving every connection attempt
        // For this, it needs:
        // - The directory at which all models are registered
        // - A provider for different types of network protocols (in this example, only
        // HTTP-REST)
        VABConnectionManager connectionManager = new VABConnectionManager(directoryProxy, new BaSyxConnectorProvider());

        OvenControlComponent controlComponent = new OvenControlComponent(connectionManager);

        // Like the VAB model created before, the structure of the control component is
        // a Map
        // Map ccModel = (Map) cc;

        // Create a server for the Control Component and provide it in the VAB (at port
        // 4002)
        HttpServlet modelServlet = new VABHTTPInterface<IModelProvider>(new VABMapProvider(controlComponent));
        LOGGER.info("Created a servlet for the oven model");
        // This time, a BaSyx-specific TCP interface is used.
        // Likewise, it is also possible to wrap the control component using a http
        // servlet as before
        BaSyxContext context = new BaSyxContext(this.contextRoot, "", this.hostname, this.port);
        // => Every servlet contained in this context is available at
        // http://localhost:4002/handson/
        context.addServletMapping("/oven/controller/*", modelServlet);
        // The model will be available at http://localhost:4003/handson/oven/controller/

        String controllerUrl =
                String.format("http://%s:%d%s/oven/controller/", this.hostname, this.port, this.contextRoot);
        directoryProxy.addMapping("ovenController", controllerUrl);
        LOGGER.info("Registered ovenController as {}", controllerUrl);

        AASHTTPServer server = new AASHTTPServer(context);
        server.start();
        LOGGER.info("HTTP server started");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.shutdown();
            }
        });
    }

    public static void main(String... args) {
        new CommandLine(new OCCStarter()).execute(args);
    }
}
