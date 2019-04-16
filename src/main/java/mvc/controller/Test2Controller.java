package mvc.controller;

import annotation.MythController;
import annotation.MythRequestMapping;
import server.HttpMythResponse;

@MythController
public class Test2Controller {
    @MythRequestMapping("/test2.do")
    public void test2(HttpMythResponse response) {
        response.writeAndFlush("成功");
    }
}
