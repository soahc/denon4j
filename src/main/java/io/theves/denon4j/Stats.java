/*
 * Copyright 2017 Sascha Theves
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.theves.denon4j;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;

public class Stats {
    private int eventCounter = 0;
    private Instant begin;

    public Stats() {
        begin = Instant.now();
    }

    public int eventCounter() {
        return eventCounter;
    }

    public void incrementEvents() {
        eventCounter++;
    }

    public Duration duration() {
        return Duration.between(begin, Instant.now());
    }

    public void print(PrintStream out) {
        PrintWriter writer = new PrintWriter(out);
        writer.printf("Session Stats:%n");
        writer.printf(" Events: %-20d%n", eventCounter);
        writer.printf(" Time:   %-20s%n", duration().toString());
        writer.flush();
    }
}
