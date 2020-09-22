package com.versatiletester.test.impl.app;

import com.versatiletester.test.categories.BrowserStack;
import com.versatiletester.test.categories.TestCompanion;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import com.versatiletester.test.TestBase;

@Category({TestCompanion.class, BrowserStack.class})
public class GuessWhoCSV_Tests extends TestBase {
    private static final Logger log = Logger.getLogger(GuessWhoCSV_Tests.class);

    @Test
    public void guessWhoBatmanFlow() {
        runWebAppActionsViaCSV("WEB_GuessWho_Batman");
    }

    @Test
    public void guessWhoBlackPantherFlow() {
        runWebAppActionsViaCSV("WEB_GuessWho_BlackPanther");
    }

    @Test
    public void guessWhoSpidermanFlow() {
        runWebAppActionsViaCSV("WEB_GuessWho_Spiderman");
    }

    @Test
    public void guessWhoSupermanFlow() {
        runWebAppActionsViaCSV("WEB_GuessWho_Superman");
    }

    @Test
    public void guessWhoDeadpoolFlow() {
        runWebAppActionsViaCSV("WEB_GuessWho_Deadpool");
    }
}