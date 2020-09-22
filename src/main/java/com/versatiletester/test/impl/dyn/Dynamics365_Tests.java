package com.versatiletester.test.impl.dyn;

import com.versatiletester.test.TestBase;
import com.versatiletester.test.categories.Dynamics;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Dynamics.class)
public class Dynamics365_Tests extends TestBase {
    private static final Logger log = Logger.getLogger(Dynamics365_Tests.class);

    @Test
    public void runTestCreateSingleEHC() {
        runDynamicsActionsViaExcel("WEB_GuessWho_Batman");
    }
}