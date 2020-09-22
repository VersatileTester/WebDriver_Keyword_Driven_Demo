package com.versatiletester.action.manager;

import com.google.common.base.Preconditions;
import com.versatiletester.action.type.Action;
import com.versatiletester.config.SpringContext;
import com.versatiletester.util.file.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CSVActionManager extends ActionManager {
    private static final Logger log = Logger.getLogger(CSVActionManager.class);

    private static final String CSV_FILE_PATH = FileUtils.convertPathToUniversal(DATA_FILE_PATH + "/csv/");
    private static final String WEBAPP_FLOW_DIR = "WebAppTestFlows";
    private static final String DYNAMICS_FLOW_DIR = "DynamicsTestFlows";

    public CSVActionManager(SpringContext springContext) {
        super(springContext);
        this.webAppFilePath = FileUtils.convertPathToUniversal(CSV_FILE_PATH + WEBAPP_FLOW_DIR);
        this.dynamicsFilePath = FileUtils.convertPathToUniversal(CSV_FILE_PATH + DYNAMICS_FLOW_DIR);
    }

    @Override
    protected String constructErrorMessage(String testFlowType, String testFlow) {
        return testFlowType + " CSV '" + testFlow + ".csv' could not be found. " +
                "Please note, this file reference is case sensitive.";
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected HashMap<String, ArrayList<Action>> loadActions(Class<? extends Action> actionClass, String filePath){
        HashMap<String, ArrayList<Action>> actionMap = new HashMap<>();

        File dir = new File(filePath);
        File[] files = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(".csv"));

        Preconditions.checkArgument(files != null);

        for (File file : files) {
            ArrayList<Action> actions = new ArrayList<>();
            String line;

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while ((line = br.readLine()) != null) {
                    ArrayList<String> actionRow = processRowString(line);

                    if (actionRow.get(0).equalsIgnoreCase("Action") ||
                            actionRow.get(0).isEmpty() ||
                            actionRow.get(0).startsWith("#")) {
                        continue;
                    }

                    Class[] cArg = new Class[2];
                    cArg[0] = String.class;
                    cArg[1] = SpringContext.class;

                     Action action = actionClass.getDeclaredConstructor(cArg).newInstance(actionRow.get(0).trim(), springContext);

                    if (!actionRow.get(1).isEmpty()) {
                        action.setIdentifierType(actionRow.get(1).trim());
                    }
                    if (!actionRow.get(2).isEmpty()) {
                        action.setIdentifierValue(actionRow.get(2).trim().replace("’", "'"));
                    }
                    if (!actionRow.get(3).isEmpty()) {
                        action.setValue(actionRow.get(3).trim().replace("’", "'"));
                    }
                    actions.add(action);
                }

            } catch (Exception e) {
                if(e.getMessage() == null){
                    throw new RuntimeException("Error loading file \"" + file.getName() + "\" due to \"" + e.getCause() + "\"");
                } else {
                    throw new RuntimeException("Error loading file \"" + file.getName() + "\" due to \"" + e.getMessage() + "\"");
                }

            }
            actionMap.put(file.getName().replace(".csv", ""), actions);
        }
        return actionMap;
    }

    /**
     * This implementation was using Regular Expressions, however considering in order to maintain it you needed
     * to understand this -> "(?:,|\\n|^)(\"(?:(?:\"\")*[^\"]*)*\"|[^\",\\n]*|(?:\\n|$))"... I chose otherwise.
     *
     * @param rawString A CSV row
     * @return A list of CSV cells in the given String
     */
    private ArrayList<String> processRowString(String rawString){
        ArrayList<String> actionRow = new ArrayList<>();
        boolean insideQuote = false;
        int startOfValue = 0;

        for(int i=0; i<=rawString.length()-1; i++) {
            if(rawString.charAt(i)==',' && !insideQuote) {
                actionRow.add(processString(rawString.substring(startOfValue,i)));
                startOfValue = i+1;
            }
            else if(rawString.charAt(i)=='"')
                insideQuote=!insideQuote;
        }

        actionRow.add(rawString.substring(startOfValue));
        return actionRow;
    }
    private String processString(String stringToProcess){
        return stringToProcess
                .trim() // Trim spaces outside of the quotes, as spaces inside are most likely intentional
                .replaceAll("(^\"|\"$)","")
                .replace("’", "'");
    }
}