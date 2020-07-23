package com.example.onlinebankingsystem.services;

import com.example.onlinebankingsystem.models.PrimaryAccount;
import com.example.onlinebankingsystem.models.SavingsAccount;

import java.security.Principal;

public interface AccountService {

    PrimaryAccount createPrimaryAccount();
    SavingsAccount createSavingsAccount();
    void deposit(String accountType, double amount, Principal principal);
    void withdrawFromPrimary (String accountType, double amount, Principal principal);
    void withdrawFromSavings (String accountType, double amount, Principal principal);


}