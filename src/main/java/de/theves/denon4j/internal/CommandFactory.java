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

package de.theves.denon4j.internal;

import de.theves.denon4j.internal.net.RequestCommand;
import de.theves.denon4j.internal.net.SetCommand;
import de.theves.denon4j.net.Command;
import de.theves.denon4j.net.CommandId;
import de.theves.denon4j.net.Parameter;
import de.theves.denon4j.net.Protocol;

/**
 * Class description.
 *
 * @author Sascha Theves
 */
class CommandFactory {

    private CommandFactory() {
    }

    static Command create(Protocol protocol, String prefix, String param) {
        if (null != prefix) {
            CommandId commandId = CommandId.random();
            if (param.contains("[")) {
                return new SetCommand(protocol, commandId, prefix);
            } else if (param.equals(Parameter.REQUEST.getValue())) {
                return new RequestCommand(protocol, commandId, prefix);
            }
            return new Command(protocol, commandId, prefix, Parameter.create(param));
        }
        throw new IllegalArgumentException("Command may not be null");
    }
}