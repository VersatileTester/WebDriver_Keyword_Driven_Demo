package com.versatiletester.action.type;

public enum IdentifierType {
    NAME("name"),
    ID("id"),
    XPATH("xpath"),
    TEXT("text"),
    TEXT_CONTAINS("text contains"),
    TAB("tab"),
    FRAME("frame"),
    DEFAULT("default");

    private String dataStringValue;

    @Override
    public String toString() { return this.dataStringValue; }
    IdentifierType(String dataStringValue) { this.dataStringValue = dataStringValue; }

    public static IdentifierType getMatch(String text) {
        for (IdentifierType platform : IdentifierType.values()) {
            if (platform.toString().equalsIgnoreCase(text)) {
                return platform;
            }
        }
        throw new RuntimeException("Action Type '" + text + "' unsupported.");
    }
}
