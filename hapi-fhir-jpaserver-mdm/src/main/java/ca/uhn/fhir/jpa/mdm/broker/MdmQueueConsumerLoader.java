/*-
 * #%L
 * HAPI FHIR JPA Server - Master Data Management
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
package ca.uhn.fhir.jpa.mdm.broker;

import ca.uhn.fhir.jpa.subscription.channel.api.ChannelConsumerSettings;
import ca.uhn.fhir.jpa.subscription.channel.api.IChannelFactory;
import ca.uhn.fhir.jpa.subscription.channel.api.IChannelReceiver;
import ca.uhn.fhir.jpa.subscription.model.ResourceModifiedJsonMessage;
import ca.uhn.fhir.mdm.api.IMdmSettings;
import ca.uhn.fhir.mdm.api.MdmModeEnum;
import ca.uhn.fhir.mdm.log.Logs;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class MdmQueueConsumerLoader {
	private static final Logger ourLog = Logs.getMdmTroubleshootingLog();

	private final IChannelFactory myChannelFactory;
	private final IMdmSettings myMdmSettings;
	private final MdmMessageHandler myMdmMessageHandler;

	protected IChannelReceiver myMdmChannel;

	public MdmQueueConsumerLoader(
			IChannelFactory theChannelFactory, IMdmSettings theMdmSettings, MdmMessageHandler theMdmMessageHandler) {
		myChannelFactory = theChannelFactory;
		myMdmSettings = theMdmSettings;
		myMdmMessageHandler = theMdmMessageHandler;

		if (myMdmSettings.getMode() == MdmModeEnum.MATCH_ONLY) {
			ourLog.info("MDM running in {} mode. MDM channel consumer disabled.", myMdmSettings.getMode());
			return;
		}

		startListeningToMdmChannel();
	}

	protected ChannelConsumerSettings getChannelConsumerSettings() {
		return new ChannelConsumerSettings();
	}

	private void startListeningToMdmChannel() {
		if (myMdmChannel == null) {
			ChannelConsumerSettings config = getChannelConsumerSettings();

			config.setConcurrentConsumers(myMdmSettings.getConcurrentConsumers());

			myMdmChannel = myChannelFactory.getOrCreateReceiver(
					IMdmSettings.EMPI_CHANNEL_NAME, ResourceModifiedJsonMessage.class, config);
			if (myMdmChannel == null) {
				ourLog.error("Unable to create receiver for {}", IMdmSettings.EMPI_CHANNEL_NAME);
			} else {
				myMdmChannel.subscribe(myMdmMessageHandler);
				ourLog.info(
						"MDM Matching Consumer subscribed to Matching Channel {} with name {}",
						myMdmChannel.getClass().getName(),
						myMdmChannel.getName());
			}
		}
	}

	@SuppressWarnings("unused")
	@PreDestroy
	public void stop() throws Exception {
		if (myMdmChannel != null) {
			// JMS channel needs to be destroyed to avoid dangling receivers
			myMdmChannel.destroy();
			ourLog.info(
					"MDM Matching Consumer unsubscribed from Matching Channel {} with name {}",
					myMdmChannel.getClass().getName(),
					myMdmChannel.getName());
		}
	}

	@VisibleForTesting
	public IChannelReceiver getMdmChannelForUnitTest() {
		return myMdmChannel;
	}
}
