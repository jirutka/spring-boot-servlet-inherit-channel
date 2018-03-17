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

import cz.jirutka.spring.boot.inheritchannel.EnableServletInheritChannel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.util.Assert;

/**
 * {@link JettyServerCustomizer} to add {@link InheritChannelServerConnector} into the Server.
 *
 * <h3>Example:</h3>
 *
 * <pre class="code">
 * &#064;ComponentScan
 * &#064;SpringBootApplication
 * public class Application {
 *
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 *
 *     &#064;Bean
 *     public JettyEmbeddedServletContainerFactory jettyEmbeddedServletContainerFactory() {
 *         JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory();
 *         factory.addServerCustomizers(new InheritChannelJettyServerCustomizer());
 *
 *         return factory;
 *     }
 * }
 * </pre>
 *
 * @see EnableServletInheritChannel @EnableServletInheritChannel
 */
public class InheritChannelJettyServerCustomizer implements JettyServerCustomizer {

    protected static final Log LOG = LogFactory.getLog(InheritChannelJettyServerCustomizer.class);

    /**
     * Convert the first {@link ServerConnector} of the <tt>server</tt> to
     * {@link InheritChannelServerConnector} or add a new one if does not have any.
     *
     * @param server the server to customize.
     */
    public void customize(Server server) {
        // Note: This method returns a copy of the internal array of connectors
        // and normally should contain single ServerConnector.
        Connector[] connectors = server.getConnectors();

        if (connectors.length > 1) {
            LOG.warn("Server has more than one Connector, only the first one will inherit channel");
        }

        if (connectors.length > 0) {
            Connector connector = connectors[0];
            // This should not happen.
            Assert.isInstanceOf(ServerConnector.class, connector);

            connectors[0] = new InheritChannelServerConnector((ServerConnector) connector);
        } else {
            connectors = new Connector[]{ new InheritChannelServerConnector(server) };
        }

        server.setConnectors(connectors);
    }
}
