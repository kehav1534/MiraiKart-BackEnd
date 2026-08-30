package com.eshu.OnlineShopping.service;


import com.eshu.OnlineShopping.dto.UserInfoDto;
import com.eshu.OnlineShopping.dto.UserRegisterDto;
import com.eshu.OnlineShopping.enums.UserStatus;
import com.eshu.OnlineShopping.exceptions.DuplicateException;
import com.eshu.OnlineShopping.model.User;
import com.eshu.OnlineShopping.model.UserAuth;
import com.eshu.OnlineShopping.repository.UserAuthRepository;
import com.eshu.OnlineShopping.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    UserAuthRepository userAuthRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    /**
     * Single signup entry point: creates the UserAuth credential row and the
     * User profile row together and links them, so a token issued at login
     * always resolves back to a real, complete profile.
     */
    @Transactional
    public String registerUser(UserRegisterDto dto) {
        if (userAuthRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateException("Email", dto.getEmail());
        }
        if (dto.getContactNo() != null && userInfoRepository.existsByContactNo(dto.getContactNo())) {
            throw new DuplicateException("Contact number", dto.getContactNo());
        }

        UserAuth userAuth = new UserAuth();
        userAuth.setEmail(dto.getEmail());
        userAuth.setPassword(passwordEncoder.encode(dto.getPassword()));
        userAuthRepository.save(userAuth);

        User newUser = new User();
        newUser.setContactNo(dto.getContactNo());
        newUser.setAddress(dto.getAddress());
        newUser.setGender(dto.getGender());
        newUser.setDob(dto.getDob());
        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setUserAuth(userAuth);
        userInfoRepository.save(newUser);

        return "User registered successfully";
    }

    public List<User> getAllUsers(){
        return userInfoRepository.findAll();
    }

    /**
     * Resolves the buyer profile a SELLER account shops through. Sellers
     * never register a separate buyer account - the first time their token
     * hits the cart/checkout, a bare User row (no UserAuth; they never log
     * into it directly) is created and tagged with linkedSellerId so every
     * later call finds the same one. Cart/Order/OrderItem all key off a
     * plain User id, so nothing downstream needs to know this profile is
     * seller-backed.
     */
    @Transactional
    public User getOrCreateShoppingProfileForSeller(int sellerId) {
        return userInfoRepository.findByLinkedSellerId(sellerId)
                .orElseGet(() -> {
                    User shoppingProfile = new User();
                    shoppingProfile.setStatus(UserStatus.ACTIVE);
                    shoppingProfile.setLinkedSellerId(sellerId);
                    return userInfoRepository.save(shoppingProfile);
                });
    }

    public String updateUserDetails(int userId, UserInfoDto userInfoDto){
        User user = userInfoRepository.findById(userId).orElse(null);
        if(user!=null){
//            user.setEmail(userInfoDto.getEmail()!=null?userInfoDto.getEmail():user.getEmail());
            user.setContactNo(userInfoDto.getContactNo()!=null?userInfoDto.getContactNo():user.getContactNo());
            user.setAddress(userInfoDto.getAddress()!=null?userInfoDto.getAddress():user.getAddress());
            user.setGender(userInfoDto.getGender()!=null?userInfoDto.getGender():user.getGender());
            user.setDob((userInfoDto.getDob()!=null)?userInfoDto.getDob():user.getDob());
            userInfoRepository.save(user);
            return "User Details successfully updated.";
        }
        return "Error: User not found. User Details not updated.";
    }
}
