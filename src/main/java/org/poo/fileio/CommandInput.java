package org.poo.fileio;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public final class CommandInput {
    private String command;
    private String email;
    private String account;
    private String newPlanType;
    private String role;
    private String currency;
    private String target;
    private String description;
    private String cardNumber;
    private String commerciant;
    private String receiver;
    private String alias;
    private String accountType;
    private String splitPaymentType;
    private String type;
    private String location;
    private int timestamp;
    private int startTimestamp;
    private int endTimestamp;
    private double interestRate;
    private double spendingLimit;
    private double depositLimit;
    private double amount;
    private double minBalance;
    private List<String> accounts;
    private List<Double> amountForUsers;

    // constructor for split payment
    public CommandInput(final String currency, final double amount,
                        final List<String> accounts, final List<Double> amountForUsers,
                        final String splitPaymentType, final int timestamp) {
        this.currency = currency;
        this.amount = amount;
        this.accounts = accounts;
        this.amountForUsers = amountForUsers;
        this.splitPaymentType = splitPaymentType;
        this.timestamp = timestamp;
    }

    public String getCommand() {
        return command;
    }

    public String getEmail() {
        return email;
    }

    public String getAccount() {
        return account;
    }

    public String getCurrency() {
        return currency;
    }

    public double getAmount() {
        return amount;
    }

    public double getMinBalance() {
        return minBalance;
    }

    public String getTarget() {
        return target;
    }

    public String getDescription() {
        return description;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCommerciant() {
        return commerciant;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getStartTimestamp() {
        return startTimestamp;
    }

    public int getEndTimestamp() {
        return endTimestamp;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getAlias() {
        return alias;
    }

    public String getAccountType() {
        return accountType;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public List<String> getAccounts() {
        return accounts;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public void setCardNumber(final String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setAccount(final String accountNumber) {
        this.account = accountNumber;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setAmmount(final double newAmount) {
        this.amount = newAmount;
    }

    public void setTimestamp(final int timestamp) {
        this.timestamp = timestamp;
    }

    public String getNewPlanType() {
        return newPlanType;
    }

    public String getRole() {
        return role;
    }

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public double getSpendingLimit() {
        return spendingLimit;
    }

    public double getDepositLimit() {
        return depositLimit;
    }

    public List<Double> getAmountForUsers() {
        return amountForUsers;
    }

    public String getSplitPaymentType() {
        return splitPaymentType;
    }

    public void setAccounts(List<String> accounts) {
        this.accounts = accounts;
    }

    public void setNewPlanType(String newPlanType) {
        this.newPlanType = newPlanType;
    }
}
