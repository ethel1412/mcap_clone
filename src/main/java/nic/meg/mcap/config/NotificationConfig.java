package nic.meg.mcap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mcap.notification")
public class NotificationConfig {
    
    private Urgent urgent = new Urgent();
    
    public Urgent getUrgent() {
        return urgent;
    }
    
    public void setUrgent(Urgent urgent) {
        this.urgent = urgent;
    }
    
    public static class Urgent {
        private int threshold = 7; // Default: 7 days
        
        public int getThreshold() {
            return threshold;
        }
        
        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }
    }
}
