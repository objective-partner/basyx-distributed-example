package basyx.distributed;

/*-
 * #%L
 * basyx-distributed-example-starter
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

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


@Command
public abstract class BasyxStarter implements Runnable {

    @Option(names = {"-dc", "--directoryContextRoot"}, fallbackValue = "", arity = "0..1",
            description = "ContextRoot of directory, like '/handson' or blank")
    protected String directoryContextRoot = "";

    @Option(names = {"-dh", "--directoryHostname"}, description = "Hostname of directory like 'localhost'")
    protected String directoryHostname = "localhost";

    @Option(names = {"-dp", "--directoryPort"}, description = "Port of directory like 8080")
    protected int directoryPort = 8081;

    @Option(names = {"-c", "--contextRoot"}, fallbackValue = "", arity = "0..1",
            description = "ContextRoot, like '/handson' or blank")
    protected String contextRoot = "";

    @Option(names = {"-h", "--host"},
            description = "hostname with that the component registers itself in directory / registry")
    protected String hostname = "localhost";

    @Option(names = {"-p", "--port"},
            description = "hostname with that the component registers itself in directory / registry")
    protected int port = -1;

    @Option(names = {"-rc", "--registryContextRoot"}, fallbackValue = "", arity = "0..1",
            description = "ContextRoot of registry / registry, like '/handson' or blank")
    protected String registryContextRoot = "";

    @Option(names = {"-rh", "--registryHostname"}, description = "Hostname of registry like 'localhost'")
    protected String registryHostname = "localhost";

    @Option(names = {"-rp", "--registryPort"}, description = "Port of registry like 4000")
    protected int registryPort = 4000;

}
