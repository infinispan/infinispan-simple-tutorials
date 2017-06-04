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

package org.infinispan.tutorial.simple.configuration;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionType;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.lookup.DummyTransactionManagerLookup;
import org.infinispan.tutorial.simple.domain.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaygroundConfiguration {

    private final static Logger log = LoggerFactory.getLogger(PlaygroundConfiguration.class);

    private DefaultCacheManager cacheManager;
    private Cache<Long, Value> cache;

    public PlaygroundConfiguration build() {
        cacheManager = buildCacheManager();
        cache = cacheManager.getCache("PLAYGROUND", true);
        cache.addListener(new ListenerTest());
        return this;
    }

    public DefaultCacheManager getCacheManager() {
        return cacheManager;
    }

    public Cache<Long, Value> getCache() {
        return cache;
    }

    private DefaultCacheManager buildCacheManager() {
        GlobalConfiguration glob = new GlobalConfigurationBuilder().clusteredDefault()
                .transport().clusterName("PLAYGROUND")
                .globalJmxStatistics().allowDuplicateDomains(true).enable()
                .build();

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.jmxStatistics().enable();
        configureCacheMode(configurationBuilder);

        if (persistence()) {
            configureCacheStore(configurationBuilder);
        }

        Configuration loc = configurationBuilder.transaction().transactionMode(TransactionMode.TRANSACTIONAL).transactionManagerLookup(new DummyTransactionManagerLookup()).build();
        return new DefaultCacheManager(glob, loc, true);
    }

    private void configureCacheStore(ConfigurationBuilder configurationBuilder) {

        configurationBuilder
                .persistence()
                .passivation(isPassivated())
                .addSingleFileStore()
                .shared(false)
                .location(location())
                .eviction()
                .strategy(EvictionStrategy.LRU).type(EvictionType.COUNT).size(evictionSize())
                .build();

    }

    private void configureCacheMode(ConfigurationBuilder configurationBuilder) {
        CacheMode cacheMode = getCacheMode();
        if (cacheMode.isDistributed()) {
            configurationBuilder
                    .clustering().cacheMode(cacheMode)
                    .hash().numOwners(getNumOwners());
        } else {
            configurationBuilder
                    .clustering().cacheMode(cacheMode);
        }
    }

    private CacheMode getCacheMode() {
        try {
            return CacheMode.valueOf(System.getProperty("grid.mode", "DIST_SYNC"));
        } catch (IllegalArgumentException e) {
            return CacheMode.DIST_SYNC;
        }
    }

    private int getNumOwners() {
        try {
            return Integer.valueOf(System.getProperty("grid.owners", "2"));
        } catch (IllegalArgumentException e) {
            return 2;
        }
    }

    private boolean persistence() {
        try {
            return Boolean.valueOf(System.getProperty("grid.persistence", "false"));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isPassivated() {
        try {
            return Boolean.valueOf(System.getProperty("grid.persistence.passivation", "false"));
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    public String location() {
        try {
            return System.getProperty("grid.persistence.location", System.getProperty("java.io.tmpdir"));
        } catch (IllegalArgumentException e) {
            return System.getProperty("java.io.tmpdir");
        }
    }

    private int evictionSize() {
        try {
            return Integer.valueOf(System.getProperty("grid.persistence.evictionSize", "10"));
        } catch (IllegalArgumentException e) {
            return 10;
        }
    }

}
