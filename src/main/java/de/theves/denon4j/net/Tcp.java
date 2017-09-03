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

package de.theves.denon4j.net;

import de.theves.denon4j.controls.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Sends the actual bytes of a command to the receiver.
 *
 * @author stheves
 */
public final class Tcp implements Protocol {
    private final Logger logger = LoggerFactory.getLogger(Tcp.class);
    private final Integer port;
    private final String host;
    private final EventReader eventReader;

    private EventDispatcher eventDispatcher;
    private Socket socket;
    private Writer writer;
    private Event mostRecent;

    public Tcp(String host, Integer port) {
        this.host = Optional.ofNullable(host).orElse("127.0.0.1");
        this.port = Optional.ofNullable(port).orElse(23);
        socket = new Socket();
        eventReader = new EventReader(this, socket);
    }

    void received(Event event) {
        if (logger.isDebugEnabled()) {
            logger.debug("Event received: {}", event);
        }

        notify(event);
        mostRecent = event;
    }

    private void notify(Event event) {
        if (null != eventDispatcher) {
            try {
                eventDispatcher.dispatch(event);
            } catch (Exception e) {
                logger.warn("Error invoking event listener", e);
            }
        }
    }

    @Override
    public void establishConnection(int timeout) throws ConnectException {
        if (isConnected()) {
            throw new ConnectException("Already connected.");
        }
        try {
            socket.setSoTimeout(0); // set infinite poll timeout
            socket.connect(new InetSocketAddress(host, port), timeout);

            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));

            eventReader.start();
        } catch (SocketTimeoutException ste) {
            throw new TimeoutException("Could not establish connection within timeout of " + timeout + " ms.", ste);
        } catch (IOException e) {
            throw new ConnectException("Cannot establishConnection to host/ip " + host + " on port " + port, e);
        }
    }


    @Override
    public boolean isConnected() {
        return null != socket && socket.isConnected();
    }

    @Override
    public void disconnect() {
        if (!isConnected()) {
            return;
        }
        try {
            eventReader.interrupt();
            socket.close();
        } catch (IOException e) {
            // ignore
            logger.debug("Disconnect failure", e);
        }
    }

    @Override
    public void send(Command command) {
        checkConnection();
        doSend(command);
    }

    @Override
    public void setDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }


    @Override
    public Event request(Command requestCommand) {
        synchronized (eventReader) {
            mostRecent = null;
            send(requestCommand);
            // wait until response is received
            try {
                eventReader.wait();
            } catch (InterruptedException e) {
                logger.warn("Receive interrupted", e);
            }
            // we expecting the last event as result of the request command
            return Optional.ofNullable(mostRecent).orElseThrow(() -> new ExecutionException("Could not get response"));
        }
    }


    private void checkConnection() {
        if (!isConnected()) {
            throw new ConnectionException("Not connected.");
        }
    }

    private void doSend(Command command) {
        synchronized (eventReader) {
            try {
                writer.write(command.signature() + PAUSE);
                writer.flush();
            } catch (Exception e) {
                throw new ConnectionException("Communication failure.", e);
            }
        }
    }
}