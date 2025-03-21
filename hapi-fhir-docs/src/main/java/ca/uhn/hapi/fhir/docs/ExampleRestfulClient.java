/*-
 * #%L
 * HAPI FHIR - Docs
 * %%
 * Copyright (C) 2014 - 2025 Smile CDR, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package ca.uhn.hapi.fhir.docs;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;

import java.util.List;

@SuppressWarnings("unused")
public class ExampleRestfulClient {

	// START SNIPPET: client
	public static void main(String[] args) {
		FhirContext ctx = FhirContext.forDstu2();
		String serverBase = "http://foo.com/fhirServerBase";

		// Create the client
		IRestfulClient client = ctx.newRestfulClient(IRestfulClient.class, serverBase);

		// Try the client out! This method will invoke the server
		List<Patient> patients = client.getPatient(new StringType("SMITH"));
	}
	// END SNIPPET: client

}
