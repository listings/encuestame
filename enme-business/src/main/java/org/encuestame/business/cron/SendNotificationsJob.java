/*
 ************************************************************************************
 * Copyright (C) 2001-2011 encuestame: system online surveys Copyright (C) 2009
 * encuestame Development Team.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.encuestame.business.cron;

import java.util.Random;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.encuestame.utils.json.NotificationResume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

/**
 * Publish notifications to users subscribed 
 * @author Picado, Juan juanATencuestame.org
 * @since October 6, 2013
 */
public class SendNotificationsJob {
	
	private static final Log logger = LogFactory.getLog(SendNotificationsJob.class);
	
	@Autowired
    private SimpMessageSendingOperations messagingTemplate;
	
	/**
	 * @param messagingTemplate the messagingTemplate to set
	 */
	public void setMessagingTemplate(final SimpMessageSendingOperations messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}
	
	/**
	 * 
	 */
	public void sendNotifications() {
		String username = "demo10";
		final NotificationResume notificationResume = new NotificationResume();
		notificationResume.setTotalNewNot(RandomUtils.nextLong());
		notificationResume.setTotalNot(RandomUtils.nextLong());
		//logger.debug("sendNotifications update: " + notificationResume.toString());
		// en vez del username, un key que se genera en cada login, queda guardado en la bd y se refresca cada x minss
		this.messagingTemplate.convertAndSend("/topic/notification-updates." + username, notificationResume);
	}

	/**
	 * @return the messagingTemplate
	 */
	public SimpMessageSendingOperations getMessagingTemplate() {
		return messagingTemplate;
	}
	
	
}
