package com.versatiletester.util.driver;

public enum GDSBrowsers {
    WINDOWS_IE("winIE","Windows","10","Internet Explorer","11"),
    WINDOWS_EDGE("winEdge","Windows","10","Edge","17"),
    WINDOWS_CHROME("winChrome","Windows","10","Chrome","70"),
    WINDOWS_FIREFOX("winFF","Windows","10","Firefox","63"),
    OSX_SAFARI("osxSafari","OS X","Mojave","Safari","12"),
    OSX_CHROME("osxChrome","OS X","Mojave","Chrome","70"),
    OSX_FIREFOX("osxFF","OS X","Mojave","Firefox","63"),
    IPAD_SAFARI("ipadSafari","iPad Pro","11.2","Safari",null),
    SAMSUNG_CHROME("samsungChrome","Samsung Galaxy Note 8","7.1","Google Chrome",null);

    private String description;
    private String os;
    private String osVersion;
    private String browser;
    private String browserVersion;

    GDSBrowsers(String description, String os, String osVersion, String browser, String browserVersion){
        this.description = description;
        this.os = os;
        this.osVersion = osVersion;
        this.browser = browser;
        this.browserVersion = browserVersion;
    }

    public String getDescription(){ return this.description; }
    public String getOs() { return os; }
    public String getOsVersion() { return osVersion; }
    public String getBrowser() { return browser; }
    public String getBrowserVersion() { return browserVersion; }

    public String toString(){
        return  this.description + " : " +
                this.os + " : " +
                this.osVersion + " : " +
                this.browser + " : " +
                this.browserVersion;
    }

    public static GDSBrowsers getMatch(String text){
        for(GDSBrowsers browser : GDSBrowsers.values()){
            if(browser.description.equalsIgnoreCase(text)){
                return browser;
            }
        }
        throw new RuntimeException("GDS Browser '" + text + "' unsupported.");
    }
}
