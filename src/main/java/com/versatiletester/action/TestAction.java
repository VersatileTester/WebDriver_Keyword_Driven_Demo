package com.versatiletester.action;

import com.versatiletester.action.type.Action;
import com.versatiletester.action.type.ActionType;
import com.versatiletester.action.type.IdentifierType;
import com.versatiletester.config.MavenProfiles;
import com.versatiletester.config.SpringContext;
import com.versatiletester.config.TestContext;
import com.versatiletester.util.driver.DriverFactory;
import com.versatiletester.util.driver.DriverUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class TestAction implements Action {
    private static final Logger log = Logger.getLogger(TestAction.class);

    protected ActionType actionType;
    protected IdentifierType identifierType;
    protected String identifierValue;
    protected String value;

    protected SpringContext springContext;

    public TestAction(String actionTypeDataString, SpringContext springContext) {
        this.actionType = ActionType.getMatch(actionTypeDataString);
        this.springContext = springContext;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public IdentifierType getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(String identifierTypeDataString) {
        this.identifierType = IdentifierType.getMatch(identifierTypeDataString);
    }

    public String getIdentifierValue() {
        return identifierValue;
    }

    public void setIdentifierValue(String identifierValue) {
        this.identifierValue = identifierValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void perform(DriverFactory driverFactory, TestContext testContext) {
        switch (actionType) {
            case CLICK: {
                DriverUtils.clickPageTransitionElement(driverFactory.getInstance(), getUsableElementIdentifier());
                break;
            }
            case CLICK_IF_EXISTS: {
                if (this.identifierType == IdentifierType.TEXT) {
                    if (DriverUtils.isStringVisible(driverFactory.getInstance(), identifierValue)) {
                        DriverUtils.clickByText(driverFactory.getInstance(), identifierValue);
                    }
                } else {
                    if (DriverUtils.elementExists(driverFactory.getInstance(), getUsableElementIdentifier())) {
                        DriverUtils.clickPageTransitionElement(driverFactory.getInstance(), getUsableElementIdentifier());
                    }
                }
                break;
            }
            case DOUBLE_CLICK: {
                if (this.identifierType == IdentifierType.TEXT) {
                    DriverUtils.clickByText(driverFactory.getInstance(), identifierValue);
                } else {
                    DriverUtils.doubleClickPageTransitionElement(driverFactory.getInstance(), getUsableElementIdentifier());
                }
                break;
            }
            case FIELD_IF_EXISTS: {
                if (!DriverUtils.elementExists(driverFactory.getInstance(), getUsableElementIdentifier())) {
                    break;
                } //Otherwise, don't break and execute the following 'FIELD' case
            }
            case FIELD: {
                String textToEnter = "";
                if (value.startsWith("<")) {
                    textToEnter = springContext.getEnvProperty(value.replaceAll("[<>]", ""));
                } else if(value.startsWith("$")){
                    textToEnter = testContext.getVariable(value.replaceAll("[$]", ""));
                } else {
                    textToEnter = value;
                }
                DriverUtils.clearFieldAndSendKeys(driverFactory.getInstance(), getUsableElementIdentifier(),textToEnter);
                break;
            }
            case SET_TODAY_PLUS: {
                int daysToAdd = Integer.parseInt(value);
                LocalDate today = LocalDate.now();
                LocalDateTime time = LocalDateTime.now();
                LocalDate dateRequested = today.plus(daysToAdd, ChronoUnit.DAYS);
                String dateSeparator = "/";

                testContext.setVariable(identifierValue+".Day",
                        String.valueOf(dateRequested.getDayOfMonth()));
                testContext.setVariable(identifierValue+".Month",
                        String.valueOf(dateRequested.getMonthValue()));
                testContext.setVariable(identifierValue+".Year",
                        String.valueOf(dateRequested.getYear()));
                testContext.setVariable(identifierValue + ".DateTime",
                        "AutoEHC-" + today + "-" + time.get(ChronoField.MILLI_OF_SECOND));

                testContext.setVariable(identifierValue+".DDMMYYYY",
                        dateRequested.getDayOfMonth() + dateSeparator +
                                dateRequested.getMonthValue() + dateSeparator +
                                dateRequested.getYear());
                testContext.setVariable(identifierValue+".MMDDYYYY",
                        dateRequested.getMonthValue() + dateSeparator +
                                dateRequested.getDayOfMonth() + dateSeparator +
                                dateRequested.getYear());
                break;
            }
            case NAVIGATE: {
                if (value.startsWith("<")) {
                    DriverUtils.navigateTo(driverFactory.getInstance(),
                            springContext.getEnvProperty(value.replaceAll("[<>]", "")));
                } else if(value.startsWith("$")){
                    DriverUtils.navigateTo(driverFactory.getInstance(),
                            testContext.getVariable(value.replaceAll("[$]", "")));
                } else if (value.equalsIgnoreCase("back")) {
                    driverFactory.getInstance().navigate().back();
                } else if (value.equalsIgnoreCase("refresh")) {
                    driverFactory.getInstance().navigate().refresh();
                }
                DriverUtils.waitUntilPageFullyLoaded(driverFactory.getInstance());
                break;
            }
            case SCREENSHOT: {
                DriverUtils.waitUntilPageFullyLoaded(driverFactory.getInstance());
                DriverUtils.takeScreenshot(driverFactory.getInstance(), value);
                break;
            }
            case ASSERT_ELEMENT_TEXT: {
                log.info("Asserting element (" + this.toString() + ") has text '" + value + "'");
                assertEquals(value, driverFactory.getInstance().findElement(getUsableElementIdentifier())
                        .getText().trim().replaceAll("\\s"," "));
                break;
            }
            case ASSERT_ELEMENT: {
                if(value.equalsIgnoreCase("exists")){
                    log.info("Asserting element (" + this.toString() + ") exists.");
                    assertTrue(DriverUtils.elementExists(driverFactory.getInstance(),getUsableElementIdentifier()));
                    break;
                }
                if(value.equalsIgnoreCase("not exists")){
                    log.info("Asserting element (" + this.toString() + ") does not exists.");
                    assertFalse(DriverUtils.elementExists(driverFactory.getInstance(),getUsableElementIdentifier()));
                    break;
                }
                if(value.equalsIgnoreCase("enabled")){
                    log.info("Asserting element (" + this.toString() + ") is enabled.");
                    assertTrue(driverFactory.getInstance().findElement(getUsableElementIdentifier()).isEnabled());
                    break;
                }
                if(value.equalsIgnoreCase("disabled")){
                    log.info("Asserting element (" + this.toString() + ") is disabled.");
                    assertFalse(driverFactory.getInstance().findElement(getUsableElementIdentifier()).isEnabled());
                    break;
                }
                throw new UnsupportedOperationException("Assert Element value '" + value + "' unsupported.");
            }
            case SWITCH_TO: {
                switch (identifierType) {
                    case TAB: {
                        ArrayList<String> tabs = new ArrayList<>(driverFactory.getInstance().getWindowHandles());
                        driverFactory.getInstance().switchTo().window(tabs.get(Integer.parseInt(identifierValue) - 1));
                        break;
                    }
                    case FRAME: {
                        if(DriverUtils.elementExists(driverFactory.getInstance(),By.id(identifierValue))){
                            driverFactory.getInstance().switchTo().frame(identifierValue);
                        }
                        break;
                    }
                    case DEFAULT:{
                        driverFactory.getInstance().switchTo().defaultContent();
                        break;
                    }
                    case TEXT: { }
                    case ID: { }
                    case NAME: { }
                    case XPATH: {
                        throw new RuntimeException("IdentifierType Unsupported for SWITCH_TO Action: " + identifierType.name());
                    }
                }
                break;
            }
            case SELECT: {
                Select dropdown = new Select(driverFactory.getInstance().findElement(getUsableElementIdentifier()));
                dropdown.selectByVisibleText(value);
                break;
            }
            case WAIT: {
                try {
                    Thread.sleep(Integer.parseInt(value));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
            case UPLOAD_FILE: {
                if (!springContext.getEnvProperty(SpringContext.MAVEN_PROFILE_PROPERTY_NAME)
                        .equalsIgnoreCase(MavenProfiles.BROWSERSTACK.toString())) {
                    DriverUtils.clearFieldAndSendKeys(driverFactory.getInstance(),
                            getUsableElementIdentifier(),
                            System.getProperty("user.dir")
                                    +File.separator+"src"+File.separator+"main"+File.separator+"resources"+File.separator+"upload"+File.separator + value);
                } //else Skip all upload steps when running through BrowserStack
                break;
            }
            case CLOSE_TAB:{
                driverFactory.getInstance().close();
                break;
            }
            case SET_ELEMENT_TEXT:{
                String elementText = driverFactory.getInstance().findElement(getUsableElementIdentifier()).getText();
                testContext.setVariable(value,elementText);
                break;
            }
            case SET_CURRENT_URL:{
                testContext.setVariable(value,driverFactory.getInstance().getCurrentUrl());
                break;
            }
            case BREAK_POINT:{
                log.info("This is a break point");
                break;
            }
        }
    }

    protected By getUsableElementIdentifier() {
        By elementIdentifier = null;

        if (identifierType != null && identifierValue != null) {
            switch (identifierType) {
                case NAME: {
                    elementIdentifier = By.name(identifierValue);
                    break;
                }
                case ID: {
                    elementIdentifier = By.id(identifierValue);
                    break;
                }
                case XPATH: {
                    elementIdentifier = By.xpath(identifierValue);
                    break;
                }
                case TEXT: {
                    elementIdentifier = By.xpath("//*[text()[normalize-space() = '" + identifierValue + "']]");
                    // //label[//text()[normalize-space() = 'some label']]
                    break;
                }
                case TEXT_CONTAINS: {
                    elementIdentifier = By.xpath("//*[text()[contains(normalize-space(.),'" + identifierValue + "')]]");
                    break;
                }
            }
        } else {
            throw new RuntimeException("DATA ERROR - Unsupported IdentifierType/IdentifierValue: " +
                    "IdentifierType=" + identifierType + " IdentifierValue=" + identifierValue);
        }
        return elementIdentifier;
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append("ActionType: ");
        description.append(this.actionType.name());
        description.append("; ");
        if (identifierType != null) {
            description.append("IdentifierType: ");
            description.append(this.identifierType.toString());
            description.append("; ");
        }
        if (identifierValue != null) {
            description.append("IdentifierValue: ");
            description.append(this.identifierValue);
            description.append("; ");
        }
        if (value != null) {
            description.append("Value: ");
            description.append(this.value);
            description.append("; ");
        }
        return description.toString();
    }
}