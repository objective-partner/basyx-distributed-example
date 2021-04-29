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

import org.eclipse.basyx.vab.registry.restapi.VABRegistryModelProvider;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxHTTPServer;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;
import basyx.distributed.BasyxStarter;
import picocli.CommandLine;

public class VABDirectoryStarter extends BasyxStarter {

    public VABDirectoryStarter() {
        this.port = 8081;
    }


    @Override
    public void run() {
        // Ein Servlet aufbauen und das Directory einbinden
        BaSyxContext context = new BaSyxContext(this.directoryContextRoot, this.directoryContextRoot,
                this.directoryHostname, this.directoryPort);
        context.addServletMapping("/*", new VABHTTPInterface<IModelProvider>(new VABRegistryModelProvider()));

        // Das Servlet auf einem Server ablegen und Server starten
        BaSyxHTTPServer registryServer = new BaSyxHTTPServer(context);
        registryServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                registryServer.shutdown();
            }
        });
    }

    public static void main(String... args) {
        new CommandLine(new VABDirectoryStarter()).execute(args);
    }
}
