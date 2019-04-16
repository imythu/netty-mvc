package server;

public class Properties {
    private static long expire = 1200000;//毫秒

    public static long getExpire() {
        return expire;
    }

    public static void setExpire(long expire) {
        Properties.expire = expire;
    }
}
