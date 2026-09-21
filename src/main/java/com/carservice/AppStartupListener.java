package com.carservice;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AppStartupListener {

    static {
        System.setProperty("java.awt.headless", "true");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("\n✅ AutoElite is Ready\n");
    }
}
