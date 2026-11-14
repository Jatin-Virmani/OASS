package com.oass.listener;

import com.oass.util.Config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.logging.Logger;

/**
 * Runs once when Tomcat starts the application.
 * Loads config.properties and (in later phases) starts the reminder scheduler.
 * Syllabus: Section D (ServletContextListener, application lifecycle).
 */
@WebListener
public class AppStartListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppStartListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        LOG.info("OASS starting...");
        Config.load();   // will throw and abort startup if config is missing
        com.oass.service.ReminderService.start(); // Start 2-hour deadline background checker
        LOG.info("OASS started. App at /oass on port 8080.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        LOG.info("OASS shutting down.");
        com.oass.service.ReminderService.stop(); // Clean shutdown of background threads
    }
}
