package nic.meg.mcap.enums;

public enum SmsTemplate {

	MERIT_LIST("1407177643188876193",
			"Dear %s, the merit list for MCAP is now available. Please log in to the portal to check your status. : MEGCAP"),

	FINAL_VERIFICATION("1407177643183130028",
			"Dear %s, seat allotment is completed for %s. Please verify your admission by %s, as the final list of applicants will be submitted. : MEGCAP"),

	PROGRAM_UPDATE("1407177643176752411",
			"Dear %s, update programme details and lock the seat matrix by %s for %s in MCAP to participate in seat allotment. : MEGCAP"),

	APPLICATION_APPROVED("1407177643168932712",
			"Dear %s, your application has been approved for %s. Your username is %s and your temporary password is %s. Please log in and set a new password. : MEGCAP"),

	APPLICATION_REJECTED("1407177643150858596",
			"Dear %s, your application %s has been rejected. Please log in to the portal for details. : MEGCAP"),

	SEAT_CONFIRMED("1407177643143383046",
			"Dear %s, your seat for Programme/Course %s at %s is confirmed. For further admission processes, please visit the respective institute. : MEGCAP"),

	SEAT_ALLOTTED("1407177643131709535",
			"Dear %s, you have been allotted a seat in %s for Programme/Course %s. Please confirm before %s by logging in to the MCAP portal. : MEGCAP"),

	APPLICATION_SUBMITTED("1407177643104615059",
			"Dear %s, your application %s has been successfully submitted for MCAP. : MEGCAP"),

	OTP("1407177979476514270",
			"%s is your OTP for Meghalaya Common Admission Portal. OTP valid for %s minutes. Do not share with anyone. : MCAP");

	private final String templateId;
	private final String format;

	SmsTemplate(String templateId, String format) {
		this.templateId = templateId;
		this.format = format;
	}

	public String getTemplateId() {
		return templateId;
	}

	public String format(Object... values) {
		return String.format(format, values);
	}
}