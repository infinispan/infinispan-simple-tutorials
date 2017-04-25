/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.infinispan.tutorial.simple.console.commands;

import org.infinispan.Cache;
import org.infinispan.tutorial.simple.InfinispanHelper;
import org.infinispan.tutorial.simple.configuration.PlaygroundConfiguration;
import org.infinispan.tutorial.simple.console.UI;
import org.infinispan.tutorial.simple.console.support.IllegalParametersException;
import org.infinispan.tutorial.simple.domain.Value;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class WhoConsoleCommand implements ConsoleCommand {

    private static final String COMMAND_NAME = "who";
    private final Cache<Long, Value> cache;

    public WhoConsoleCommand(PlaygroundConfiguration conf) {
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
            console.println("Who am I for key " + id + "?");

            if (InfinispanHelper.isPrimary(cache, id)) {
                console.println("PRIMARY");
            }

            if (InfinispanHelper.isReadOwner(cache, id)) {
                console.println("READ OWNER");
            }

            if (InfinispanHelper.isWriteOwner(cache, id)) {
                console.println("WRITE OWNER");
            }

            if (InfinispanHelper.isWriteBackup(cache, id)) {
                console.println("WRITE BACKUP");
            }

        } catch (NumberFormatException e) {
            throw new IllegalParametersException("Expected usage: who <key>\nValue for key has to be a number. Example:\n locate 10");
        } catch (NoSuchElementException e) {
            throw new IllegalParametersException("Expected usage: who <key>");
        }
        return true;
    }

    @Override
    public void usage(UI console) {
        console.println(COMMAND_NAME + " <key>");
        console.println("\t\tWhich is the role for this node for an object.");
    }
}
