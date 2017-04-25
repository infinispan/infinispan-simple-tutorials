/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.infinispan.tutorial.simple;

import org.infinispan.tutorial.simple.domain.Value;
import org.infinispan.Cache;
import org.infinispan.distribution.LocalizedCacheTopology;
import org.infinispan.remoting.transport.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InfinispanHelper {

    public static String routingTable(Cache<Long, Value> cache) {
        return cache.getAdvancedCache().getDistributionManager().getWriteConsistentHash().getRoutingTableAsString();
    }

    public static List<Address> locate(Cache<Long, Value> cache) {
        return cache.getAdvancedCache().getDistributionManager().getCacheTopology().getActualMembers();
    }

    public static Address locatePrimary(Cache<Long, Value> cache, Long key) {
        return cache.getAdvancedCache().getDistributionManager().getCacheTopology().getDistribution(key).primary();
    }

    public static List<Address> locateReadOwners(Cache<Long, Value> cache, Long key) {
        return cache.getAdvancedCache().getDistributionManager().getCacheTopology().getDistribution(key).readOwners();
    }

    public static List<Address> locateWriteOwners(Cache<Long, Value> cache, Long key) {
        return cache.getAdvancedCache().getDistributionManager().getCacheTopology().getDistribution(key).writeOwners();
    }

    public static Collection<Address> locateWriteBackupOwners(Cache<Long, Value> cache, Long key) {
        return cache.getAdvancedCache().getDistributionManager().getCacheTopology().getDistribution(key).writeBackups();
    }

    public static boolean isPrimary(Cache<Long, Value> cache, Long key) {
        return cache.getAdvancedCache().getDistributionManager().getCacheTopology().getDistribution(key).isPrimary();
    }

    public static boolean isReadOwner(Cache<Long, Value> cache, Long key) {
        return cache.getAdvancedCache().getDistributionManager().getCacheTopology().getDistribution(key).isReadOwner();
    }

    public static boolean isWriteOwner(Cache<Long, Value> cache, Long key) {
        return cache.getAdvancedCache().getDistributionManager().getCacheTopology().getDistribution(key).isWriteOwner();
    }

    public static boolean isWriteBackup(Cache<Long, Value> cache, Long key) {
        return cache.getAdvancedCache().getDistributionManager().getCacheTopology().getDistribution(key).isWriteBackup();
    }

    public static boolean checkIfCacheIsPrimaryFor(Cache<Long, Value> cache, long key) {
        return cache.getAdvancedCache().getDistributionManager().getCacheTopology().getDistribution(key).isPrimary();
    }

    public static boolean checkIfKeyIsLocalInCache(Cache<Long, Value> cache, long key) {
        LocalizedCacheTopology topology = cache.getAdvancedCache().getDistributionManager().getCacheTopology();
        return topology.getDistribution(key).isPrimary() || topology.isReadOwner(key) || topology.isWriteOwner(key);
    }

    public static boolean checkIfCacheIsSecondaryFor(Cache<Long, Value> cache, long key) {
        return !checkIfCacheIsPrimaryFor(cache, key) && checkIfKeyIsLocalInCache(cache, key);
    }

    public static Set<String> valuesFromKeys(Cache<Long, Value> cache) {
        return valuesFromKeys(cache, Filter.ALL);
    }

    public static Set<String> localValuesFromKeys(Cache<Long, Value> cache) {
        return valuesFromKeys(cache, Filter.LOCAL);
    }

    public static Set<String> primaryValuesFromKeys(Cache<Long, Value> cache) {
        return valuesFromKeys(cache, Filter.PRIMARY);
    }

    public static Set<String> replicaValuesFromKeys(Cache<Long, Value> cache) {
        return valuesFromKeys(cache, Filter.REPLICA);
    }

    private static Set<String> valuesFromKeys(Cache<Long, Value> cache, Filter filter) {
        Set<String> values = new HashSet<String>();

        for (Long l : cache.keySet()) {
            switch (filter) {
                case ALL:
                    values.add(l + " " + cache.get(l));
                    break;
                case LOCAL:
                    if (checkIfKeyIsLocalInCache(cache, l)) {
                        values.add(l + " " + cache.get(l));
                    }
                    break;
                case PRIMARY:
                    if (checkIfCacheIsPrimaryFor(cache, l)) {
                        values.add(l + " " + cache.get(l));
                    }
                    break;
                case REPLICA:
                    if (checkIfCacheIsSecondaryFor(cache, l)) {
                        values.add(l + " " + cache.get(l));
                    }
                    break;
            }
        }
        return values;
    }

    private static final Logger log = LoggerFactory.getLogger(InfinispanHelper.class);

    private enum Filter {ALL, LOCAL, PRIMARY, REPLICA};

}
