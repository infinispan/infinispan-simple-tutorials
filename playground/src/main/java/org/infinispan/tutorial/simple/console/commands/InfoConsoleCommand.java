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

package org.infinispan.tutorial.simple.console.commands;

import org.infinispan.tutorial.simple.configuration.PlaygroundConfiguration;
import org.infinispan.tutorial.simple.console.UI;
import org.infinispan.tutorial.simple.console.support.IllegalParametersException;

import java.util.Iterator;

public class InfoConsoleCommand implements ConsoleCommand {

    private static final String COMMAND_NAME = "info";
    private PlaygroundConfiguration conf;

    public InfoConsoleCommand(PlaygroundConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public String command() {
        return COMMAND_NAME;
    }

    @Override
    public boolean execute(UI console, Iterator<String> args) throws IllegalParametersException {
        console.println(buildInfo());
        return true;
    }

    private String buildInfo() {

        return "Cache Mode: " + conf.getCache().getCacheConfiguration().clustering().cacheModeString() + "\n" +
                "Cache Manager Status: " + conf.getCacheManager().getStatus() + "\n" +
                "Cache Manager Address: " + conf.getCacheManager().getAddress() + "\n" +
                "Coordinator address: " + conf.getCacheManager().getCoordinator() + "\n" +
                "Is Coordinator: " + conf.getCacheManager().isCoordinator() + "\n" +
                "Cluster Name: " + conf.getCacheManager().getClusterName() + "\n" +
                "Cluster Size: " + conf.getCacheManager().getClusterSize() + "\n" +
                "Member list: " + conf.getCacheManager().getMembers() + "\n" +
                "Cache Name: " + conf.getCache() + "\n" +
                "Cache Size: " + conf.getCache().size() + "\n" +
                "Cache Status: " + conf.getCache().getStatus() + "\n" +
                "Cache Persistence:" + conf.getCache().getCacheConfiguration().persistence().toString() + "\n" +
                "Persistence Directory:" + conf.location() + "\n" +
                "Number of Owners: " + conf.getCache().getAdvancedCache().getDistributionManager().getWriteConsistentHash().getNumOwners() + "\n" +
                "Number of Segments: " + conf.getCache().getAdvancedCache().getDistributionManager().getWriteConsistentHash().getNumSegments() + "\n";
    }

    @Override
    public void usage(UI console) {
        console.println(COMMAND_NAME);
        console.println("\t\tInformation on cache.");
    }

}
