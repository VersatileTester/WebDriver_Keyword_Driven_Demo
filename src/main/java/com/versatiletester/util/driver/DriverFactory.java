package com.versatiletester.util.driver;

import com.google.common.base.Preconditions;
import com.versatiletester.config.MavenProfiles;
import com.versatiletester.config.SpringContext;
import com.versatiletester.util.file.FileUtils;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class DriverFactory {
    private static final Logger log = Logger.getLogger(DriverFactory.class);

    private static final String LOCAL_DRIVER_BINARY_PATH = FileUtils.convertPathToUniversal("src/main/resources/binaries/");

    private static final String SAFARI_DRIVER_VERSION = "13.1";

    private RemoteWebDriver driver;
    private DriverCapabilityManager driverCapabilityManager;
    private SpringContext springContext;

    public enum LocalBrowser{
        FIREFOX("firefox"),
        SAFARI("safari"),
        CHROME("chrome");

        private String description;
        LocalBrowser(String browser){this.description = browser;}
        public String toString(){return this.description;}
        public static LocalBrowser getMatch(String text){
            for(LocalBrowser browser : LocalBrowser.values()){
                if(browser.toString().equals(text)){
                    return browser;
                }
            }
            throw new RuntimeException("Local Browser '" + text + "' unsupported.");
        }
    }

    public DriverFactory(DriverCapabilityManager driverCapabilityManager, SpringContext springContext){
        this.driverCapabilityManager = driverCapabilityManager;
        this.springContext = springContext;
    }

    public RemoteWebDriver getInstance() {
        if(driver == null || driver.getSessionId() == null){
            log.info("Maven Profile: " + springContext.getEnvProperty(SpringContext.MAVEN_PROFILE_PROPERTY_NAME));
            switch(MavenProfiles.getMatch(springContext.getEnvProperty(SpringContext.MAVEN_PROFILE_PROPERTY_NAME))){
                case LOCAL:{
                    log.info("Local Maven profile activated, instantiating driver");
                    driver = instantiateLocalDriver();
                    break;
                }
                case GRID:{
                    Preconditions.checkArgument(springContext.getEnvProperty(SpringContext.GRID_URL_PROPERTY_NAME) != null,
                            "In order to use the selenium grid maven profile, pass in the grid url as " +
                                    "'-Dgrid.url=http://127.0.0.1:4444/hub/wb' or set it in the grid config file.");
                    driver = instantiateRemoteDriver(
                            springContext.getEnvProperty(SpringContext.GRID_URL_PROPERTY_NAME),
                            driverCapabilityManager.generateDefaultChromeCapabilities(
                                springContext.getEnvProperty(SpringContext.PROJECT_NAME_PROPERTY_NAME),
                                springContext.getEnvProperty(SpringContext.BUILD_NUM_PROPERTY_NAME)));
                    break;
                }
                case BROWSERSTACK:{
                    //TODO: Add Preconditions to bstack profile
                    log.info("Browserstack Maven profile activated, instantiating driver");

                    GDSBrowsers gdsBrowser = GDSBrowsers.getMatch(springContext.getEnvProperty(SpringContext.BROWSER_PROPERTY_NAME));

                    Preconditions.checkArgument(gdsBrowser != null, "Unknown browser defined: " +
                                    springContext.getEnvProperty(SpringContext.BROWSER_PROPERTY_NAME));

                    // Null values from mobile devices handled in the 'generateBrowserStackCapabilities' method.
                    driver = instantiateRemoteDriver(
                            springContext.getEnvProperty(SpringContext.BSTACK_URL_PROPERTY_NAME),
                            driverCapabilityManager.generateBrowserStackCapabilities(
                                    gdsBrowser,
                                    springContext.getEnvProperty(SpringContext.PROJECT_NAME_PROPERTY_NAME),
                                    springContext.getEnvProperty(SpringContext.BUILD_NUM_PROPERTY_NAME),
                                    springContext.getEnvProperty(SpringContext.LOCAL_ID_PROPERTY_NAME),
                                    Boolean.parseBoolean(springContext.getEnvProperty(
                                            SpringContext.BSTACK_LOCAL_BOOL_PROPERTY_NAME))));
                    break;
                }
            }
            resetDriverTimeouts(driver);
            setupBrowserWindow();
        }
        return driver;
    }

    private RemoteWebDriver instantiateLocalDriver(){
        switch(springContext.getEnvProperty(SpringContext.BROWSER_PROPERTY_NAME).toLowerCase()){
            case "firefox":{
                return getFirefoxDriver();
            }
            case "safari":{
                return getSafariDriver();
            }
            default:{} //Generate a chrome instance by default.
            case "chrome":{
                return getChromeDriver();
            }
        }
    }
    private RemoteWebDriver instantiateRemoteDriver(String url, Capabilities caps) {
        try {
            return new RemoteWebDriver(new URL(url),caps);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static SafariDriver getSafariDriver() {
        String safariBinaryPath = "";

        Preconditions.checkArgument(SystemUtils.IS_OS_MAC, "Incorrect platform");

        safariBinaryPath = LOCAL_DRIVER_BINARY_PATH + "safaridriver-v" + SAFARI_DRIVER_VERSION + "-macos/safaridriver";

        File f = new File(safariBinaryPath);
        System.setProperty("webdriver.safari.driver", f.getAbsolutePath());

        SafariDriver driver = new SafariDriver(new SafariOptions());
        return driver;
    }

    private static FirefoxDriver getFirefoxDriver() {
        WebDriverManager.firefoxdriver().version("0.26").setup();
        return new FirefoxDriver(new FirefoxOptions());
    }
    private ChromeDriver getChromeDriver() {
        WebDriverManager.chromedriver().version("85").setup();
        return new ChromeDriver();
    }


    /**
     * Call this method to reset the driver instance - Completely closes the browser and instantiates a new one based
     * on the existing properties / requested configuration.
     */
    public RemoteWebDriver resetDriverSession(){
        if(this.driver != null){
            this.driver.quit();
        }
        return getInstance();
    }

    public static void resetDriverTimeouts(RemoteWebDriver driver){
        /* The driver will wait x seconds for every page to load */
        driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
        /* The driver will wait x seconds for every element to be visible
         Increased the timeout to 10 seconds as IE needs more than 5 */
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    private void setupBrowserWindow(){
        MavenProfiles profile = MavenProfiles.getMatch(springContext.getEnvProperty(SpringContext.MAVEN_PROFILE_PROPERTY_NAME));

        switch(profile){
            case BROWSERSTACK:{
                GDSBrowsers browser = GDSBrowsers.getMatch(springContext.getEnvProperty(SpringContext.BROWSER_PROPERTY_NAME));

                //Maximize doesn't work on mobile operating systems
                if( browser != GDSBrowsers.OSX_CHROME &&
                    browser != GDSBrowsers.SAMSUNG_CHROME &&
                    browser != GDSBrowsers.IPAD_SAFARI )
                {
                    driver.manage().window().maximize();
                }

                //For OSX Chrome, you need to fullscreen the browser instead of maximising
                if(browser == GDSBrowsers.OSX_CHROME){
                    driver.manage().window().fullscreen();
                }

                break;
            }
            case GRID:{ } //Grid/Local profiles are the same logic
            case LOCAL:{
                if( SystemUtils.IS_OS_MAC &&
                    springContext.getEnvProperty(SpringContext.BROWSER_PROPERTY_NAME).equalsIgnoreCase("chrome") )
                {
                    driver.manage().window().fullscreen();
                } else {
                    driver.manage().window().maximize();
                }
                break;
            }
        }
    }
}
