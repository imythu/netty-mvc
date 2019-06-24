package mvc.service.impl;

import annotation.MythService;
import mvc.service.TestService;

/**
 * @author myth
 */
@MythService
public class TestServiceImpl implements TestService {
    @Override
    public String getName() {
        return "我是netty";
    }
}
