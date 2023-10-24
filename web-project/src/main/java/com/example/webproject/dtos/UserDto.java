package com.example.webproject.dtos;
import com.example.webproject.dtos.UpdateUserDto;
import jakarta.validation.constraints.NotNull;

public class UserDto extends UpdateUserDto {
    @NotNull(message = "Username can't be empty!")
    private String username;

    public UserDto() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
