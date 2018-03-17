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
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;

/**
 * {@link EmbeddedServletContainerCustomizer} to add {@link InheritChannelJettyServerCustomizer}
 * into auto-configured embedded Jetty servlet container.
 *
 * @see EnableServletInheritChannel @EnableServletInheritChannel
 */
public class InheritChannelJettyServletContainerCustomizer implements EmbeddedServletContainerCustomizer {

    public void customize(ConfigurableEmbeddedServletContainer container) {
        if (container instanceof JettyEmbeddedServletContainerFactory)  {
            JettyEmbeddedServletContainerFactory factory = (JettyEmbeddedServletContainerFactory) container;

            // Ensure that the customizer is added only once.
            for (JettyServerCustomizer customizer : factory.getServerCustomizers()) {
                if (customizer instanceof InheritChannelJettyServerCustomizer) {
                    return;
                }
            }
            factory.addServerCustomizers(new InheritChannelJettyServerCustomizer());
        }
    }
}
