package com.versatiletester.action;

import com.versatiletester.config.SpringContext;
import com.versatiletester.config.TestContext;
import com.versatiletester.util.driver.DriverFactory;
import com.versatiletester.util.driver.DriverUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DynamicsAction extends TestAction {
    private static final Logger log = Logger.getLogger(DynamicsAction.class);

    public DynamicsAction(String actionTypeDataString, SpringContext springContext) { super(actionTypeDataString, springContext); }

    @Override
    public void perform(DriverFactory driverFactory, TestContext testContext){
        switch(actionType) {
            case CLICK_IF_EXISTS: {}
            case FIELD_IF_EXISTS:{
                DriverUtils.waitUntilPageFullyLoaded(driverFactory.getInstance());
                super.perform(driverFactory,testContext);
                bypassLoadingScreen(driverFactory.getInstance());
                break;
            }
            case CLICK: {}
            case DOUBLE_CLICK: {}
            case ASSERT_ELEMENT: {}
            case ASSERT_ELEMENT_TEXT: {}
            case UPLOAD_FILE: {
                super.perform(driverFactory,testContext);
                bypassLoadingScreen(driverFactory.getInstance());
                break;
            }
            case FIELD:{
                driverFactory.getInstance().findElement(getUsableElementIdentifier()).click();
                try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
                super.perform(driverFactory,testContext);
                bypassLoadingScreen(driverFactory.getInstance()); //Possibly not required for this action
                break;
            }
            case SWITCH_TO: {
                try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
                super.perform(driverFactory,testContext);
                break;
            }
            case NAVIGATE: {
                super.perform(driverFactory,testContext);
                try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
                break;
            }
            case SCREENSHOT: {}
            case WAIT: {}
            case CLOSE_TAB: {}
            case SET_ELEMENT_TEXT: {}
            case BREAK_POINT:{
                super.perform(driverFactory,testContext);
                break;
            }
            case DYNAMIC_FIELD: {
                String textToEnter;
                if (value.startsWith("<")) {
                    textToEnter = springContext.getEnvProperty(value.replaceAll("[<>]", ""));
                } else if(value.startsWith("$")){
                    textToEnter = testContext.getVariable(value.replaceAll("[$]", ""));
                } else {
                    textToEnter = value;
                }

                WebElement field = driverFactory.getInstance().findElement(getUsableElementIdentifier());

                DriverUtils.scrollToElement(driverFactory.getInstance(),field);

                //Click on the field
                field.click();

                try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }

                //Enter the text
                field.sendKeys(textToEnter);

                //Wait for the dropdown - Max wait time is 2 seconds on slower machines. Cannot be determined dynamically.
                try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

                //Select the first element in the list
                field.sendKeys(Keys.TAB);
                try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }

                driverFactory.getInstance().switchTo().activeElement().sendKeys(Keys.TAB);
                try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }

                //Fields with the 'Recent Records' link need a 3rd tab key to be entered
                if(DriverUtils.elementExists(driverFactory.getInstance(),By.xpath("//button[text()='Recent records']"))){
                    driverFactory.getInstance().switchTo().activeElement().sendKeys(Keys.TAB);
                    try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
                }

                driverFactory.getInstance().switchTo().activeElement().sendKeys(Keys.ENTER);

                break;
            }
            case FIELD_NO_CLEAR:{
                String textToEnter;

                if (value.startsWith("<")) {
                    textToEnter = springContext.getEnvProperty(value.replaceAll("[<>]", ""));
                } else if(value.startsWith("$")){
                    textToEnter = testContext.getVariable(value.replaceAll("[$]", ""));
                } else {
                    textToEnter = value;
                }

                driverFactory.getInstance().findElement(getUsableElementIdentifier()).click();
                driverFactory.getInstance().findElement(getUsableElementIdentifier()).sendKeys(textToEnter);
                break;
            }
        }
    }

    private void bypassLoadingScreen(RemoteWebDriver driver){
        int overlayTimeoutSeconds = 20;

        if(DriverUtils.elementExists(driver,By.xpath("//span[text() = 'Saving...']"))){
            log.info("'Saving' overlay detected");
            WebDriverWait wait = new WebDriverWait(driver, overlayTimeoutSeconds);
            wait.until(ExpectedConditions.invisibilityOf(driver.findElement(By.xpath("//span[text() = 'Saving...']"))));
            log.info("'Saving' overlay complete");
        }
        if(DriverUtils.elementExists(driver,By.xpath("//span[text() = 'Dispatching Certificates. Please Wait..']"))){
            log.info("'Dispatching Certificates' overlay detected");
            WebDriverWait wait = new WebDriverWait(driver, overlayTimeoutSeconds);
            wait.until(ExpectedConditions.invisibilityOf(driver.findElement(
                    By.xpath("//span[text() = 'Dispatching Certificates. Please Wait..']"))));
            log.info("'Dispatching Certificates' overlay complete");
        }
    }
}
