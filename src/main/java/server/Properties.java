package server;

/**
 * @author myth
 */
public class Properties {
    /**
    过期时间
     */
    private static long expire = 1200000;

    public static long getExpire() {
        return expire;
    }

    public static void setExpire(long expire) {
        Properties.expire = expire;
    }
}
