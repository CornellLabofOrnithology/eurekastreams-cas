/*
 * Copyright (c) 2009-2010 Lockheed Martin Corporation
 *
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
 */
package org.eurekastreams.server.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eurekastreams.commons.hibernate.QueryOptimizer;
import org.eurekastreams.server.domain.CasPerson;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class provides the mapper functionality for Person entities.
 */
@Deprecated
public class CasPersonMapperImpl extends DomainEntityMapper<CasPerson> implements CasPersonMapper
{
    /**
     * Local log instance.
     */
    private static Log logger = LogFactory.getLog(CasPersonMapper.class);
    

    /**
     * Constructor.
     * 
     * @param inQueryOptimizer
     *            the QueryOptimizer to use for specialized functions.
     */
    public CasPersonMapperImpl(final QueryOptimizer inQueryOptimizer)
    {
        super(inQueryOptimizer);
    }

    /**
     * Retrieve the name of the DomainEntity. This is to allow for the super class to identify the table within
     * hibernate.
     * 
     * @return The name of the domain entity.
     */
    @Override
    protected String getDomainEntityName()
    {
        return "CasPerson";
    }

  
    /**
     * Creates a follower/following relationship between two Person objects.
     * 
     * @param followerId
     *            The id of the follower Person
     * @param followingId
     *            The id of the person being Followed.
     */
    @Transactional
    public void updateUserId(final String accountId, final String userId)
    {
    	//TODO check for this userid already
        /*Query q = getEntityManager().createQuery(
                "FROM Follower where followerId=:followerId and followingId=:followingId").setParameter("followerId",
                followerId).setParameter("followingId", followingId);

        if (q.getResultList().size() > 0)
        {
            // already following
            return;
        }*/

        // add follower
        //getEntityManager().persist(new Follower(followerId, followingId));

        // now update the counts for persons subtracting 1 for themselves.
        getEntityManager().createQuery(
                "update versioned CasPerson set userId = :userId where accountId = :accountId").setParameter(
                "userId", userId).setParameter("accountId", accountId).executeUpdate();

       
        getEntityManager().flush();
        getEntityManager().clear();

        // reindex the following in the search index

        // Note: Finding the entity by id is massively faster than doing a refresh on
        // the entity. This way the recently fetched entity will have the updated counts
        // to send to index.
        //Person followingEntity = findById(followingId);

        //getFullTextSession().index(followingEntity);
    }

  
}
