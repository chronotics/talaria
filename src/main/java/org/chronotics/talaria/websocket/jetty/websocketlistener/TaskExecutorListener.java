package org.chronotics.talaria.websocket.jetty.websocketlistener;

import org.chronotics.talaria.common.TaskExecutor;
import org.chronotics.talaria.websocket.jetty.JettyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class TaskExecutorListener extends JettyListener {

    private static final Logger logger =
            LoggerFactory.getLogger(TaskExecutorListener.class);

    /**
     *
     * @param bytes
     * @param i
     * offset
     * @param i1
     * length
     */
    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
        TaskExecutor<byte []> executor = getBytesExecutor();
        if(executor == null) {
            return;
        }
        byte [] newBytes = new byte[i1];
        newBytes = Arrays.copyOfRange(bytes,i,i+i1);
        try {
            executor.execute(newBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketText(String s) {
        TaskExecutor<String> executor = getStringExecutor();
        if(executor == null) {
            return;
        }
        try {
            executor.execute(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        logger.error(throwable.toString());
    }
}
