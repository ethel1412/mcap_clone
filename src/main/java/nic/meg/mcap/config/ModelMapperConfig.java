package nic.meg.mcap.config;

import nic.meg.mcap.dto.request.UserDTO;
import nic.meg.mcap.entities.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        
        // 1. General Config: Strict matching and prevent null overwrites (Important for Applicant Portal)
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true); 

        // 2. Specific Rules: Preserved from your main application class
        modelMapper.typeMap(UserDTO.class, User.class)
                .addMappings(m -> m.skip(User::setUserId)); // <-- Critical rule kept safe!

        return modelMapper;
    }
}