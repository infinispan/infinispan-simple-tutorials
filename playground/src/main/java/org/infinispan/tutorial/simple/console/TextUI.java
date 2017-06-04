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

package org.infinispan.tutorial.simple.console;

import org.infinispan.tutorial.simple.console.commands.ConsoleCommand;
import org.infinispan.tutorial.simple.console.support.ConsoleCommandComparator;
import org.infinispan.tutorial.simple.console.support.ConsoleCommandNotFoundException;
import org.infinispan.tutorial.simple.console.support.IllegalParametersException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

public class TextUI implements UI {

    private List<ConsoleCommand> commands;

    private final static Logger log = LoggerFactory.getLogger(TextUI.class);

    private final BufferedReader in;
    private final PrintStream out;

    public TextUI(List<ConsoleCommand> commands) {
        this.in = new BufferedReader(new InputStreamReader(System.in));
        this.out = System.out;
        this.commands = commands;
    }

    public void start() throws IOException {
        boolean keepRunning = true;
        while (keepRunning) {
            out.print("> ");
            out.flush();
            String line = in.readLine();
            if (line == null) {
                break;
            }
            keepRunning = processLine(line);
        }
    }

    private boolean processLine(String line) {
        Scanner scanner = new Scanner(line);
        try {
            String name = scanner.next();
            return findByName(name)
                    .orElseThrow(() -> new ConsoleCommandNotFoundException(name))
                    .execute(this, scanner);
        } catch (NoSuchElementException e) {
            out.println("> ");
        } catch (IllegalParametersException | ConsoleCommandNotFoundException e) {
            println(e.getMessage());
        }
        return true;
    }

    @Override
    public void print(Object message) {
        out.print(message);
    }

    @Override
    public void println(Object message) {
        out.println(message);
    }

    @Override
    public void print(String message) {
        out.print(message);
    }

    @Override
    public void println(String message) {
        out.println(message);
    }

    @Override
    public void println() {
        out.println();
    }

    @Override
    public void printUsage() {

        StreamSupport.stream(commands.spliterator(), true)
                .sorted(new ConsoleCommandComparator())
                .forEachOrdered(c -> {
                    c.usage(this);
                });
    }

    private Optional<ConsoleCommand> findByName(String name) {
        return StreamSupport.stream(commands.spliterator(), false)
                .filter(c -> Pattern.compile(c.command()).matcher(name).matches())
                .findFirst();
    }
}