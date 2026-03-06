package com.TestFlashCard.FlashCard.response;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String renewalToken;
    private int userId;
    private String accountName;
    private String role;

    public void setAccessToken(String token){
        this.accessToken=token;
    }
    public void setRenewalToken(String token){
        this.renewalToken=token;
    }
    public void setId(int id){
        this.userId=id;
    }
    public void setAccountName(String name){
        this.accountName=name;
    }
    public void setRole(String role){
        this.role=role;
    }

    public String getAccessToken(){
        return this.accessToken;
    }
    public String getRenewalToken(){
        return this.renewalToken;
    }
    public int getUserId(){
        return this.userId;
    }
    public String getAccountName(){
        return this.accountName;
    }
    public String getRole(){
        return this.role;
    }
}


