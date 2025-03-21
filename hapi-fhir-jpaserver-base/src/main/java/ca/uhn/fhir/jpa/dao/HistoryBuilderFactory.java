/*-
 * #%L
 * HAPI FHIR JPA Server
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
package ca.uhn.fhir.jpa.dao;

import ca.uhn.fhir.jpa.config.JpaConfig;
import ca.uhn.fhir.jpa.model.dao.JpaPid;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Date;

public class HistoryBuilderFactory {

	@Autowired
	private ApplicationContext myApplicationContext;

	public HistoryBuilder newHistoryBuilder(
			@Nullable String theResourceType,
			@Nullable JpaPid theResourceId,
			@Nullable Date theRangeStartInclusive,
			@Nullable Date theRangeEndInclusive) {
		return (HistoryBuilder) myApplicationContext.getBean(
				JpaConfig.HISTORY_BUILDER,
				theResourceType,
				theResourceId,
				theRangeStartInclusive,
				theRangeEndInclusive);
	}
}
