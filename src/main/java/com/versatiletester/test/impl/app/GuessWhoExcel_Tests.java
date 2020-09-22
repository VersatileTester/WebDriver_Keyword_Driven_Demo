package com.versatiletester.test.impl.app;

import com.versatiletester.test.TestBase;
import com.versatiletester.test.categories.BrowserStack;
import com.versatiletester.test.categories.TestCompanion;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({TestCompanion.class, BrowserStack.class})
public class GuessWhoExcel_Tests extends TestBase {
    private static final Logger log = Logger.getLogger(GuessWhoExcel_Tests.class);

    @Test
    public void guessWhoBatmanFlow() {
        runWebAppActionsViaExcel("GuessWho_Batman");
    }

    @Test
    public void guessWhoBlackPantherFlow() {
        runWebAppActionsViaExcel("GuessWho_BlackPanther");
    }

    @Test
    public void guessWhoSpidermanFlow() {
        runWebAppActionsViaExcel("GuessWho_Spiderman");
    }

    @Test
    public void guessWhoSupermanFlow() {
        runWebAppActionsViaExcel("GuessWho_Superman");
    }

    @Test
    public void guessWhoDeadpoolFlow() {
        runWebAppActionsViaExcel("GuessWho_Deadpool");
    }
}