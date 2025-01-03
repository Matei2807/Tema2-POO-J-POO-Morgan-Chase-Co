package org.poo.main.Accounts;

import org.poo.fileio.CommandInput;
import org.poo.main.Card;
import org.poo.main.Commerciant;
import org.poo.main.Cashback.TransactionInfoForCashback;
import org.poo.main.Users.User;
import org.poo.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Account {
    protected String accountNumber;
    protected double balance;
    protected String currency;
    protected List<Card> cards;
    protected double minBalance;
    protected int minBalanceTimestamp;
    protected Map<Commerciant, TransactionInfoForCashback> commerciants; // // commerciants that the user has interacted with // TODO : remove this
    protected String type; // savings, classic, business

    private boolean isNull = false;

    public Account(final CommandInput command, final User user) {
        accountNumber = Utils.generateIBAN();
        balance = 0;
        currency = command.getCurrency();
        cards = new ArrayList<>();
        commerciants = new HashMap<>();
        type = command.getAccountType();
    }

    public Account() { // Constructor for NullAccount
        accountNumber = "";
        balance = 0;
        currency = "";
        cards = new ArrayList<>();
        commerciants = new HashMap<>();
        type = "";
        isNull = true;
    }

    /**
     * Add interest to the account
     */
    public abstract void addInterest();

    /**
     * Returns the type of the account
     */
    public String getType() {
        return type;
    }

    /**
     * Add funds to the account
     * @param amount the amount to add
     */
    public void addFunds(final double amount) {
        balance += amount;
    }

    /**
     * Deduct funds from the account
     * @param amount the amount to deduct
     */
    public void deductFunds(final double amount) {
        balance -= amount;
    }

    /**
     * Create a card for the account
     * @param oneTime if the card is one time
     * @return the created card
     */
    public Card createCard(final boolean oneTime) {
        Card card = new Card(oneTime);
        cards.add(card);
        return card;
    }

    public void addCard(final Card card) {
        cards.add(card);
    }

    /**
     * Delete a card from the account
     * @param cardNumber the card number to delete
     */
    public void deleteCard(final String cardNumber) {
        Card card = getCard(cardNumber);
        if (card != null) {
            cards.remove(card);
        }
    }

    /**
     * Get a card from the account by card number
     * @param cardNumber the card number to get
     * @return the card
     */
    public Card getCard(final String cardNumber) {
        for (Card card : cards) {
            if (card.getCardNumber().equals(cardNumber)) {
                return card;
            }
        }
        return null;
    }

    /**
     * Get the account number
     * @return the account number
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Get the balance of the account
     * @return the balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Get the currency of the account
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Get the cards of the account
     * @return the cards
     */
    public List<Card> getCards() {
        return cards;
    }

    /**
     * Set the minimum balance of the account
     * @param minBalance the minimum balance
     */
    public void setMinBalance(final double minBalance) {
        this.minBalance = minBalance;
    }

    /**
     * Get the minimum balance of the account
     * @return the minimum balance
     */
    public double getMinBalance() {
        return minBalance;
    }

    /**
     * Set the minimum balance timestamp
     * @param timestamp the timestamp
     */
    public void setMinBalanceTimestamp(final int timestamp) {
        minBalanceTimestamp = timestamp;
    }

    /**
     * Get the minimum balance timestamp
     * @return the timestamp
     */
    public int getMinBalanceTimestamp() {
        return minBalanceTimestamp;
    }

    /**
     * Add a transaction to the commerciant
     * @param commerciant the commerciant to add the transaction to
     * @param amount the amount of the transaction
     * @return the cashback percentage
     */
    public double addCommerciantTransaction(final Commerciant commerciant, final double amount, final User user) {
        if (!commerciants.containsKey(commerciant)) {
            commerciants.put(commerciant, new TransactionInfoForCashback());
        }
        commerciants.get(commerciant).addTransaction(amount);

        user.checkPlanUpgrade(amount);

        // return cashback percentage
        return commerciant.getCashbackStrategy().getCashback(getTransactionInfoForCashback(commerciant), commerciant.getType(), user, amount);
    }

    /**
     * Get the commerciants of the account
     * @return the commerciants
     */
    public Map<Commerciant, TransactionInfoForCashback> getCommerciants() {
        return commerciants;
    }

    /**
     * Check if the account is null
     * @return true if the account is null, false otherwise
     */
    public boolean isNull() {
        return isNull;
    }

    /**
     * Get a commerciant from the account by name
     * @param name the name of the commerciant
     * @return the commerciant
     */
    public Commerciant findCommerciant(final String name) {
        for (Commerciant commerciant : commerciants.keySet()) {
            if (commerciant.getName().equals(name)) {
                return commerciant;
            }
        }
        return null;
    }

    public double getInterestRate() {
        return 0;
    }

    public TransactionInfoForCashback getTransactionInfoForCashback(Commerciant commerciant) {
        return commerciants.get(commerciant);
    }
}
