package com.versatiletester.config;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Javadoc
 */
public class TestContext {
    HashMap<String,String> dynamicVariables = new HashMap<>();

    public void setVariable(String variableName, String variableValue){
        this.dynamicVariables.put(variableName,variableValue);
    }

    public String getVariable(String variableName){
        return this.dynamicVariables.get(variableName);
    }

    @Override
    public String toString() {
        StringWriter stringWriter = new StringWriter();

        for(Map.Entry<String,String> entry : dynamicVariables.entrySet()){
            stringWriter.append(entry.getKey());
            stringWriter.append(":");
            stringWriter.append(entry.getValue());
            stringWriter.append("; ");
        }

        return stringWriter.toString();
    }
}