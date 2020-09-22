package com.versatiletester.test.impl.google;

import com.versatiletester.test.TestBase;
import com.versatiletester.test.categories.BrowserStack;
import com.versatiletester.test.categories.Google;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({Google.class, BrowserStack.class})
public class GoogleSearch_Tests extends TestBase {
    private static final Logger log = Logger.getLogger(GoogleSearch_Tests.class);

    @Test
    public void googleTest1() {
        runWebAppActionsViaExcel("WEB_GuessWho_Batman");
    }
}