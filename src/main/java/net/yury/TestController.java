package net.yury;

import net.yury.simplehttp.annotation.SHController;
import net.yury.simplehttp.annotation.SHMapping;

import java.util.HashMap;
import java.util.Map;

public class TestController implements SHController {
    @SHMapping(path = "/test")
    public String test(String content) {
        return "success";
    }

    @SHMapping(path = "/test2")
    public String test2(String content) {
        return "success2";
    }

    @SHMapping(path = "/test3")
    public String test3(String content) {
        Map<String, String> params = getParams(content);
        String p1 = params.getOrDefault("p1", null);
        String p2 = params.getOrDefault("p2", null);
        if (p1 == null || p2 == null) {
            throw new RuntimeException("p1 or p2 param not exists");
        }
        return p1 + "-" + p2;
    }

    @SHMapping(path = "/testPost", method = "POST")
    public void testPost(String content) {
        System.out.println(content);
    }

    public Map<String, String> getParams(String content) {
        Map<String, String> params = new HashMap<>();
        String[] split = content.split("&");
        for (String s : split) {
            String[] split2 = s.split("=");
            if (split2.length >= 2) {
                params.put(split2[0], split2[1]);
            }
        }
        return params;
    }
}
