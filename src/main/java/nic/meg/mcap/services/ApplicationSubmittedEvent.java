package nic.meg.mcap.services;

import org.springframework.context.ApplicationEvent;

public class ApplicationSubmittedEvent extends ApplicationEvent {

    private final Long applicationId;

    public ApplicationSubmittedEvent(Object source, Long applicationId) {
        super(source);
        this.applicationId = applicationId;
    }

    public Long getApplicationId() {
        return applicationId;
    }
}