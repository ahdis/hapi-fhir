package ca.uhn.fhir.jpa.provider.r4;

import java.util.Date;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.UnsignedIntType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import ca.uhn.fhir.jpa.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.dao.IFhirResourceDaoComposition;
import ca.uhn.fhir.jpa.util.JpaConstants;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

/*
 * #%L
 * HAPI FHIR JPA Server
 * %%
 * Copyright (C) 2014 - 2019 University Health Network
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

public class BaseJpaResourceProviderCompositionR4 extends JpaResourceProviderR4<Composition> {

	@Autowired
	@Qualifier("myBundleDaoR4")
	protected IFhirResourceDao<Bundle> myBundleDao;

	/**
	 * Composition/123/$document
	 *
	 * @param theRequestDetails
	 */
	//@formatter:off
	@Operation(name = JpaConstants.OPERATION_DOCUMENT, idempotent = true, bundleType=BundleTypeEnum.DOCUMENT)
	public IBaseBundle getDocumentForComposition(

			javax.servlet.http.HttpServletRequest theServletRequest,

			@IdParam
			IdType theId,

			@Description(formalDefinition="Whether to store the document at the bundle end-point (/Bundle) or not once it is generated. Value = true or false (default is for the server to decide)")
			@OperationParam(name = "persist")
			BooleanType persist,

			@Description(formalDefinition="Results from this method are returned across multiple pages. This parameter controls the size of those pages.")
			@OperationParam(name = Constants.PARAM_COUNT)
			UnsignedIntType theCount,

			@Description(shortDefinition="Only return resources which were last updated as specified by the given range")
			@OperationParam(name = Constants.PARAM_LASTUPDATED, min=0, max=1)
			DateRangeParam theLastUpdated,

			@Sort
			SortSpec theSortSpec,

			RequestDetails theRequestDetails
			) {
		//@formatter:on

		startRequest(theServletRequest);
		try {
			IBundleProvider bundleProvider = ((IFhirResourceDaoComposition<Composition>) getDao()).getDocumentForComposition(theServletRequest, theId, theCount, theLastUpdated, theSortSpec, theRequestDetails);
			if (bundleProvider.size()==0) {
				throw new ResourceNotFoundException(theId);
			}
			List<IBaseResource> resourceList = bundleProvider.getResources(0, bundleProvider.size());
			Bundle bundle = new Bundle().setType(Bundle.BundleType.DOCUMENT);
			String bundleFullUrlBase = theServletRequest.getRequestURL().toString();
			bundleFullUrlBase = bundleFullUrlBase.substring(0,bundleFullUrlBase.lastIndexOf("/Composition")+1);
			
			for (IBaseResource resource : resourceList) {
				if (resource!=null && resource.getIdElement()!=null) {
					Bundle.BundleEntryComponent bundleEntryComponent = new Bundle.BundleEntryComponent();
					bundleEntryComponent.setFullUrl(bundleFullUrlBase + resource.getIdElement().getResourceType()+"/"+resource.getIdElement().getIdPart());
					bundleEntryComponent.setResource((Resource) resource);
					bundle.addEntry(bundleEntryComponent);
				} 
			}
			
			if (persist!=null && persist.booleanValue() && myBundleDao!=null) {
				myBundleDao.create(bundle);
			} else {
				Meta meta = new Meta();
				meta.setLastUpdated(new Date());
				bundle.setMeta(meta);							
			}
			
			return bundle;
		} finally {
			endRequest(theServletRequest);
		}
	}
}
