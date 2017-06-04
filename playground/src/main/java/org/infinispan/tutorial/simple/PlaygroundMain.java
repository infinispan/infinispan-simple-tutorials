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

import org.infinispan.tutorial.simple.configuration.PlaygroundConfiguration;
import org.infinispan.tutorial.simple.console.TextUI;
import org.infinispan.tutorial.simple.console.commands.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlaygroundMain {

    public static void main(String[] args) throws IOException {

        PlaygroundConfiguration conf = new PlaygroundConfiguration();
        conf.build();

        List<ConsoleCommand> commands = new ArrayList<>();
        commands.add(new AddressConsoleCommand(conf));
        commands.add(new AllConsoleCommand(conf));
        commands.add(new ClearConsoleCommand(conf));
        commands.add(new GetConsoleCommand(conf));
        commands.add(new HelpConsoleCommand());
        commands.add(new InfoConsoleCommand(conf));
        commands.add(new KeyConsoleCommand(conf));
        commands.add(new LoadTestConsoleCommand(conf));
        commands.add(new LocalConsoleCommand(conf));
        commands.add(new LocateConsoleCommand(conf));
        commands.add(new PrimaryConsoleCommand(conf));
        commands.add(new PutConsoleCommand(conf));
        commands.add(new PutIfAbsentConsoleCommand(conf));
        commands.add(new QuitConsoleCommand(conf));
        commands.add(new ReplicaConsoleCommand(conf));
        commands.add(new RoutingConsoleCommand(conf));
        commands.add(new WhoConsoleCommand(conf));

        TextUI textUI = new TextUI(commands);
        printBanner();
        textUI.start();

    }

    private static void printBanner() {
        System.out.println("---------------------------------------");
        System.out.println("       Infinispan Playground CLI");
        System.out.println("---------------------------------------");
        System.out.println();
    }

    private static Logger log = LoggerFactory.getLogger(PlaygroundMain.class.getName());
}

