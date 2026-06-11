package nic.meg.mcap.enums;

public enum LoginType {
    STANDARD,      // User logs in via form, password is RSA-encrypted from client
    PROGRAMMATIC   // System logs user in (e.g., after registration), password is plain text
}