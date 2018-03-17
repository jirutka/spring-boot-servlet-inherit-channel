/*
 * Copyright 2018 Jakub Jirutka <jakub@jirutka.cz>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.jirutka.spring.boot.inheritchannel.jetty;

import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.AbstractConnectionFactory;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.Scheduler;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.Executor;

/**
 * {@linkplain ServerConnector}'s subclass that implicitly sets
 * {@link ServerConnector#setInheritChannel(boolean) inheritChannel} and reports real port number
 * obtained from the inherited channel. Unlike the parent class, this throws
 * an exception when inherited channel is not available.
 */
public class InheritChannelServerConnector extends ServerConnector {

    /**
     * Constructs a new InheritChannelServerConnector with the same parameters as the given
     * <tt>connector</tt> has and remove the <tt>connector</tt>'s
     * {@link ServerConnector#getSelectorManager() SelectorManager} from the context.
     *
     * @param connector the "template" to use for construction of new InheritChannelServerConnector.
     */
    public InheritChannelServerConnector(ServerConnector connector) {
        this(connector.getServer(),
                connector.getExecutor(),
                connector.getScheduler(),
                connector.getByteBufferPool(),
                connector.getAcceptors(),
                connector.getSelectorManager().getSelectorCount(),
                connector.getConnectionFactories().toArray(new ConnectionFactory[1]));
        removeBean(connector.getSelectorManager());
    }

    public InheritChannelServerConnector(Server server) {
        this(server, null, null, null, -1, -1, new HttpConnectionFactory());
    }

    public InheritChannelServerConnector(Server server, int acceptors, int selectors) {
        this(server, null, null, null, acceptors, selectors, new HttpConnectionFactory());
    }

    public InheritChannelServerConnector(Server server, int acceptors, int selectors,
                                         ConnectionFactory... factories) {
        this(server, null, null, null, acceptors, selectors, factories);
    }

    public InheritChannelServerConnector(Server server, ConnectionFactory... factories) {
        this(server, null, null, null, -1, -1, factories);
    }

    public InheritChannelServerConnector(Server server, SslContextFactory sslContextFactory) {
        this(server, -1, -1, sslContextFactory);
    }

    public InheritChannelServerConnector(Server server, int acceptors, int selectors,
                                         SslContextFactory sslContextFactory) {
        this(server, null, null, null, acceptors, selectors,
                AbstractConnectionFactory.getFactories(sslContextFactory, new HttpConnectionFactory()));
    }

    public InheritChannelServerConnector(Server server, SslContextFactory sslContextFactory,
                                         ConnectionFactory... factories) {
        this(server, null, null, null, -1, -1,
                AbstractConnectionFactory.getFactories(sslContextFactory, factories));
    }

    public InheritChannelServerConnector(Server server, Executor executor, Scheduler scheduler,
                                         ByteBufferPool bufferPool, int acceptors, int selectors,
                                         ConnectionFactory... factories) {
        super(server, executor, scheduler, bufferPool, acceptors, selectors, factories);

        setPort(0);
        setInheritChannel(true);
    }


    /**
     * {@inheritDoc}
     *
     * @throws SocketException if inherited channel is not available or is a wrong type
     */
    @Override
    public void open() throws IOException {
        if (getTransport() == null) {
            Channel channel = System.inheritedChannel();

            if (channel == null) {
                throw new SocketException("No inherited channel is available! "
                                        + "Expected a TCP socket passed as STDIN.");
            }
            if (! (channel instanceof ServerSocketChannel)) {
                throw new SocketException("Inherited channel is not a ServerSocketChannel, but "
                                        + channel.getClass());
            }
        }
        super.open();
    }

    @Override
    public int getLocalPort() {
        Object channel = getTransport();

        if (channel != null) {
            Assert.state(channel instanceof ServerSocketChannel,
                    "Inherited channel is not of type ServerSocketChannel: " + channel.toString());
            try {
                SocketAddress address = ((ServerSocketChannel) channel).getLocalAddress();
                if (address instanceof InetSocketAddress) {
                    return ((InetSocketAddress) address).getPort();
                }
            } catch (IOException ex) {
                LOG.warn("Unable to get port number from the inherited channel: {}", channel);
            }
        }

        return super.getLocalPort();  // fallback
    }

    @Override
    public void setInheritChannel(boolean inheritChannel) {
        Assert.isTrue(inheritChannel, "This class should be used only with inheritChannel");
        super.setInheritChannel(true);
    }
}
