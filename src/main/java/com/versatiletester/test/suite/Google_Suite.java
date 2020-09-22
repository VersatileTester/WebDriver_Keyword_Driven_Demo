package com.versatiletester.test.suite;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;
import com.versatiletester.test.categories.TestCompanion;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;

@RunWith(WildcardPatternSuite.class)
@Categories.IncludeCategory(TestCompanion.class)
@SuiteClasses("../impl/**/*Tests.class")
public class Google_Suite {
}
