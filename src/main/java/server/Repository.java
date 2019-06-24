package server;

import java.util.HashMap;
import java.util.Map;

/**
 * @author myth
 */
public class Repository {
    private static Map<String, HttpMythSession> sessionMap = new HashMap<>();
    private static String fileRoot;

    public static String getFileRoot() {
        return fileRoot;
    }

    public static void setFileRoot(String fileRoot) {
        Repository.fileRoot = fileRoot;
    }

    public synchronized static void addSession(HttpMythSession session) {
        sessionMap.put(session.getSessionId(), session);
    }

    public synchronized static void remove(String sessionId) {
        sessionMap.remove(sessionId);
    }

    public synchronized static HttpMythSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }
}
