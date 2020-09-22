package com.versatiletester.action.manager;

import com.versatiletester.action.DynamicsAction;
import com.versatiletester.action.TestAction;
import com.versatiletester.action.type.Action;
import com.versatiletester.config.SpringContext;
import com.versatiletester.util.file.FileUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class ActionManager {
    protected static final String DATA_FILE_PATH = FileUtils.convertPathToUniversal("src/main/resources/data");
    protected String webAppFilePath;
    protected String dynamicsFilePath;

    protected HashMap<String, ArrayList<Action>> webAppActionWorkbook;
    protected HashMap<String, ArrayList<Action>> dynamicsActionWorkbook;

    protected final SpringContext springContext;

    public ActionManager(SpringContext springContext) {
        this.springContext = springContext;
    }

    @PostConstruct
    private void postConstruct(){
        webAppActionWorkbook = loadActions(TestAction.class, webAppFilePath);
        dynamicsActionWorkbook = loadActions(DynamicsAction.class, dynamicsFilePath);
    }

    public ArrayList<Action> getWebAppActions(String testFlow) {
        if(webAppActionWorkbook.get(testFlow) != null){
            return webAppActionWorkbook.get(testFlow);
        } else {
            throw new UnsupportedOperationException(constructErrorMessage("WebApp",testFlow));
        }
    }
    public ArrayList<Action> getDynamicsActions(String testFlow) {
        if(dynamicsActionWorkbook.get(testFlow) != null){
            return dynamicsActionWorkbook.get(testFlow);
        } else {
            throw new UnsupportedOperationException(constructErrorMessage("Dynamics",testFlow));
        }
    }

    protected abstract String constructErrorMessage(String testFlowType, String testFlow);
    protected abstract HashMap<String, ArrayList<Action>> loadActions(Class<? extends Action> actionClass, String filePath);
}
