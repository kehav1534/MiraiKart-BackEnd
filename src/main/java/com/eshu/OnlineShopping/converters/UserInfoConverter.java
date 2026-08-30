package com.eshu.OnlineShopping.converters;

import com.eshu.OnlineShopping.dto.UserInfoDto;
import com.eshu.OnlineShopping.enums.UserStatus;
import com.eshu.OnlineShopping.model.User;

public class UserInfoConverter {
    public static User convertUserInfoDtoIntoUser(UserInfoDto userInfoDto){
        User newUser = new User();
        newUser.setDob(userInfoDto.getDob());
        newUser.setAddress(userInfoDto.getAddress());
        newUser.setGender(userInfoDto.getGender());
        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setContactNo(userInfoDto.getContactNo());
        return newUser;
    }
}
