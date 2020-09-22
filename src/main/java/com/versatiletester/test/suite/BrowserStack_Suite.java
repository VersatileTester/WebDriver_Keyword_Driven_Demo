package com.versatiletester.test.suite;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import com.versatiletester.test.categories.BrowserStack;

@RunWith(WildcardPatternSuite.class)
@Categories.IncludeCategory(BrowserStack.class)
@SuiteClasses("../impl/**/*Tests.class")
public class BrowserStack_Suite {
}
