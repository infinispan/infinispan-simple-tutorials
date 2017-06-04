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

import org.infinispan.Cache;
import org.infinispan.remoting.transport.Address;
import org.infinispan.tutorial.simple.InfinispanHelper;
import org.infinispan.tutorial.simple.configuration.PlaygroundConfiguration;
import org.infinispan.tutorial.simple.console.UI;
import org.infinispan.tutorial.simple.console.support.IllegalParametersException;
import org.infinispan.tutorial.simple.domain.Value;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class LocateConsoleCommand implements ConsoleCommand {

    private static final String COMMAND_NAME = "locate";
    private final Cache<Long, Value> cache;

    public LocateConsoleCommand(PlaygroundConfiguration conf) {
        this.cache = conf.getCache();
    }

    @Override
    public String command() {
        return COMMAND_NAME;
    }

    @Override
    public boolean execute(UI console, Iterator<String> args) throws IllegalParametersException {
        try {
            Long id = Long.parseLong(args.next());

            console.println("Locating key " + id);
            List<Address> owners = InfinispanHelper.locate(cache);
            for (Address address : owners) {

                console.print(address);

                if (address.equals(cache.getCacheManager().getAddress())) {
                    console.print(" *");
                }

                if (address.equals(InfinispanHelper.locatePrimary(cache, id))) {
                    console.print(" (P)");
                }

                if (InfinispanHelper.locateReadOwners(cache, id).contains(address)) {
                    console.print(" (RO)");
                }

                if (InfinispanHelper.locateWriteOwners(cache, id).contains(address)) {
                    console.print(" (WO)");
                }

                if (InfinispanHelper.locateWriteBackupOwners(cache, id).contains(address)) {
                    console.print(" (WB)");
                }

                console.println();

            }
        } catch (NumberFormatException e) {
            throw new IllegalParametersException("Expected usage: locate <key>\nValue for key has to be a number. Example:\n locate 10");
        } catch (NoSuchElementException e) {
            throw new IllegalParametersException("Expected usage: locate <key>");
        }
        return true;
    }

    @Override
    public void usage(UI console) {
        console.println(COMMAND_NAME + " <key>");
        console.println("\t\tLocate an object in the grid.");
    }
}
