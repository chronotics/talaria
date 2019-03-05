package org.chronotics.talaria.websocket.jetty;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class JettySessionCommon {

    private static final Logger logger =
            LoggerFactory.getLogger(JettySessionCommon.class);

    public static Future<Void> sendMessage(Session _session, Object _value) {
        Future<Void> future = null;
        if(_value instanceof String) {
            future = _session.getRemote().sendStringByFuture((String)_value);
        } else if (_value instanceof byte[]) {
            future = _session.getRemote().sendBytesByFuture(ByteBuffer.wrap((byte[])_value));
        } else {
            // convert Object to String because Web uses String type of data
            future = _session.getRemote().sendStringByFuture(_value.toString());
            logger.info("toString() is {}", _value.toString());
        }

        return future;
    }

    public static List<String> getParameterList(Session _session, String _key) {
        Map<String, List<String>> parameterMap =
                _session.getUpgradeRequest().getParameterMap();
        return parameterMap.get(_key);
    }
}
