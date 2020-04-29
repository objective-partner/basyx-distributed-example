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

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;

import javax.xml.transform.stream.StreamResult;

import org.eclipse.basyx.aas.factory.xml.MetamodelToXMLConverter;
import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.connected.ConnectedAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.aas.registration.api.IAASRegistryService;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IdentifierType;
import org.eclipse.basyx.submodel.metamodel.map.SubModel;
import org.eclipse.basyx.submodel.metamodel.map.qualifier.LangString;
import org.eclipse.basyx.submodel.metamodel.map.qualifier.LangStrings;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetypedef.PropertyValueTypeDef;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.OperationHelper;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.OperationVariable;
import org.eclipse.basyx.vab.protocol.api.IConnectorProvider;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AasXmlProviderSubModelFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(AasXmlProviderSubModelFactory.class);

	public static SubModel createInstance(final IAASRegistryService registry) {

		SubModel aasXmlExporterSubModel = new SubModel();
		aasXmlExporterSubModel.setIdShort("XmlExporter");
		aasXmlExporterSubModel.setIdentification(IdentifierType.CUSTOM, "basyx.distributed.oven:submodel:xmlexporter:v0.0.1");
		
		Operation operation = new Operation();
		operation.setIdShort("aasToXml");
		
		LangStrings operationDescription = new LangStrings();
		operationDescription.add(new LangString("en-en", "Takes a ModelUrn, fetches corresponding object from registry and returns it as XML."));
		operation.setDescription(operationDescription);

		Function<Object[], Object> xmlExporterInvokable = params -> {
			IConnectorProvider connectorProvider = new HTTPConnectorProvider();
			ConnectedAssetAdministrationShellManager manager =
					new ConnectedAssetAdministrationShellManager(registry, connectorProvider);
			
			ConnectedAssetAdministrationShell connectedAAS;
			String rawUrnString = params[0].toString();
			String urn = rawUrnString;
			try {
				// The ID of the oven AAS
				ModelUrn aasURN = new ModelUrn(urn);
				connectedAAS = manager.retrieveAAS(aasURN);
			} catch (Exception e) {
				String msg = "Failed to fetch AAS with urn " + urn + " from registry";
				LOGGER.error(msg, e);
				throw new RuntimeException(msg, e);
			}
			
			
			
			try {
				StringWriter resultWithoutTypes = new StringWriter();
				MetamodelToXMLConverter.convertToXML(Arrays.asList(connectedAAS), Collections.emptyList(), Collections.emptyList(), connectedAAS.getSubModels().values(), new StreamResult(resultWithoutTypes));
				return resultWithoutTypes.toString();
			} catch (Exception e) {
				String msg = "Failed to transform AAS with urn " + urn + " to XML";
				LOGGER.error(msg, e);
				throw new RuntimeException(msg, e);
			}
		};
		operation.setInvocable(xmlExporterInvokable);
		
		OperationVariable urnVariable = new OperationVariable(OperationHelper.createPropertyTemplate(PropertyValueTypeDef.String));
		operation.setInputVariables(Arrays.asList(urnVariable));
		OperationVariable returnVariable = new OperationVariable(OperationHelper.createPropertyTemplate(PropertyValueTypeDef.String));
		operation.setOutputVariables(Arrays.asList(returnVariable));
		
		aasXmlExporterSubModel.addSubModelElement(operation);
		
		return aasXmlExporterSubModel;
	}
}
