package net.yury;

import net.yury.simplehttp.annotation.SHController;
import net.yury.simplehttp.annotation.SHMapping;

public class TestController implements SHController {
    @SHMapping(path = "/test")
    public String test(String content) {
        return "success";
    }
}
