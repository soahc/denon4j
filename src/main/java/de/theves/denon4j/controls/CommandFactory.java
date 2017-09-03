/*
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

package de.theves.denon4j.controls;

import de.theves.denon4j.internal.net.Command;
import de.theves.denon4j.internal.net.ParameterImpl;
import de.theves.denon4j.internal.net.RequestCommand;
import de.theves.denon4j.internal.net.SetCommand;
import de.theves.denon4j.net.Protocol;

import static de.theves.denon4j.internal.net.ParameterImpl.createParameter;

/**
 * Factory for commands.
 *
 * @author stheves
 */
public class CommandFactory {

    private CommandFactory() {
    }

    public static Command createCommand(Protocol protocol, String prefix, String param) {
        if (null != prefix) {
            if (param.contains("[")) {
                return new SetCommand(protocol, prefix);
            } else if (isRequest(prefix, param)) {
                return new RequestCommand(protocol, prefix);
            }
            return new Command(protocol, prefix, createParameter(param));
        }
        throw new IllegalArgumentException("Command may not be null");
    }

    private static boolean isRequest(String prefix, String param) {
        return param.equals(ParameterImpl.REQUEST.getValue());
    }
}
