package com.versatiletester.action.manager;

import com.google.common.base.Preconditions;
import com.versatiletester.action.DynamicsAction;
import com.versatiletester.action.TestAction;
import com.versatiletester.config.SpringContext;
import com.versatiletester.util.file.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A CSVManager implementation that does not inherit from the abstract ActionManager.
 *
 * Added as an example of using a single implementation (instead of using Excel AND CSV, which is not recommended)
 * that doesn't use raw class types. I personally prefer the 'raw class types + CSV-only' implementation, but
 * I won't judge...
 */
@Deprecated
@Component
@SuppressWarnings({"unused", "DuplicatedCode"})
public class LegacyCSVActionManager {
    private static final Logger log = Logger.getLogger(CSVActionManager.class);

    private static final String TEST_FLOW_FILE_PATH = FileUtils.convertPathToUniversal("src/main/resources/data/");
    private static final String WEBAPP_FLOW_DIR = "WebAppTestFlows";
    private static final String DYNAMICS_FLOW_DIR = "DynamicsTestFlows";

    protected HashMap<String, ArrayList<TestAction>> webAppActionWorkbook;
    protected HashMap<String, ArrayList<DynamicsAction>> dynamicsActionWorkbook;

    private final SpringContext springContext;

    public LegacyCSVActionManager(SpringContext springContext) {
        this.springContext = springContext;
        webAppActionWorkbook = loadTestActionsFromDir(TEST_FLOW_FILE_PATH + WEBAPP_FLOW_DIR);
        dynamicsActionWorkbook = loadDynamicsActionsFromDir(TEST_FLOW_FILE_PATH + DYNAMICS_FLOW_DIR);
    }

    public ArrayList<TestAction> getWebAppActionsForFile(String csv) {
        if(webAppActionWorkbook.get(csv) != null){
            return webAppActionWorkbook.get(csv);
        } else {
            throw new UnsupportedOperationException("WebApp CSV '" + csv + ".csv' could not be found. " +
                    "Please note, this file reference is case sensitive.");
        }
    }
    public ArrayList<DynamicsAction> getDynamicsActionsForFile(String csv) {
        if(dynamicsActionWorkbook.get(csv) != null){
            return dynamicsActionWorkbook.get(csv);
        } else {
            throw new UnsupportedOperationException("Dynamics CSV '" + csv + ".csv' could not be found. " +
                    "Please note, this file reference is case sensitive.");
        }
    }

    private HashMap<String, ArrayList<TestAction>> loadTestActionsFromDir(String dirPath) {
        HashMap<String, ArrayList<TestAction>> testActionMap = new HashMap<>();

        File dir = new File(dirPath);
        File[] files = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(".csv"));

        Preconditions.checkArgument(files != null);

        for (File file : files) {
            ArrayList<TestAction> testActions = new ArrayList<>();
            String line;

            if (file.getName().equalsIgnoreCase("Template")) {
                continue;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while ((line = br.readLine()) != null) {
                    ArrayList<String> actionRow = processRowString(line);

                    if (actionRow.get(0).equalsIgnoreCase("Action") ||
                        actionRow.get(0).isEmpty() ||
                        actionRow.get(0).startsWith("#")) {
                        continue;
                    }

                    TestAction action = new TestAction(actionRow.get(0).trim(), springContext);

                    if (!actionRow.get(1).isEmpty()) {
                        action.setIdentifierType(actionRow.get(1).trim());
                    }

                    if (!actionRow.get(2).isEmpty()) {
                        action.setIdentifierValue(actionRow.get(2).trim().replace("’", "'"));
                    }

                    if (!actionRow.get(3).isEmpty()) {
                        action.setValue(actionRow.get(3).trim().replace("’", "'"));
                    }
                    testActions.add(action);
                }

            } catch (Exception e) {
                StringWriter stack = new StringWriter();
                e.printStackTrace(new PrintWriter(stack));
                throw new RuntimeException("Error loading file: " + file.getName() + "\n" + stack.toString());
            }
            testActionMap.put(file.getName().replace(".csv", ""), testActions);
        }
        return testActionMap;
    }

    private HashMap<String, ArrayList<DynamicsAction>> loadDynamicsActionsFromDir(String dirPath) {
        HashMap<String, ArrayList<DynamicsAction>> dynamicsActionMap = new HashMap<>();

        File dir = new File(dirPath);
        File[] files = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(".csv"));

        Preconditions.checkArgument(files != null);

        for (File file : files) {
            ArrayList<DynamicsAction> dynamicsActions = new ArrayList<>();
            String line;

            if (file.getName().equalsIgnoreCase("Template")) {
                continue;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while ((line = br.readLine()) != null) {
                    ArrayList<String> actionRow = processRowString(line);

                    if (actionRow.get(0).equalsIgnoreCase("Action") ||
                            actionRow.get(0).isEmpty() ||
                            actionRow.get(0).startsWith("#")) {
                        continue;
                    }

                    DynamicsAction dynamicsAction = new DynamicsAction(actionRow.get(0).trim(), springContext);

                    if (!actionRow.get(1).isEmpty()) {
                        dynamicsAction.setIdentifierType(actionRow.get(1).trim());
                    }
                    if (!actionRow.get(2).isEmpty()) {
                        dynamicsAction.setIdentifierValue(actionRow.get(2).trim().replace("’", "'"));
                    }
                    if (!actionRow.get(3).isEmpty()) {
                        dynamicsAction.setValue(actionRow.get(3).trim().replace("’", "'"));
                    }
                    dynamicsActions.add(dynamicsAction);
                }

            } catch (Exception e) {
                throw new RuntimeException("Error loading file \"" + file.getName() + "\" due to \"" + e.getMessage() + "\"");
            }
            dynamicsActionMap.put(file.getName().replace(".csv", ""), dynamicsActions);
        }
        return dynamicsActionMap;
    }

    private ArrayList<String> processRowString(String rawString){
        ArrayList<String> actionRow = new ArrayList<>();
        String csvCellRegexPattern = "(?:,|\\n|^)(\"(?:(?:\"\")*[^\"]*)*\"|[^\",\\n]*|(?:\\n|$))";
        Pattern pattern = Pattern.compile(csvCellRegexPattern);
        Matcher matcher = pattern.matcher(rawString);

        while(matcher.find()) {
            actionRow.add(matcher.group()
                    .replaceAll("^([,\"])*","")
                    .replaceAll("([,\"])*$","")
                    .replace("’", "'"));
        }
        return actionRow;
    }
}