package com.findr;

import com.findr.service.ItemService;
import com.findr.service.ServiceFactory;
import com.findr.web.ApiServer;

/** Starts the web backend. Run this, then open http://localhost:8080 */
public class ServerMain {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        ItemService service = ServiceFactory.create("data");
        new ApiServer(service, port).start();
    }
}
