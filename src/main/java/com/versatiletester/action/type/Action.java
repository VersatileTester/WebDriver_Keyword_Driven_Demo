package com.versatiletester.action.type;

import com.versatiletester.config.TestContext;
import com.versatiletester.util.driver.DriverFactory;

public interface Action {

    void perform(DriverFactory driverFactory, TestContext testContext);
    void setIdentifierType(String identifierType);
    void setIdentifierValue(String identifierValue);
    void setValue(String value);
}
