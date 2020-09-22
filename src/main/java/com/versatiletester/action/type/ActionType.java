package com.versatiletester.action.type;

public enum ActionType {
    CLICK("click"),
    CLICK_IF_EXISTS("click optional"),
    DOUBLE_CLICK("double click"),
    FIELD("field"),
    FIELD_IF_EXISTS("field optional"),
    FIELD_NO_CLEAR("field no clear"),
    DYNAMIC_FIELD("dynamic field"),
    NAVIGATE("navigate"),
    SCREENSHOT("screenshot"),
    ASSERT_ELEMENT_TEXT("assert element text"),
    ASSERT_ELEMENT("assert element"),
    SWITCH_TO("switch to"),
    SELECT("select"),
    WAIT("wait"),
    UPLOAD_FILE("upload file"),
    SET_TODAY_PLUS("set today plus"),
    CLOSE_TAB("close tab"),
    SET_ELEMENT_TEXT("set element text"),
    SET_CURRENT_URL("set current url"),
    BREAK_POINT("break point");

    private final String dataStringValue;

    @Override
    public String toString() { return this.dataStringValue; }
    ActionType(String dataStringValue) { this.dataStringValue = dataStringValue; }

    public static ActionType getMatch(String text) {
        for (ActionType platform : ActionType.values()) {
            if (platform.toString().equalsIgnoreCase(text)) {
                return platform;
            }
        }
        throw new RuntimeException("Action Type '" + text + "' unsupported.");
    }
}