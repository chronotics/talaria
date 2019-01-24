package org.chronotics.talaria.websocket.jetty;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Configuration
@Validated
@PropertySources({
        @PropertySource(
                value = "classpath:spring/properties/missing.properties",
                ignoreResourceNotFound=true),
        @PropertySource("classpath:spring/properties/jetty_websocket_server.properties")
})
@Component
public class JettyWebSocketServerProperties {

    public boolean isNull() {
        if(ip == null && port == null) {
            return true;
        } else {
            return false;
        }
    }

    @Valid
    @NotNull
    @Value("${jetty.websocket.server.ip}")
    private String ip;
    public String getIp() {
        return ip;
    }
    public void setIp(String _ip) {
        ip = _ip;
    }

    @Valid
    @NotNull
    @Value("${jetty.websocket.server.port}")
    private String port;
    public String getPort() {
        return port;
    }
    public void setPort(String _port) {
        port = _port;
    }

    @Valid
    @NotNull
    @Value("${jetty.websocket.server.contextPath}")
    private String contextPath;
    public String getContextPath() {
        return contextPath;
    }
    public void setContextPath(String _contextPath) {
        contextPath = _contextPath;
    }

    @Valid
    @NotNull
    @Value("${jetty.websocket.server.topicId}")
    private String topicId;
    public String getTopicId() {
        return topicId;
    }
    public void setTopicId(String _topicId) {
        topicId = _topicId;
    }

    @Valid
    @NotNull
    @Value("${jetty.websocket.server.topicPath}")
    private String topicPath;
    public String getTopicPath() {
        return topicPath;
    }
    public void setTopicPath(String _topicPath) {
        topicPath = _topicPath;
    }
}
