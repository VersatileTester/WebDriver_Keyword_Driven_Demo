# Keyword-Driven Selenium WedDriver Test Suite

## Requirements
Please make sure to have Java 1.8 and version 3+ of Maven installed, as these will enable all other dependencies.

## Usage

### Profiles

------------------------

#### Local (Default)
The local profile is, as you'd expect, to run the suite on your local dev instance.

------------------------

#### BrowserStack ('bstack')
The bstack profile can be used to run against all instances of BrowserStack (Local + public cloud). It has a 
set of pre-defined browser/device combinations matching the GDS compatibility requirements (all combinations that are 
supported within BrowserStack).

------------------------

#### Grid
The grid profile can be used to run against a local running Selenium Grid. This is required to scale the tests 
in parallel.

------------------------

### How to run
All additional arguments are optional and will overwrite the config and pom based properties. The only requirement 
is to run the suite using `mvn verify` as opposed to `mvn test` as the reporting mechanism is linked to the 
former maven stage.

Maven command format:
```
mvn clean verify -P<profile> -Dtest=<TestClassName>
```

Example:
```
mvn clean verify -Pgrid -Dtest=Regression_Suite -Denvironment=local
```

------------------------

### Full list of configurable properties
`parallel.tests` Defaults to 1 local, 5 bstack, 10 grid. The number of simultaneous JVM processes spawned to run 
tests. I.e. the number of tests to run in parallel. 

`browser` Defaults to 'default' (pom.xml), which, within the framework will generate a chrome instance - Always 
required when using the bstack profile.

`environment` Defaults to 'sandpit' (config.properties). Sets the target environment to run the suite against. 

`ALL config values` All the config values defined (config.properties) can be overridden by passing in the 
value as an argument (see above).

------------------------

#### Profile specific properties

##### Grid
`grid.url` Defaults to `http://127.0.0.1:4444/wd/hub`, which should always target the correct local instance 
(regardless of running on localhost vs DevOps).   

------------------------

##### BrowserStack
`browserstack.local` Defaults to false (Required value for DevOps). If using the public Browserstack platform, 
change this to false.

`browserstack.url` No default value set (it is passed in via DevOps). If using the public Browserstack platform, 
change this to your provided URL.

`local.id` No default value set (it is passed in via DevOps). Represents a unique identifier for each local 
connection when multiple local connections are connected.

`build` Defaults to a date-time stamp in the format "yyyyMMdd'T'HHmmss" if not defined. Used (in combination with 
the 'projectname' property) to identify the tests within BrowserStack.

------------------------

### Pre-built Scripts
`scripts/browserstack/<browser>.sh` Will run the BrowserStack test suite using the bstack profile for the target 
browser/device combination, required for pipeline runs against DevOps.

`scripts/functional/<TestSuite>.sh` Will run the specified test suite using the grid profile, designed for DevOps.

------------------------

### CSV Action Lists
 
The implementation of the CSV action lists means that;
 - If you have a comma in a cell (quite likely when using more nuanced XPath), you need to wrap that cell with 
 quotation marks
 - Timing-resiliency is built into the foundations of the framework, so any 'Wait' actions added must be justified 
 (with a comment outlining the reason in the 'Notes' column of the action).
 - Any actions that you wish to comment out temporarily can be done by adding a '#' character to the beginning of the 
 line, for example, `Click, ID, button-login,,` would be commented out by using `#Click, ID, button-login,,`. 

------------------------

#### Actions

------------------------
```
[Click]             //Click on an element
[Click Optional]    //Click on an element if it exists
[Double Click]      //Double click on an element
```
Requires:
- Identifier Type
- Identifier Value

Equivalence: `driver.findElement(By.<IdentifierType>(<IdentifierValue>)).click();`

------------------------

```
[Field]             //Send a value to a given element
[Field Optional]    //Send a value to a given element if it exists
```

Requires:
- Identifier Type
- Identifier Value
- Value

Equivalence: `driver.findElement(By.<IdentifierType>(<IdentifierValue>)).sendKeys(<Value>);`

------------------------

```
[Dynamics Field]    //Send a value to a given Dynamics dropdown field  
```

Requires:
- Identifier Type
- Identifier Value
- Value

NOTE: This is used for fields in Dynamics that use javascript dropdowns in order to select the correct value.

------------------------

```
[Field No Clear]    //Send a value to a given element without clearing the field first - Required for some fields in Dynamics
```

Requires:
- Identifier Type
- Identifier Value
- Value

NOTE: This is used for fields in Dynamics that require the user to NOT clear the field beforehand 
(otherwise the field-value is repopulated by Dynamics before any following send-keys data).

------------------------

```
[Navigate]          //Navigate to the specified value (URL, Property or special function)
```

Requires:
- Value [1]

Special Functions:
- [2] 'back' will navigate the browser to the previous page.
- [3] 'refresh' will refresh the current page.

For example:
`navigate,,,refresh`

Equivalence: 
- [1] `driver.get(<value>);`
- [2] `driver.navigate().back();`
- [3] `driver.navigate().refresh();`

------------------------

```
[Screenshot]        //Take a screenshot of the existing page and label it using the value provided
```

Requires:
- Value

Note:

Naming convention is `<value>.<driverSessionID>.png`

------------------------

```
[Assert Element Text]   //Assert that an element has the specified text (exact match only)
```

Requires:
- Identifier Type
- Identifier Value
- Value

Equivalence: 
- [1] `assertEquals(<value>, driver.findElement(By.<IdentifierType>(<IdentifierValue>)).getText());`

------------------------

```
[Assert Element]    //Assert a specific condition (value) against a specified element (Identifier Type/Value)
```

Requires:
- Identifier Type
- Identifier Value
- Value

Special Functions:
- [1] A Value of `Exists` will assert that the specified element exists within the DOM structure
- [2] A Value of `Not Exists` will assert that the specified element does not exist within the DOM structure 
- [3] A Value of `Enabled` will assert that the specified element is enabled.
- [4] A Value of `Disabled` will assert that the specified element is disabled.

Equivalence: 
- [1] `assertTrue(elementExists(driver.findElement(By.<IdentifierType>(<IdentifierValue>))));`
- [2] `assertFalse(elementExists(driver.findElement(By.<IdentifierType>(<IdentifierValue>))));`
- [3] `assertTrue(driver.findElement(By.<IdentifierType>(<IdentifierValue>)).isEnabled());`
- [3] `assertFalse(driver.findElement(By.<IdentifierType>(<IdentifierValue>)).isEnabled());`

------------------------
```
[Switch To]         //Switch the driver context to a Tab or Frame
```

Requires:
- Identifier Type

Special Functions:
- [1] An Identifier Type of `Tab` will switch to the tab number corresponding to the Identifer Value (indexing from 1).
- [2] An Identifier Type of `Frame` will switch to a frame element with the ID of the Identifier Value.
- [3] An Identifier Type of `Default` will switch back to the default driver window context (required to call after switching tabs/frames).

Equivalence: 
- [1] `driver.switchTo().window(tabs.get(<identifierValue> - 1));`
- [2] `driver.switchTo().frame(<identifierValue>);`
- [3] `driver.switchTo().defaultContent();`


------------------------

```
[Select]             //Select a text option from a Select element (dropdown) on the page
```

Requires:
- Identifier Type
- Identifier Value
- Value

Equivalence: 
> `Select selectDropdown = new Select(driver.findElement(By.<IdentifierType>(<IdentifierValue>)));`
> `selectDropdown.selectByVisibleText(<value>);`

------------------------

```
[Wait]          //Halts the execution of the test thread for X milliseconds
```

Requires:
- Value

Equivalence: `Thread.sleep(<value>);`

------------------------

```
[Upload File]                   
```

Requires:
- Identifier Type
- Identifier Value
- Value

Equivalence: `driver.findElement(By.<IdentifierType>(<IdentifierValue>)).sendKeys("./upload/"+<value>);`

Note:
Upload file functionality is skipped when running using the Browserstack ('bstack') profile.

------------------------

```
[Set Today Plus]    //Set a defined variable as a dynamic date
```

Requires:
- Identifier Value (Variable Name - Case Sensitive)
- Value (Number of days to add to current date)

Example:

Action: `Set Today Plus,,movingDate,2,`

Outcome: 6 usable variables containing the date-components targeting 2 days in the future can be accessed using;
- "movingDate.Day"
- "movingDate.Month"
- "movingDate.Year"
- "movingDate.Localtime"
- "movingDate.DDMMYYYY"
- "movingDate.MMDDYYYY"

------------------------

```
[Close Tab]             //Closes the current tab
```

Requires no configuration.

------------------------

```
[Set Current Url]       //Creates a referencable variable containing the current URL
```

Requires:
- Value (Name of the variable to store the current URL)

Equivalence: `testContext.setVariable(<value>,driver.getCurrentUrl());`

------------------------
```
[Set Element Text]      //Creates a referencable variable containing the text of a found element
```

Requires:
- Identifier Type
- Identifier Value
- Value (Name of the variable to store the element's text)

Equivalence:
```
String elementText = driver.findElement(By.<IdentifierType>(<IdentifierValue>)).getText();
testContext.setVariable(value,elementText);
```

------------------------

```
[Break Point]           //A DEBUGGING-ONLY action that allows for breakpointing mid-CSV files. (DO NOT COMMIT THESE ACTION TO THE CODEBASE)
```

Requires no configuration.

To use, place a breakpoint on the following line inside of the BREAK_POINT case of the TestAction.perform(...) method. 

`log.info("This is a break point");`

------------------------

#### Variables/Properties
All dynamic values defined within the framework (ApplicationID, CaseID, Dynamic/Future Date Ranges) can be accessed using a '$' in the `value` of the action.

Example:
- In order to enter the 'appID' variable value into a field with an ID of 'field1', you would use the Action `Field,ID,field1,$appID,`
- In order to enter a previously set 'movingDate' (using the 'Set Today Plus' action) into a field with Xpath of "//input[@qa-data-id='moving-month']", you would use the Action `Field,Xpath,//input[@qa-data-id='moving-month'],$movingDate,`

All properties defined in the framework's properties file, passed in as arguments or defined in the system properties can be accessed in the actions by encapsulating the value with angled brackets.

Example:
- In order to navigate to the 'google.base.url' property, you would use the Action `Navigate,,,<google.base.url>`
- In order to enter the 'local.app.username' property into a field with an ID of 'user_id', you would use the Action `Field,ID,user_id,<app.username>,`