package com.example.onlinebankingsystem.services.ServicesImplementation;

import com.example.onlinebankingsystem.models.*;
import com.example.onlinebankingsystem.repository.*;
import com.example.onlinebankingsystem.services.TransactionService;
import com.example.onlinebankingsystem.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    private UserService userService;

    @Autowired
    private PrimaryTransactionRepository primaryTransactionRepository;

    @Autowired
    private SavingsTransactionRepository savingsTransactionRepository;

    @Autowired
    private PrimaryAccountRepository primaryAccountRepository;

    @Autowired
    private SavingsAccountRepository savingsAccountRepository;
    @Autowired
    RecipientRepository recipientRepository;


    @Override
    public List<PrimaryTransaction> findPrimaryTransactionList(String username) {
        User user = userService.findByUsername(username);
        List<PrimaryTransaction> primaryTransactionList = user.getPrimaryAccount().getPrimaryTransactionList();

        return primaryTransactionList;
    }

    @Override
    public List<SavingsTransaction> findSavingsTransactionList(String username) {
        User user = userService.findByUsername(username);
        List<SavingsTransaction> savingsTransactionList = user.getSavingsAccount().getSavingsTransactionList();

        return savingsTransactionList;
    }

    @Override
    public void savePrimaryDepositTransaction(PrimaryTransaction primaryTransaction) {
        primaryTransactionRepository.save(primaryTransaction);
    }

    @Override
    public void saveSavingsDepositTransaction(SavingsTransaction savingsTransaction) {
        savingsTransactionRepository.save(savingsTransaction);
    }

    @Override
    public void savePrimaryWithdrawTransaction(PrimaryTransaction primaryTransaction) {
        primaryTransactionRepository.save(primaryTransaction);
    }

    @Override
    public void saveSavingsWithdrawTransaction(SavingsTransaction savingsTransaction) {
        savingsTransactionRepository.save(savingsTransaction);
    }

    @Override
    public void fromPrimarytoSomeoneElseTransfer(Recipient recipient, String accountType, String amount,
                                                 PrimaryAccount primaryAccount, SavingsAccount savingsAccount) {
        primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(new BigDecimal(amount)));
        primaryAccountRepository.save(primaryAccount);

        Date date = new Date();

        PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Transfer to recipient "+
                recipient.getName(), "Transfer", "Finished", Double.parseDouble(amount),
                primaryAccount.getAccountBalance(), primaryAccount);
        primaryTransactionRepository.save(primaryTransaction);
    }

    @Override
    public void fromSavingstoSomeoneElseTransfer(Recipient recipient, String accountType, String amount,
                                                 PrimaryAccount primaryAccount, SavingsAccount savingsAccount) {
        savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(new BigDecimal(amount)));
        savingsAccountRepository.save(savingsAccount);

        Date date = new Date();

        SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Transfer to recipient " +
                recipient.getName(), "Transfer", "Finished", Double.parseDouble(amount),
                savingsAccount.getAccountBalance(), savingsAccount);
        savingsTransactionRepository.save(savingsTransaction);
    }


    @Override
    public List<Recipient> findRecipientList(Principal principal) {
        String username = principal.getName();
        List<Recipient> recipientList = recipientRepository.findAll().stream()            //convert list to stream
                .filter(recipient -> username.equals(recipient.getUser().getUsername()))    //filters the line, equals to username
                .collect(Collectors.toList());

        return recipientList;
    }

    @Override
    public Recipient saveRecipient(Recipient recipient) {
        return recipientRepository.save(recipient);
    }

    @Override
    public Recipient findRecipientByName(String recipientName) {
        return recipientRepository.findByName(recipientName);
    }

    @Override
    public void deleteRecipientByName(String recipientName) {
        recipientRepository.deleteByName(recipientName);
    }

    @Override
    public void toSomeoneElseTransfer(Recipient recipient, String accountType, String amount,
                                      PrimaryAccount primaryAccount, SavingsAccount savingsAccount) {
        if (accountType.equalsIgnoreCase("Primary")) {
            primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(new BigDecimal(amount)));
            primaryAccountRepository.save(primaryAccount);

            Date date = new Date();

            PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Transfer to recipient "+
                    recipient.getName(), "Transfer", "Finished", Double.parseDouble(amount),
                    primaryAccount.getAccountBalance(), primaryAccount);
            primaryTransactionRepository.save(primaryTransaction);
        } else if (accountType.equalsIgnoreCase("Savings")) {
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(new BigDecimal(amount)));
            savingsAccountRepository.save(savingsAccount);

            Date date = new Date();

            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Transfer to recipient " +
                    recipient.getName(), "Transfer", "Finished", Double.parseDouble(amount),
                    savingsAccount.getAccountBalance(), savingsAccount);
            savingsTransactionRepository.save(savingsTransaction);
        }
    }

}