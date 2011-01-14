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
package org.encuestame.persistence.dao.imp;

import org.encuestame.persistence.dao.ISocialProviderDao;
import org.encuestame.persistence.domain.security.SocialAccountProvider;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

/**
 * Repository of Social Provider Items.
 *
 * @author Picado, Juan juanATencuestame.org
 * @since Dec 25, 2010 9:55:59 AM
 * @version $Id:$
 */
@Repository("socialProviderDao")
public class SocialProviderDao extends AbstractHibernateDaoSupport implements ISocialProviderDao {

    @Autowired
    public SocialProviderDao(SessionFactory sessionFactory) {
        setSessionFactory(sessionFactory);
    }

    /**
     * Social Account Provider Id.
     * @param socialAccountId social account id.
     * @return {@link SocialAccountProvider}.
     */
    public SocialAccountProvider getSocialAccountProviderId(final Long socialAccountId) {
        return (SocialAccountProvider)
               (getHibernateTemplate().get(SocialAccountProvider.class,
                socialAccountId));
    }

    /**
     * Get Social Account Provider.
     * @param socialName name
     * @return
     */
    @SuppressWarnings("unchecked")
    public SocialAccountProvider getSocialAccountProviderId(final String socialName) {
        final DetachedCriteria criteria = DetachedCriteria.forClass(SocialAccountProvider.class);
        criteria.add(Restrictions.eq("name", socialName) );
        return (SocialAccountProvider) DataAccessUtils.uniqueResult(getHibernateTemplate()
                .findByCriteria(criteria));
    }

}