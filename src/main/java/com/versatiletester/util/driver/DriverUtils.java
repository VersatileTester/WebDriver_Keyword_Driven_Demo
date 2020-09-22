package com.versatiletester.util.driver;

import com.google.common.base.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Test Utility Class - used to contain all RemoteWebDriver related utility methods.
 */
@SuppressWarnings("WeakerAccess")
public abstract class DriverUtils {
    private static final Logger log = Logger.getLogger(DriverUtils.class);

    public static void refreshPage(RemoteWebDriver driver){
        driver.navigate().refresh();
        waitUntilPageFullyLoaded(driver);
    }
    public static void navigateTo(RemoteWebDriver driver, String url){
        driver.get(url);
        waitUntilPageFullyLoaded(driver);
    }
    public static boolean elementExists(RemoteWebDriver driver, By by){
        waitUntilPageFullyLoaded(driver);
        // Set a small element timeout because it is a try catch.
        driver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
        try{
            driver.findElement(by);
            DriverFactory.resetDriverTimeouts(driver);
            return true;
        } catch(NoSuchElementException e){
            DriverFactory.resetDriverTimeouts(driver);
            return false;
        }
    }
    public static byte[] getScreenshotBytes(RemoteWebDriver driver){
        return ((TakesScreenshot)driver).getScreenshotAs(OutputType.BYTES);
    }
    public static void takeScreenshot(RemoteWebDriver driver, String filePrefix){
        try {
            File screenshotFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshotFile,
                    new File(com.versatiletester.util.file.FileUtils.OUTPUT_FILE_DIRECTORY + "screenshots/" +
                    filePrefix + //"." + LocalDateTime.now().format(DateTimeFormatter.ofPattern("'T'yyyyMMddHHmmssS")) +
                    "." + driver.getSessionId() + ".png"));

        } catch (IOException ex) { log.error("Attempt to take screenshot failed, cause was: " + ex.getCause()); }
    }
    public static void takeHTMLSnapshot(RemoteWebDriver driver, String filePrefix){
        try {
            byte[] htmlSnapshot = driver.getPageSource().getBytes(Charsets.UTF_8);
            FileUtils.writeByteArrayToFile(
                    new File(com.versatiletester.util.file.FileUtils.OUTPUT_FILE_DIRECTORY + "html/" +
                    filePrefix + "." + LocalDateTime.now().format(DateTimeFormatter.ofPattern("'T'yyyyMMddHHmmssS")) +
                    "." + driver.getSessionId() + ".html"), htmlSnapshot);
        } catch (IOException ex) { log.error("Attempt to take screenshot failed, cause was: " + ex.getCause()); }
    }
    public static void clearFieldAndSendKeys(WebElement element, String text){
        element.clear();
        element.sendKeys(text);
    }
    public static void clearFieldAndSendKeys(RemoteWebDriver driver, By elementIdentifier, String text){
        driver.findElement(elementIdentifier).clear();
        driver.findElement(elementIdentifier).sendKeys(text);
    }

    public static void clickPageTransitionElement(RemoteWebDriver driver, WebElement element){
        element.click();
        waitUntilPageFullyLoaded(driver);
    }
    public static void clickPageTransitionElement(RemoteWebDriver driver, By elementIdentifier){
        clickPageTransitionElement(driver, driver.findElement(elementIdentifier));
    }
    public static void doubleClickPageTransitionElement(RemoteWebDriver driver, By elementIdentifier){
        Actions action = new Actions(driver);
        action.doubleClick(driver.findElement(elementIdentifier)).build().perform();
        //Wait just long enough for page to trigger dom refresh
        try { Thread.sleep(300); } catch (InterruptedException e) { e.printStackTrace(); }
        waitUntilPageFullyLoaded(driver);
    }
    public static void waitUntilPageFullyLoaded(RemoteWebDriver driver){
        
            try { Thread.sleep(300); } catch (InterruptedException e) { e.printStackTrace(); }
            new WebDriverWait(driver, 60).until(
                    webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
            /*if((Boolean)((JavascriptExecutor) driver).executeScript("return window.jQuery != undefined")){
                new WebDriverWait(driver, 30).until(
                        webDriver -> (Boolean)((JavascriptExecutor) webDriver).executeScript("return jQuery.active === 0"));
            }*/
            try { Thread.sleep(300); } catch (InterruptedException e) { e.printStackTrace(); }
    }
    public static boolean isElementVisible(RemoteWebDriver driver, By elementIdentifier){

        try{
            driver.findElement(elementIdentifier);
            return true;
        }catch(NoSuchElementException e){
            return false;
        }
    }

    

    
    /**
     * Generic utility method which enters text into an element based on it's value attribute.
     *
     * @param xpathElement - Used to specify an element's type more uniquely
     * @param value - The 'value' css attribute of the element
     */
    public static void clickByValue(RemoteWebDriver driver, String xpathElement, String value){
        WebElement element = driver.findElement(
                By.xpath("//" + xpathElement + "[@value='" + value + "']"));
        scrollToElement(driver,element);
        clickPageTransitionElement(driver,element);
    }
    /**
     * Generic utility method which clicks an element based on it's text.
     *
     * @param xpathElement - Used to specify an element's type more uniquely
     * @param text - The search text of the element
     */
    public static void clickByText(RemoteWebDriver driver, String xpathElement, String text){
        WebElement element = driver.findElement(
                By.xpath("//" + xpathElement + "[text()[contains(normalize-space(.),'" + text + "')]]"));
        //scrollToElement(element);
        clickPageTransitionElement(driver,element);
    }
    /** Will scroll to the element provided, meeting the prerequisite of visibility before interaction. */
    public static void scrollToElement(RemoteWebDriver driver, WebElement element){
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }
    /** Overload method for clickByText which clicks any(the first) matching element */
    public static void clickByText(RemoteWebDriver driver, String text){ clickByText(driver,"*",text); }
    /**
     * Generic utility method which finds any element with the specified search text, returning the element found.
     *
     * @param searchString - The text used to search for within the current page (ignoring irregular formatting).
     * @return WebElement - The element found on the current page, or null if the element is not found.
     */
    public static boolean isStringVisible(RemoteWebDriver driver, String xpathElement, String searchString){

        waitUntilPageFullyLoaded(driver);
        // Set a small element timeout because it is a try catch.
        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
        try{
            driver.findElement(By.xpath(xpathElement + "[text()[contains(normalize-space(.),'" + searchString + "')]]"));
            DriverFactory.resetDriverTimeouts(driver);
            return true;
        } catch(NoSuchElementException e){
            DriverFactory.resetDriverTimeouts(driver);
            return false;
        }
    }
    /** Overload method for isStringVisible which matches any element regardless of identifier */
    public static boolean isStringVisible(RemoteWebDriver driver, String searchString){
        return isStringVisible(driver,"//*", searchString);
    }
    /**
     * Generic utility method which determines whether an alert matching the alert text is visible.
     *
     * @param alert - text used to determine if there is an expected alert visible.
     * @return true if an alert with the specified text is visible, false if not.
     */
    public static boolean isAlertVisible(RemoteWebDriver driver, String alert){
        Alert alertWindow = new WebDriverWait(driver, 2).until(ExpectedConditions.alertIsPresent());

        driver.switchTo().alert();
        if(normaliseString(alertWindow.getText()).equals(alert)){
            alertWindow.accept();
            return true;
        } else {
            alertWindow.accept();
            return false;
        }
    }
    /** Internal utility method which formats all input strings correctly by removing erroneous formatting */
    private static String normaliseString(String input){
        return input.trim().replace("\n", " ").replace("  ", " ");
    }
}


