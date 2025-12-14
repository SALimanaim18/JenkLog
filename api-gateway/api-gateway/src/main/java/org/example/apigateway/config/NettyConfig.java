package org.example.apigateway.config;

import io.netty.channel.ChannelOption;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.server.HttpServer;

@Configuration
public class NettyConfig {

    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> nettyCustomizer() {
        return factory -> factory.addServerCustomizers(
                httpServer -> httpServer
                        .httpRequestDecoder(spec -> spec
                                .maxHeaderSize(131072)  // ← 128KB
                                .maxInitialLineLength(16384)  // ← 16KB
                        )
        );
    }
}