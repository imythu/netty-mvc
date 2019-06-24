package server;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HttpMythSession{

    private String sessionId;
    private Map<String, Object> attributeMap;
    //毫秒
    private long expire = 1200000;
    private Date createTime;

    public HttpMythSession(String cookie) {
        String mythId = cookie.split("=")[0];
        if (!isExist(mythId)) {
            sessionId = UUID.randomUUID().toString();
            attributeMap = new HashMap<>();
            expire = Properties.getExpire();
            createTime = new Date();
            Repository.addSession(this);
        }
    }

//    private void setSessionId(String id) {
//        sessionId = id;
//    }

    public String getSessionId() {
        return sessionId;
    }

    private boolean isExist(String mythId){
        HttpMythSession session = Repository.getSession(mythId);
        boolean isExist =  session != null;
        if (isExist) {
            if (session.expire < remainingTime(session.createTime)) {
                try {
                    session.finalize();
                    Repository.remove(mythId);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                return false;
            }
            this.sessionId = session.sessionId;
            this.attributeMap = session.attributeMap;
            this.expire = session.expire;
            this.createTime = session.createTime;
        }
        return isExist;
    }

    private long remainingTime(Date createTime) {
        Date currentDate = new Date();
        return currentDate.getTime() - createTime.getTime();
    }
}
