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
package cz.jirutka.spring.boot.inheritchannel;

import cz.jirutka.spring.boot.inheritchannel.EnableServletInheritChannel.JettyConfiguration;
import cz.jirutka.spring.boot.inheritchannel.jetty.InheritChannelJettyServerCustomizer;
import cz.jirutka.spring.boot.inheritchannel.jetty.InheritChannelJettyServletContainerCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adjusts auto-configured embedded servlet container to inherit channel
 * (socket) from the process that started JVM. It is activated only when
 * property <tt>server.inherit-channel</tt> is truthy (e.g. using CLI argument
 * <tt>--server.inherit-channel=true</tt>).
 *
 * <p>This is usually referred as "passing socket to a process" or "socket
 * activation". JVM supports (x)inetd-style of passing socket - through STDIN
 * (file descriptor 0).</p>
 *
 * <p>Currently only Jetty is supported, but it should be possible to add
 * implementation for Tomcat as well.</p>
 *
 * <h3>Example:</h3>
 *
 * <pre class="code">
 * &#064;ComponentScan
 * &#064;SpringBootApplication
 * &#064;EnableServletInheritChannel
 * public class Application {
 *
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 * </pre>
 *
 * @see InheritChannelJettyServletContainerCustomizer
 * @see InheritChannelJettyServerCustomizer
 * @see System#inheritedChannel()
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(JettyConfiguration.class)
public @interface EnableServletInheritChannel {

    @Configuration
    @ConditionalOnClass(org.eclipse.jetty.server.Server.class)
    @ConditionalOnProperty("server.inherit-channel")
    class JettyConfiguration {

        @Bean
        public EmbeddedServletContainerCustomizer embeddedServletContainerCustomizer() {
            return new InheritChannelJettyServletContainerCustomizer();
        }
    }
}
