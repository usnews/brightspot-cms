---
layout: default
title: Task
id: task
---

## Task

The task tool shows all background tasks being implemented on the server. New tasks that are created show up within the interface, including Database Manager tasks carried out through the `_debug/db-manager`tool.

Creating a new task requires extending from Task, found within `dari.util`. Alternately a simple way to implement a new task is:

	 private Task taskName = new Task(null, "Name to show in Task Tool") { 
       protected void doTask() { 
           **TASK**
       } 
    };

**Starting Task**

Create a .jsp file and place it in the _debug directory of your project. The task can then be started from within the _debug Custom menu.

	<%
	Example example = Example.getInstance();

	if (!StringUtils.isBlank(request.getParameter("run"))) {
    // run it
    importer.start();

	}

### Scheduled Task

#### Create a Listener

    package com.project.importer;

    import com.psddev.dari.db.Application;
    import com.psddev.dari.util.ObjectUtils;
    import com.project.model.ImporterSchedulerSettings;
    import com.project.model.ImporterSchedulerSettings.TaskSchedule;

    import org.quartz.CronScheduleBuilder;
    import org.quartz.CronTrigger;
    import org.quartz.DateBuilder;
    import org.quartz.JobDetail;
    import org.quartz.JobBuilder;
    import org.quartz.ScheduleBuilder;
    import org.quartz.Scheduler;
    import org.quartz.SchedulerException;
    import org.quartz.SimpleScheduleBuilder;
    import org.quartz.Trigger;
    import org.quartz.TriggerBuilder;
    import org.quartz.impl.StdSchedulerFactory;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    import javax.servlet.ServletContext;
    import javax.servlet.ServletContextEvent;
    import javax.servlet.ServletContextListener;

    import java.net.InetAddress;
    import java.net.UnknownHostException;
    import java.util.Arrays;
    import java.util.List;

    /**
     * This gets called on server startup and allows us to start up the Quartz
     * scheduler
     * 
     * The scheduler properties are in resources quartz.properties. We have one
     * thread configured there so that no two jobs run at the same time
     */
    public class SchedulerListener implements ServletContextListener {

    private static Logger logger = LoggerFactory
            .getLogger(SchedulerListener.class);
    
    private static Scheduler scheduler = null;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        logger.info("Initializing SchedulerListener");

        try {
            ImporterSchedulerSettings schedulerSettings = Application.Static
                    .getInstance(ImporterSchedulerSettings.class);

            if (schedulerSettings.isDisableScheduler()) {
                logger.info("Scheduler disabled in Admin/ImporterSchedulerSettings");
                return;
            }
            String jobHost = schedulerSettings.getJobHost();
            String thisHost = InetAddress.getLocalHost().getHostName();

            if (jobHost != null && jobHost.equals("localhost")) {
                jobHost = thisHost;
            }

            if (jobHost == null || !jobHost.equals(thisHost)) {
                logger.info("This not the host defined as the jobHost in Admin/ImporterSchedulerSettings. No scheduler will be started");
                return;
            } else {
                logger.info("This is the scheduler host. Commencing with scheduler initialization");
            }

            // Grab the Scheduler instance from the Factory
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            if (schedulerSettings.getScheduledTasks() != null) {
                for (TaskSchedule taskSchedule : schedulerSettings
                        .getScheduledTasks()) {
                    String schedule = taskSchedule.getCronSchedule();
                    String className = taskSchedule.getImporterTaskClass();
                   
                    
                    if (schedule != null && className != null) {
                        try {
                            Class jobClass = Class.forName(className);
                            // define the job and tie it to the importer class
                            JobDetail job = JobBuilder
                                    .newJob(jobClass)
                                    .withIdentity(className, "ImporterGroup")
                                    .build();

                            CronTrigger trigger = TriggerBuilder
                                    .newTrigger()
                                    .withIdentity(className + "Trigger",
                                            "ImporterGroup")
                                    .withSchedule(
                                            CronScheduleBuilder
                                                    .cronSchedule(schedule))
                                    .build();

                            // Tell quartz to schedule the job using our trigger
                            scheduler.scheduleJob(job, trigger);
                         
                        } catch (Exception e) {
                            logger.error("Unable to schedule the WCities Importer");
                            e.printStackTrace();
                        }
                    }
                }
            }

            // and start it all off
            scheduler.start();

            logger.info("Scheduler started");

        } catch (SchedulerException e) {
            logger.error("Error encountered when starting the scheduler");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            logger.error("Error encountered when starting the scheduler - Unable to determine our hostname");
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Error encountered when starting the scheduler");
            e.printStackTrace();  
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        try {
            logger.info("Stopping the scheduler");
            if (scheduler != null && scheduler.isStarted()) {
                scheduler.shutdown();           
            }
        } catch (SchedulerException e) {
            logger.error("Error encountered when stopping the scheduler");
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Error encountered when starting the scheduler");
            e.printStackTrace();  
        }

      }
    }

### Add to web.xml

    <!--  Listeners -->

          <listener>
            <listener-class>com.projectname.importer.SchedulerListener</listener-class>

         </listener>

#### Scheduler Settings

    package com.projectname.model;

    import java.util.ArrayList;
    import java.util.List;

    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;


    import com.psddev.cms.tool.Tool;
    import com.psddev.cms.db.Content;
    import com.psddev.cms.db.Directory;
    import com.psddev.cms.db.ToolUi;

    /**
     * Settings for our import job scheduler
     */
    public class ImporterSchedulerSettings extends Tool {

	static public Logger logger = LoggerFactory
			.getLogger(ImporterSchedulerSettings.class);

	
	@ToolUi.Note("The scheduler only runs on the specified host (can specify 'localhost', but should do so only in single server configuration). Requires restart to take effect")
	private String jobHost;
	@ToolUi.Note("If checked, the scheduler will not run on any host. Requires restart to take effect")
	private boolean disableScheduler;
	
	private List<TaskSchedule> scheduledTasks;
	
    public String getJobHost() {
        return jobHost;
    }
    public void setJobHost(String jobHost) {
        this.jobHost = jobHost;
    }
    public boolean isDisableScheduler() {
        return disableScheduler;
    }
    public void setDisableScheduler(boolean disableScheduler) {
        this.disableScheduler = disableScheduler;
    }
   
    public List<TaskSchedule> getScheduledTasks() {
        return scheduledTasks;
    }
    public void setScheduledTasks(List<TaskSchedule> scheduledTasks) {
        this.scheduledTasks = scheduledTasks;
    }

        @Embedded
        public static class TaskSchedule extends Content {
            @ToolUi.Note("Name of the importer class. This class must implement the Quartz Job interface")
            private String importerTaskClass;
            @ToolUi.Note("Schedule following cron syntax (e.g. 0/20 * * * * ?)")
            private String cronSchedule;
        
        public String getImporterTaskClass() {
            return importerTaskClass;
        }
        public void setImporterTaskClass(String importerTaskClass) {
            this.importerTaskClass = importerTaskClass;
        }
        public String getCronSchedule() {
            return cronSchedule;
        }
        public void setCronSchedule(String cronSchedule) {
            this.cronSchedule = cronSchedule;
        }
        
        
    }
     


    }

### Quartz

When using Quartz, the resource file, `quartz.properties` must be created:

    org.quartz.scheduler.instanceName = YOURSchedulerNAME
    org.quartz.threadPool.threadCount = 1
    org.quartz.jobStore.class = org.quartz.simpl.RAMJobStore

Within your `pom.xml` add:

     <!--  Quartz Scheduler -->
		<dependency>
			<groupId>org.quartz-scheduler</groupId>
			<artifactId>quartz</artifactId>
			<version>2.1.5</version>
		</dependency>
		<dependency>
			<groupId>org.quartz-scheduler</groupId>
			<artifactId>quartz-oracle</artifactId>
			<version>2.1.5</version>
		</dependency>
		<dependency>
			<groupId>org.quartz-scheduler</groupId>
			<artifactId>quartz-weblogic</artifactId>
			<version>2.1.5</version>
		</dependency>
		<dependency>
			<groupId>org.quartz-scheduler</groupId>
			<artifactId>quartz-jboss</artifactId>
			<version>2.1.5</version>
		</dependency>