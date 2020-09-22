package com.versatiletester.action.manager;

import com.google.common.base.Preconditions;
import com.versatiletester.action.type.Action;
import com.versatiletester.config.SpringContext;
import com.versatiletester.util.file.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

@Component
public class ExcelActionManager extends ActionManager {
    private static final Logger log = Logger.getLogger(ExcelActionManager.class);

    private static final String EXCEL_FILE_PATH = FileUtils.convertPathToUniversal(DATA_FILE_PATH + "/excel/");
    private static final String WEBAPP_FLOW_FILE = "WebAppTestFlows.xlsm";
    private static final String DYNAMICS_FLOW_FILE = "DynamicsTestFlows.xlsm";

    DataFormatter dataFormatter = new DataFormatter();

    public ExcelActionManager(SpringContext springContext){
        super(springContext);
        this.webAppFilePath = FileUtils.convertPathToUniversal(EXCEL_FILE_PATH + WEBAPP_FLOW_FILE);
        this.dynamicsFilePath = FileUtils.convertPathToUniversal(EXCEL_FILE_PATH + DYNAMICS_FLOW_FILE);
    }

    @Override
    protected String constructErrorMessage(String testFlowType, String testFlow) {
        return testFlowType + " Sheet '" + testFlow + "' could not be found.";
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected HashMap<String, ArrayList<Action>> loadActions(Class<? extends Action> actionClass, String filePath) {
        HashMap<String, ArrayList<Action>> actionMap = new HashMap<>();
        Workbook workbook = null;

        try {
            workbook = WorkbookFactory.create(new File(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Preconditions.checkArgument(workbook != null,"Could not load file: " + filePath);
        Iterator<Sheet> sheets = workbook.sheetIterator();

        while(sheets != null && sheets.hasNext()){
            Sheet sheet = sheets.next();
            ArrayList<Action> actions = new ArrayList<>();

            for (Row row: sheet) {
                if( dataFormatter.formatCellValue(row.getCell(0)).equalsIgnoreCase("Action")
                        || dataFormatter.formatCellValue(row.getCell(0)).isEmpty() ){
                    continue;
                }

                Class[] cArg = new Class[2];
                cArg[0] = String.class;
                cArg[1] = SpringContext.class;

                Action action;
                try {
                    action = actionClass.getDeclaredConstructor(cArg).newInstance(dataFormatter.formatCellValue(row.getCell(0)).trim(), springContext);
                } catch (Exception e) {
                    throw new UnsupportedOperationException("Error creating Test Action object, please check the code.");
                }

                if(!dataFormatter.formatCellValue(row.getCell(1)).isEmpty()){
                    action.setIdentifierType(dataFormatter.formatCellValue(row.getCell(1)).trim());
                }

                if(!dataFormatter.formatCellValue(row.getCell(2)).isEmpty()){
                    action.setIdentifierValue(dataFormatter.formatCellValue(row.getCell(2)).trim().replace("’","'"));
                }

                if(!dataFormatter.formatCellValue(row.getCell(3)).isEmpty()){
                    action.setValue(dataFormatter.formatCellValue(row.getCell(3)).trim().replace("’","'"));
                }

                actions.add(action);
            }
            actionMap.put(sheet.getSheetName(),actions);
        }
        return actionMap;
    }
}
