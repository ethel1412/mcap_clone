package nic.meg.mcap.enums;

public enum SubjectType {
	CORE,
    ELECTIVE,
    MAJOR,
    MINOR,
    MDC,
    SEC,
    AEC,
    VAC,
    GENERAL;
	
	public static SubjectType from(String value) {
		if (value == null)
			return null;
		String norm = value.trim().toUpperCase().replace('-', '_').replace(' ', '_');
		return SubjectType.valueOf(norm);
	}
}