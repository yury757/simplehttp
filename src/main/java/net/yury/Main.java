package net.yury;

import net.yury.simplehttp.SHServer;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        TestController testController = new TestController();
        SHServer server = new SHServer(8080, testController);
        server.start();
    }
}
