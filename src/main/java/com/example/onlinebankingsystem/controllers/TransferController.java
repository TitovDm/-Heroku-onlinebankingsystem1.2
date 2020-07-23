package com.example.onlinebankingsystem.controllers;


import com.example.onlinebankingsystem.models.*;
import com.example.onlinebankingsystem.repository.PrimaryAccountRepository;
import com.example.onlinebankingsystem.repository.PrimaryTransactionRepository;
import com.example.onlinebankingsystem.repository.SavingsAccountRepository;
import com.example.onlinebankingsystem.repository.SavingsTransactionRepository;
import com.example.onlinebankingsystem.services.TransactionService;
import com.example.onlinebankingsystem.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/transfer")
public class TransferController {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private UserService userService;

    @Autowired
    private PrimaryAccountRepository primaryAccountRepository;
    @Autowired
    private SavingsAccountRepository savingsAccountRepository;

    @Autowired
    private PrimaryTransactionRepository primaryTransactionRepository;
    @Autowired
    private SavingsTransactionRepository savingsTransactionRepository;



    @RequestMapping(value = "/betweenAccounts", method = RequestMethod.GET)
    public String betweenAccount (Model model) {
        model.addAttribute("transferFrom", "");
        model.addAttribute("transferTo", "");
        model.addAttribute("amount", "");

        return "betweenAccounts";
    }

    @RequestMapping(value = "/betweenAccounts", method = RequestMethod.POST)
    public String betweenAccountPost (
            @ModelAttribute("transferFrom") String transferFrom,
            @ModelAttribute("transferTo") String transferTo,
            @ModelAttribute("amount") String amount,
            Model model,
            Principal principal) throws Exception {
        User user = userService.findByUsername(principal.getName());
        PrimaryAccount primaryAccount = user.getPrimaryAccount();
        SavingsAccount savingsAccount = user.getSavingsAccount();

        if (transferFrom.equalsIgnoreCase("Primary") && transferTo.equalsIgnoreCase("Savings")) {
            if (primaryAccount.getAccountBalance().compareTo(new BigDecimal(amount)) < 0) {
                model.addAttribute("wrongTransaction", true);
                return "betweenAccounts";
            }
            primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(new BigDecimal(amount)));

            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().add(new BigDecimal(amount)));
            primaryAccountRepository.save(primaryAccount);
            savingsAccountRepository.save(savingsAccount);

            Date date = new Date();

            PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Between account transfer from " + transferFrom + " to " + transferTo, "Account", "Finished", Double.parseDouble(amount), primaryAccount.getAccountBalance(), primaryAccount);
            primaryTransactionRepository.save(primaryTransaction);
            return "redirect:/userFront";
        } else if (transferFrom.equalsIgnoreCase("Savings") && transferTo.equalsIgnoreCase("Primary")) {
            if (savingsAccount.getAccountBalance().compareTo(new BigDecimal(amount)) < 0) {
                model.addAttribute("wrongTransaction", true);
                return "betweenAccounts";
            }
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(new BigDecimal(amount)));
            primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().add(new BigDecimal(amount)));
            primaryAccountRepository.save(primaryAccount);
            savingsAccountRepository.save(savingsAccount);

            Date date = new Date();

            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Between account transfer from " + transferFrom + " to " + transferTo, "Transfer", "Finished", Double.parseDouble(amount), savingsAccount.getAccountBalance(), savingsAccount);
            savingsTransactionRepository.save(savingsTransaction);
            return "redirect:/userFront";
        } else {
            return "betweenAccounts";
        }
    }

    @RequestMapping(value = "/recipient", method = RequestMethod.GET)
    public String recipient(Model model, Principal principal) {
        List<Recipient> recipientList = transactionService.findRecipientList(principal);

        Recipient recipient = new Recipient();

        model.addAttribute("recipientList", recipientList);
        model.addAttribute("recipient", recipient);

        return "recipient";
    }

    @RequestMapping(value = "/recipient/save", method = RequestMethod.POST)
    public String recipientPost(@ModelAttribute("recipient") Recipient recipient, Principal principal) {

        User user = userService.findByUsername(principal.getName());
        recipient.setUser(user);
        transactionService.saveRecipient(recipient);

        return "redirect:/transfer/recipient";
    }

    @RequestMapping(value = "/recipient/edit", method = RequestMethod.GET)
    public String recipientEdit (@RequestParam(value = "recipientName") String recipientName, Model model,
                                 Principal principal) {
        Recipient recipient = transactionService.findRecipientByName(recipientName);
        List<Recipient> recipients = transactionService.findRecipientList(principal);
        model.addAttribute("recipients", recipients);
        model.addAttribute("recipient", recipient);

        return "recipient";
    }

    @RequestMapping (value = "/recipient/delete", method = RequestMethod.GET)
    public String recipientDelete (@RequestParam (value = "recipientName") String recipientName, Model model,
                                   Principal principal) {
        transactionService.deleteRecipientByName(recipientName);

        List<Recipient> recipients = transactionService.findRecipientList(principal);
        Recipient recipient = new Recipient();

        model.addAttribute("recipients", recipients);
        model.addAttribute("recipient", recipient);

        return "recipient";
    }

    @RequestMapping(value = "/toSomeoneElse", method = RequestMethod.GET)
    public String toSomeoneElse (Model model, Principal principal) {
        List<Recipient> recipients = transactionService.findRecipientList(principal);

        model.addAttribute("recipients", recipients);
        model.addAttribute("amount", "");

        return "toSomeoneElse";
    }

    @RequestMapping(value = "/toSomeoneElse", method = RequestMethod.POST)
    public String toSomeoneElsePost (@ModelAttribute("recipientName") String recipientName, @ModelAttribute("accountType")
            String accountType, @ModelAttribute("amount") String amount, Principal principal, Model model) {

        User user = userService.findByUsername(principal.getName());
        Recipient recipient = transactionService.findRecipientByName(recipientName);
        PrimaryAccount primaryAccount = user.getPrimaryAccount();
        SavingsAccount savingsAccount = user.getSavingsAccount();

        if (accountType.equalsIgnoreCase("Primary")) {
            if (primaryAccount.getAccountBalance().compareTo(new BigDecimal(amount)) < 0) {
                model.addAttribute("wrongTransaction", true);
                return "toSomeoneElse";
            } else {transactionService.fromPrimarytoSomeoneElseTransfer(recipient, accountType, amount,
                    user.getPrimaryAccount(), user.getSavingsAccount());}

        } else if (accountType.equalsIgnoreCase("Savings")) {
            if (savingsAccount.getAccountBalance().compareTo(new BigDecimal(amount)) < 0) {
                model.addAttribute("wrongTransaction", true);
                return "toSomeoneElse";
            } else {transactionService.fromSavingstoSomeoneElseTransfer(recipient, accountType, amount,
                    user.getPrimaryAccount(), user.getSavingsAccount());}
        } else {model.addAttribute("selectRecipient", true);
            return "toSomeoneElse"; }


        return "redirect:/userFront";
    }
}