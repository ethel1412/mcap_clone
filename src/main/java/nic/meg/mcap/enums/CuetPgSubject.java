package nic.meg.mcap.enums;

public enum CuetPgSubject {

    // Science (101)
    PHYSICS("Physics", (short)101),
    CHEMISTRY("Chemistry", (short)101),
    MATHEMATICS("Mathematics", (short)101),
    STATISTICS("Statistics", (short)101),
    COMPUTER_SCIENCE("Computer Science and Applications", (short)101),
    LIFE_SCIENCES("Life Sciences", (short)101),
    ENVIRONMENTAL_SCIENCES("Environmental Sciences", (short)101),

    // Commerce (102)
    COMMERCE("Commerce", (short)102),
    BUSINESS_ECONOMICS("Business Economics", (short)102),
    ACCOUNTING_FINANCE("Accounting & Finance", (short)102),

    // Arts/Humanities (103)
    ECONOMICS("Economics", (short)103),
    PSYCHOLOGY("Psychology", (short)103),
    SOCIOLOGY("Sociology", (short)103),
    POLITICAL_SCIENCE("Political Science", (short)103),
    HISTORY("History", (short)103),
    GEOGRAPHY("Geography", (short)103),
    PHILOSOPHY("Philosophy", (short)103),
    ENGLISH("English", (short)103),
    HINDI("Hindi", (short)103),

    // Vocational/Technical (104)
    VOCATIONAL_STUDIES("Vocational Studies", (short)104),

    // Engineering & Technology (105)
    ENGINEERING("Engineering", (short)105),

    // Management (106)
    MANAGEMENT("Management", (short)106),
    MBA("MBA", (short)106),

    // Fine Arts (107)
    FINE_ARTS("Fine Arts / Visual Arts", (short)107),
    PERFORMING_ARTS("Performing Arts", (short)107);

    private final String displayName;
    private final short streamId;

    CuetPgSubject(String displayName, short streamId) {
        this.displayName = displayName;
        this.streamId = streamId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public short getStreamId() {
        return streamId;
    }
}
