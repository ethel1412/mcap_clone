package nic.meg.mcap.enums;

public enum CuetUgSubject {

    // Science (101)
    PHYSICS("Physics", (short)101),
    CHEMISTRY("Chemistry", (short)101),
    BIOLOGY("Biology", (short)101),
    MATHEMATICS("Mathematics", (short)101),
    COMPUTER_SCIENCE("Computer Science/Informatics Practices", (short)101),
    ENVIRONMENTAL_SCIENCE("Environmental Science", (short)101),

    // Commerce (102)
    ACCOUNTANCY("Accountancy/Book Keeping", (short)102),
    BUSINESS_STUDIES("Business Studies", (short)102),
    ECONOMICS("Economics/Business Economics", (short)102),
    ENTREPRENEURSHIP("Entrepreneurship", (short)102),

    // Arts/Humanities (103)
    HISTORY("History", (short)103),
    POLITICAL_SCIENCE("Political Science", (short)103),
    SOCIOLOGY("Sociology", (short)103),
    PSYCHOLOGY("Psychology", (short)103),
    GEOGRAPHY("Geography/Geology", (short)103),
    HOME_SCIENCE("Home Science", (short)103),

    // Vocational/Technical (104)
    AGRICULTURE("Agriculture", (short)104),
    VOCATIONAL_STUDIES("Vocational Studies", (short)104),

    // Engineering & Technology (105)
    ENGINEERING_GRAPHICS("Engineering Graphics", (short)105),

    // Management (106)
    BUSINESS_ADMINISTRATION("Business Administration", (short)106),

    // Fine Arts (107)
    FINE_ARTS("Fine Arts/Visual Arts/Commercial Art", (short)107),
    PERFORMING_ARTS("Performing Arts", (short)107);

    private final String displayName;
    private final short streamId;

    CuetUgSubject(String displayName, short streamId) {
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
