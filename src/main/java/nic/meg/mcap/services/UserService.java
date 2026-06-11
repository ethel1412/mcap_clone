package nic.meg.mcap.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import nic.meg.mcap.dto.request.ChangePasswordRequestDTO;
import nic.meg.mcap.dto.request.ForcePasswordChangeRequestDTO;
import nic.meg.mcap.dto.request.UserDTO;
import nic.meg.mcap.dto.request.UserUpdateDTO;
import nic.meg.mcap.dto.response.UserActivitiesResponse;
import nic.meg.mcap.dto.response.UserResponse;
import nic.meg.mcap.dto.response.UserResponseByCode;
import nic.meg.mcap.entities.User;

public interface UserService {

	User createUser(UserDTO user);

	Page<UserResponse> getListUsers(Pageable pageable);

	Page<UserActivitiesResponse> getListUsersActivities(Pageable pageable);

	UserResponseByCode getUserByUserCode(UUID userCode);

	UserResponseByCode getUserByUsername();

	UserResponseByCode getUserByUsername(String username);

	User updateUser(UserUpdateDTO userUpdate);

	boolean changePassword(ChangePasswordRequestDTO passwordChange);

	User forceChangePassword(ForcePasswordChangeRequestDTO request, User loggedInUser);

	User getUser();

	User lockUnlockUser(UUID userCode, boolean unlock);

	UserDetails loadUserByUsername(String username);

}