/*
 ************************************************************************************
 * Copyright (C) 2001-2010 encuestame: system online surveys Copyright (C) 2009
 * encuestame Development Team.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.encuestame.core.persistence.dao;

import java.util.List;

import org.encuestame.core.persistence.pojo.SecUser;
import org.encuestame.core.persistence.pojo.notifications.Notification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 * Notification Dao.
 * @author Picado, Juan juanATencuestame.org
 * @since Sep 18, 2010 7:10:33 PM
 * @version $Id:$
 */
public class NotificationDao extends AbstractHibernateDaoSupport implements INotification{

    /**
     * Load Notifications By {@link SecUser} and Limit. This method add all notifications without User (global)
     * @param secUser {@link SecUser}
     * @param limit limit
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Notification> loadNotificationByUserAndLimit(final SecUser secUser, final Integer limit){
         final DetachedCriteria criteria = DetachedCriteria.forClass(Notification.class);
            criteria.add(Restrictions.or(Restrictions.eq("secUser", secUser), Restrictions.isNull("secUser")));
            criteria.addOrder(Order.desc("created"));
            return getHibernateTemplate().findByCriteria(criteria, 0, limit);
    }


}