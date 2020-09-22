package com.versatiletester.test;

import com.versatiletester.action.manager.CSVActionManager;
import com.versatiletester.action.manager.ExcelActionManager;
import com.versatiletester.action.type.Action;
import com.versatiletester.config.SpringContext;
import com.versatiletester.config.TestContext;
import com.versatiletester.util.driver.DriverCapabilityManager;
import com.versatiletester.util.driver.DriverFactory;
import com.versatiletester.util.driver.DriverUtils;
import com.versatiletester.util.file.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SpringContext.class)
public class TestBase {
    private static final Logger log = Logger.getLogger(TestBase.class);

    /* Utilities for Driver, Test, Spring.
    *  Any object which needs to be defined as a singleton instance is Autowired in through Spring-Boot. */
    @Autowired
    protected SpringContext springContext;
    @Autowired
    protected FileUtils fileUtils;
    @Autowired
    protected CSVActionManager csvActionManager;
    @Autowired
    protected ExcelActionManager excelActionManager;

    protected DriverFactory driverFactory;
    public TestContext testContext;

    /**
     * Generic @Before junit method to instantiate a unique set of driver-related objects before every test.
     * DO NOT CREATE ANOTHER BEFORE METHOD USING THIS NAME - THIS METHOD WILL THEN NOT RUN.
     */
    @Before
    public void beforeEveryTest(){
        DriverCapabilityManager driverCapabilityManager = new DriverCapabilityManager();
        driverFactory = new DriverFactory(driverCapabilityManager, springContext);
        testContext = new TestContext();
    }

    /**
     * Sets the initial value of variables to be equal to it's corresponding property value
     * (from application.properties). If you wish to use a different value for a variable defined here, overwrite
     * it in the beginning of your test.
     *
     * @param properties The list of property Keys to create as variables
     */
    @SuppressWarnings("unused")
    private void setDefaultVariablesUsingPropertyValue(String... properties){
        for(String propertyName : properties){
            setVariableValueUsingProperty(propertyName,propertyName);
        }
    }
    protected void setVariableValueUsingProperty(String variableName, String propertyName){
        setVariableValue(variableName,springContext.getEnvProperty(propertyName));
    }
    protected void setVariableValue(String variableName, String value){
        testContext.setVariable(variableName,value);
    }


    @Rule
    public TestName currentTest = new TestName();
    @Rule
    public final TestRule watchman = new TestWatcher() {
        @Override
        public Statement apply(Statement base, Description description) {
            return super.apply(base, description);
        }

        @Override
        public void starting(Description description){
            log.info("Test '" + description.getClassName() + "." + description.getMethodName() + "' starting...");
        }

        // This method gets invoked if the test fails for any reason:
        @Override
        protected void failed(Throwable e, Description description) {
            log.error("Test Failed: " + description.getDisplayName());
            log.error("Failing URL: " + driverFactory.getInstance().getCurrentUrl());
            log.error("Failing Title: " + driverFactory.getInstance().getTitle());
            log.error("Test Context: " + testContext.toString());

            String errorOutput;

            if( e.getMessage() != null &&
                e.getMessage().contains("Exception performing action")) {
                errorOutput = e.getMessage();
            } else {
                StringWriter stack = new StringWriter();
                e.printStackTrace(new PrintWriter(stack));
                errorOutput = "Stacktrace for failure in: " + description.getDisplayName() + "\n" + stack.toString();
            }

            log.error(errorOutput);
            DriverUtils.takeScreenshot(driverFactory.getInstance(),currentTest.getMethodName() + ".failed_state");
            DriverUtils.takeHTMLSnapshot(driverFactory.getInstance(),currentTest.getMethodName() + ".failed_state");
        }
        @Override
        protected void succeeded(Description description){
            log.info("Test Passed: " + description.getDisplayName());
        }
        // This method gets called when the test finishes, regardless of status
        // If the test fails, this will be called after the method above
        @Override
        protected void finished(Description description) {
            if (driverFactory.getInstance() != null){
                driverFactory.getInstance().quit();
            }
        }
    };

    protected void runWebAppActionsViaExcel(String actionFile){
        log.info("Starting Web App Actions: " + actionFile);
        runActions(actionFile, excelActionManager.getWebAppActions(actionFile));
    }
    protected void runWebAppActionsViaCSV(String actionFile){
        log.info("Starting Web App Actions: " + actionFile);
        runActions(actionFile, csvActionManager.getWebAppActions(actionFile));
    }

    protected void runDynamicsActionsViaExcel(String actionFile){
        log.info("Starting Dynamics Actions: " + actionFile);
        runActions(actionFile, excelActionManager.getDynamicsActions(actionFile));
    }
    protected void runDynamicsActionsViaCSV(String actionFile){
        log.info("Starting Dynamics Actions: " + actionFile);
        runActions(actionFile, csvActionManager.getDynamicsActions(actionFile));
    }

    protected void runActions(String listName, List<Action> actionList){
        int retryDelay = 5;

        for(Action action : actionList){
            try {
                try {
                    //log.info(action.toString()); //Add this line to log out all actions for debugging purposes
                    action.perform(driverFactory, testContext);
                }catch(WebDriverException e){
                    if(e.getMessage().contains("StaleElementReferenceException") ||
                    DriverUtils.elementExists(driverFactory.getInstance(),By.xpath("//span[contains(text(),'An error has occurred')]"))){
                        log.error("Webdriver Exception \"StaleElementReferenceException\" or Page Error " +
                                "\"An error has occurred\" has occurred, refreshing page and retrying action");
                        Thread.sleep(2000);
                        DriverUtils.refreshPage(driverFactory.getInstance());
                        Thread.sleep(5000);
                    } else {
                        log.error("WebDriverException occurred, waiting " + retryDelay + " seconds and trying again.");
                        Thread.sleep(retryDelay*1000);
                    }
                    action.perform(driverFactory, testContext);
                }
            } catch(Exception e){
                StringWriter stack = new StringWriter();
                e.printStackTrace(new PrintWriter(stack));

                fail("Exception performing action." +
                        "\nSheet Name: " + listName +
                        "\nLine Number: " + (actionList.indexOf(action)+2) +
                        "\nAction: " + action.toString() +
                        "\n" + stack.toString());
            }
        }
    }
}
