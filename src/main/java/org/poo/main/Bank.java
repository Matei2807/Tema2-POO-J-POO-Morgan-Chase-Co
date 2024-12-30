package org.poo.main;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.*;
import org.poo.main.Accounts.*;
import org.poo.main.Cashback.TransactionInfoForCashback;
import org.poo.main.Transactions.CommerciantTransaction;
import org.poo.main.Transactions.Transaction;
import org.poo.main.Transactions.TransactionFactory;
import org.poo.main.Users.NullUser;
import org.poo.main.Users.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public final class Bank {
    private List<User> users = new ArrayList<>();
    private List<ExchangeRate> exchangeRates = new ArrayList<>();
    private Map<String, String> aliases = new HashMap<>(); // map alias -> accountNumber
    private List<Commerciant> commerciants = new ArrayList<>();
    // adding a split payment queue(waiting for the other users to accept/reject the payment)
    private Queue<SplitPayment> splitPaymentQueue = new LinkedList<>();

    private static Bank instance = null; // Singleton Pattern

    public static Bank getInstance() {
        if (instance == null) {
            instance = new Bank();
        }
        return instance;
    }

    private Bank() {}

    public void updateData(final ObjectInput inputData) {
        users.clear();
        exchangeRates.clear();
        aliases.clear();
        commerciants.clear();
        splitPaymentQueue.clear();

        for (UserInput userInput : inputData.getUsers()) {
            users.add(new User(userInput));
        }
        for (CommerciantInput commerciantInput : inputData.getCommerciants()) {
            commerciants.add(new Commerciant(commerciantInput));
        }

        if(inputData.getExchangeRates() != null) {
            for (ExchangeInput exchangeInput : inputData.getExchangeRates()) {
                exchangeRates.add(new ExchangeRate(exchangeInput));

                // also add the inverted rate
                ExchangeInput invertedRate = new ExchangeInput();
                invertedRate.setFrom(exchangeInput.getTo());
                invertedRate.setTo(exchangeInput.getFrom());
                invertedRate.setRate(1 / exchangeInput.getRate());
                invertedRate.setTimestamp(exchangeInput.getTimestamp());
                exchangeRates.add(new ExchangeRate(invertedRate));
            }
        }
    }

    /**
     * Runs the commands from the input file and creates the output file.
     * @param commands the commands to be executed
     * @param output the output file
     */
    public void runCommands(final CommandInput[] commands, final ArrayNode output) {
        for (CommandInput command : commands) {
            switch (command.getCommand()) {
                // debug commands
                case "printUsers":
                    printUsers(command, output);
                    break;
                case "printTransactions":
                    printTransactions(command, output);
                    break;
                // platform commands
                case "addAccount":
                    addAccount(command, output);
                    break;
                case "addFunds":
                    addFunds(command, output);
                    break;
                case "createCard":
                    createCard(command, output, false);
                    break;
                case "createOneTimeCard":
                    createCard(command, output, true);
                    break;
                case "deleteAccount":
                    deleteAccount(command, output);
                    break;
                case "deleteCard":
                    deleteCard(command, output);
                    break;
                case "setMinimumBalance":
                    setMinBalance(command, output);
                    break;
                case "checkCardStatus":
                    checkCardStatus(command, output);
                    break;
                case "payOnline":
                    payOnline(command, output);
                    break;
                case "sendMoney":
                    sendMoney(command, output);
                    break;
                case "setAlias":
                    setAlias(command, output);
                    break;
                case "splitPayment":
                    splitPayment(command, output); // TODO: add commission / redo function(check ocw)
                    break;
                case "addInterest":
                    addInterest(command, output);
                    break;
                case "changeInterestRate":
                    changeInterestRate(command, output);
                    break;
                case "report":
                    classicReport(command, output);
                    break;
                case "spendingsReport":
                    spendingReport(command, output);
                    break;
                case "upgradePlan":
                    upgradePlan(command, output);
                    break;
                case "withdrawSavings":
                    withdrawSavings(command, output);
                    break;
                case "addNewBusinessAssociate":
                    addNewBusinessAssociate(command, output);
                    break;
                case "changeSpendingLimit":
                    changeSpendingLimit(command, output);
                    break;
                case "changeDepositLimit":
                    changeDepositLimit(command, output);
                    break;
                case "cashWithdrawal":
                    cashWithdrawal(command, output);
                    break;
                case "acceptSplitPayment":
                    acceptSplitPayment(command, output);
                    break;
                case "rejectSplitPayment":
                    rejectSplitPayment(command, output);
                    break;
                case "businessReport":
                    businessReport(command, output);
                    break;
                default:
                    System.out.println("Invalid command: " + command.getCommand());
                    break;
            }
        }
    }

    private void cashWithdrawal(CommandInput command, ArrayNode output) {
        String cardNumber = command.getCardNumber();
        double amount = command.getAmount();
        String email = command.getEmail();
        String location = command.getLocation();
        int timestamp = command.getTimestamp();

        Account account = getAccountByCardNumber(cardNumber);
        if (account.isNull()) {
            System.out.println("Account not found for card: " + cardNumber);
            // TODO: create the transaction
            return;
        }

        Card card = account.getCard(cardNumber);
        if (card == null) {
            System.out.println("Card not found: " + cardNumber);
            // TODO: create the transaction
            return;
        }

        if (card.getStatus().equals("frozen")) {
            // TODO : check if transaction is needed
//            Transaction transaction = TransactionFactory.createTransaction(command,
//                                                             "cardBlocked",
//                                                                "");
//            User user = getUserByIBAN(account.getAccountNumber());
//            user.addTransaction(transaction);
            return;
        } else if (card.getStatus().equals("used")) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "Card has already been used");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        // add commission to the amount
        double exchangeRateRON = getExchangeRate(account.getCurrency(), "RON");
        double spentAmountInRON = amount * exchangeRateRON;
        String planType = account.getPlan();
        switch (planType) {
            case "standard":
                amount *= 1.002;
                break;
            case "silver":
                if (spentAmountInRON >= 500) {
                    amount *= 1.001;
                }
                break;
            default:
                break;
        }

        if (account.getBalance() < amount) {
            Transaction transaction = TransactionFactory.createTransaction(command,
                                                             "noFunds",
                                                                "");
            User user = getUserByIBAN(account.getAccountNumber());
            user.addTransaction(transaction);
            return;
        }

        account.deductFunds(amount);

        Transaction transaction = TransactionFactory.createTransaction(command, "cashWithdrawal", "");
        User user = getUserByIBAN(account.getAccountNumber());
        user.addTransaction(transaction);
    }

    private void changeDepositLimit(CommandInput command, ArrayNode output) {
        String account = command.getAccount();
        String userEmail = command.getEmail();
        double amount = command.getAmount();
        int timestamp = command.getTimestamp();

        User user = getUserByEmail(userEmail);
        Account userAccount = user.getAccount(account);
        if (userAccount.isNull()) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "Account not found");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        //check if it is a business account
        if (!userAccount.getType().equals("business")) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "Account is not of type business");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        //check if the user is the owner of the account
        BusinessAccount businessAccount = (BusinessAccount) userAccount;
        if (!businessAccount.getOwner().equals(userEmail)) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "You are not authorized to make this transaction");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        businessAccount.setDepositLimit(amount);
    }

    private void changeSpendingLimit(CommandInput command, ArrayNode output) {
        String userEmail = command.getEmail();
        String accountNumber = command.getAccount();
        double amount = command.getAmount();
        int timestamp = command.getTimestamp();

        User user = getUserByEmail(userEmail);
        Account account = user.getAccount(accountNumber);
        if (account.isNull()) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "Account not found");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        if (!account.getType().equals("business")) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "Account is not of type business");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        BusinessAccount businessAccount = (BusinessAccount) account;
        // check if the user is the owner of the account
        if (!businessAccount.getOwner().equals(userEmail)) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "You are not authorized to make this transaction");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        businessAccount.setSpendingLimit(amount);
    }

    private void addNewBusinessAssociate(CommandInput command, ArrayNode output) {
        String account = command.getAccount();
        String role = command.getRole();
        String associateEmil = command.getEmail();
        int timestamp = command.getTimestamp();

        User user = getUserByIBAN(account);
        Account userAccount = user.getAccount(account);
        if (userAccount.isNull()) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "Account not found");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        //check if it is a business account
        if (!userAccount.getType().equals("business")) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "Account is not of type business");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        //check if the user is the owner of the account
        BusinessAccount businessAccount = (BusinessAccount) userAccount;
        if (!businessAccount.getOwner().equals(user.getEmail())) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "You are not authorized to make this transaction");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        //check if the associate is already added
        if ((role.equals("manager") && businessAccount.getManagers().contains(associateEmil))
            || (role.equals("employee") && businessAccount.getEmployees().contains(associateEmil))) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "Associate already added");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        //add the associate
        if (role.equals("manager")) {
            businessAccount.addManager(associateEmil);
        } else {
            businessAccount.addEmployee(associateEmil);
        }

        // add the account to the associate
        User associate = getUserByEmail(associateEmil);
        associate.addAccount(userAccount);
    }

    private void withdrawSavings(CommandInput command, ArrayNode output) {
        String account = command.getAccount();
        double amount = command.getAmount();
        String currency = command.getCurrency();
        int timestamp = command.getTimestamp();

        User user = getUserByIBAN(account);
        Account savingsAccount = user.getAccount(account);

        if (savingsAccount.isNull()) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "Account not found");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            System.out.println("Account not found for withdraw: " + account);
            return;
        }

        if (user.getAge() < 21) {
            // TODO: create this transaction
            Transaction transaction = TransactionFactory.createTransaction(command, "withdrawSavingsAgeError", "");
            user.addTransaction(transaction);
            return;
        }

        if (!savingsAccount.getType().equals("savings")) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "Account is not of type savings.");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        Account currentAccount = null;
        for (Account acc : user.getAccounts()) { // find the first current account with the given currency
            if (acc.getType().equals("classic") && acc.getCurrency().equals(currency)) {
                currentAccount = acc;
                break;
            }
        }
        if (currentAccount == null) { // if no current account with the given currency is found, find the first current account
            for (Account acc : user.getAccounts()) {
                if (acc.getType().equals("classic")) {
                    currentAccount = acc;
                    break;
                }
            }
        }
        if (currentAccount == null) { // if no current account is found
            Transaction transaction = TransactionFactory.createTransaction(command, "noCurrentAccountError", "");
            user.addTransaction(transaction);
            return;
        }

        double exchangeRate = getExchangeRate(savingsAccount.getCurrency(), currentAccount.getCurrency());
        double convertedAmount = amount * exchangeRate; // ammount to add in the current account

        // add commsion to the amount
        double exchangeRateRON = getExchangeRate(currency, "RON");
        double spentAmountInRON = amount * exchangeRateRON;
        String planType = currentAccount.getPlan();
        switch (planType) {
            case "standard":
                amount *= 1.002;
                break;
            case "silver":
                if (spentAmountInRON >= 500) {
                    amount *= 1.001;
                }
                break;
            default:
                break;
        }

        if (savingsAccount.getBalance() < amount) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "Insufficient funds");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        savingsAccount.deductFunds(amount);
        currentAccount.addFunds(convertedAmount);

//        command.setAmmount(convertedAmount);
//        command.setAccount(currentAccount.getAccountNumber());
//        // TODO: create the withdraw savings transaction
//        Transaction transaction = TransactionFactory.createTransaction(command, "withdrawSavings", "");
//        user.addTransaction(transaction);
    }

    private void upgradePlan(CommandInput command, ArrayNode output) {
        String newPlanType = command.getNewPlanType();
        String account = command.getAccount();
        int timestamp = command.getTimestamp();

        User user = getUserByIBAN(account);
        Account userAccount = user.getAccount(account);
        if (userAccount.isNull()) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "Account not found");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        String currentPlan = userAccount.getPlan();
        if (currentPlan.equals(newPlanType)) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "The user already has the " + newPlanType + " plan.");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        // check downgrades
        boolean downgrade = false;
        switch (currentPlan) {
            case "silver":
                if (newPlanType.equals("standard") || newPlanType.equals("student")) {
                    downgrade = true;
                }
                break;
            case "gold":
                if (newPlanType.equals("standard") || newPlanType.equals("student") || newPlanType.equals("silver")) {
                    downgrade = true;
                }
                break;
            default:
                break;
        }
        if (downgrade) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "You cannot downgrade your plan.");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        // check if the user has enough funds
        double fee = 0; // in RON
        switch (currentPlan) {
            case "standard":
            case "student":
                if (newPlanType.equals("silver")) {
                    fee = 100;
                } else if (newPlanType.equals("gold")) {
                    fee = 350;
                }
                break;
            case "silver":
                if (newPlanType.equals("gold")) {
                    fee = 250;
                }
                break;
            default:
                break;
        }

        double amount = fee * getExchangeRate("RON", userAccount.getCurrency());
        if (userAccount.getBalance() < amount) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "Insufficient funds");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        userAccount.deductFunds(amount);
        userAccount.setPlan(newPlanType);

        Transaction transaction = TransactionFactory.createTransaction(command, "upgradePlan", "");
        user.addTransaction(transaction);
    }

    private void printUsers(final CommandInput command, final ArrayNode output) {
        int timestamp = command.getTimestamp();

        ObjectNode commandOutput = output.objectNode();
        commandOutput.put("command", command.getCommand());
        ArrayNode usersArray = commandOutput.putArray("output");

        if (users.isEmpty()) {
            System.out.println("No users found");
            return;
        }
        for (User user : users) {
            ObjectNode userNode = usersArray.objectNode();
            userNode.put("firstName", user.getFirstName());
            userNode.put("lastName", user.getLastName());
            userNode.put("email", user.getEmail());
            ArrayNode accountsArray = userNode.putArray("accounts");

            if (!Objects.isNull(user.getAccounts())) {
                for (Account account : user.getAccounts()) {
                    ObjectNode accountNode = accountsArray.objectNode();
                    accountNode.put("IBAN", account.getAccountNumber());
                    accountNode.put("balance", roundToTwoDecimals(account.getBalance())); // TODO CHECK format to 2 decimals - as double(not string)
                    accountNode.put("currency", account.getCurrency());
                    accountNode.put("type", account.getType());
                    ArrayNode cardsArray = accountNode.putArray("cards");

                    if (!Objects.isNull(account.getCards())) {
                        for (Card card : account.getCards()) {
                            ObjectNode cardNode = cardsArray.objectNode();
                            cardNode.put("cardNumber", card.getCardNumber());
                            cardNode.put("status", card.getStatus().equals("used") ? "active" : card.getStatus());
                            cardsArray.add(cardNode);
                        }
                    }
                    accountsArray.add(accountNode);
                }
            }
            usersArray.add(userNode);
        }
        commandOutput.put("timestamp", timestamp);
        output.add(commandOutput);
    }

    private void printTransactions(final CommandInput command, final ArrayNode output) {
        String email = command.getEmail();
        int startTimestamp = command.getTimestamp();
        User user = getUserByEmail(email);

        ObjectNode commandOutput = output.objectNode();
        commandOutput.put("command", command.getCommand());
        ArrayNode transactionsArray = commandOutput.putArray("output");
        if (Objects.isNull(user.getTransactions())) {
            System.out.println("No transactions found for: " + email);
            return;
        }

        for (Transaction transaction : user.getTransactions()) {
            transaction.printJSONObject(transactionsArray);
        }
        commandOutput.put("timestamp", startTimestamp);
        output.add(commandOutput);
    }

    private void addAccount(final CommandInput command, final ArrayNode output) {
        String email = command.getEmail();
        User user = getUserByEmail(email);
        if (user.isNull()) {
            System.out.println("User not found for: " + email);
            return;
        }

        Account newAccount;
        if (command.getAccountType().equals("savings")) {
            newAccount = new SavingsAccount(command, user);
        } else if (command.getAccountType().equals("classic")) {
            newAccount = new CurrentAccount(command, user);
        } else { // business account
            newAccount = new BusinessAccount(command, user);
        }
        user.addAccount(newAccount);

        String accountIBAN = newAccount.getAccountNumber();
        command.setAccount(accountIBAN);
        Transaction transaction = TransactionFactory.createTransaction(command, "addAccount", "");
        user.addTransaction(transaction);
    }

    private void addFunds(final CommandInput command, final ArrayNode output) {
        String accountNumber = command.getAccount();
        double amount = command.getAmount();
        String email = command.getEmail();

        Account account = getAccountByIBAN(accountNumber);

        // check if it is a business account and the deposit limit
        if (account.getType().equals("business")) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            if (businessAccount.getDepositLimit() < amount) {
                // TODO: create the transaction
                System.out.println("Deposit limit exceeded for: " + accountNumber);
                return;
            }
        }

        if(account.isNull()) {
            System.out.println("Account not found: " + accountNumber);
            return;
        }
        account.addFunds(amount);
    }

    private void createCard(final CommandInput command, final ArrayNode output,
                            final boolean oneTime) {
        String accountNumber = command.getAccount();
        String email = command.getEmail();
        User user = getUserByEmail(email);
        if (user.isNull()) {
            System.out.println("User not found for: " + email);
            return;
        }

        Account account = user.getAccount(accountNumber);
        if (account == null) {
            Transaction transaction = TransactionFactory.createTransaction(command,
                                                             "createCardError",
                                                                "");
            user.addTransaction(transaction);
            return;
        }

        Card card = account.createCard(oneTime);
        card.setOwnerEmail(email);
        String cardNumber = card.getCardNumber();
        command.setCardNumber(cardNumber);

        Transaction transaction = TransactionFactory.createTransaction(command, "createCard", "");
        user.addTransaction(transaction);
    }

    private void deleteAccount(final CommandInput command, final ArrayNode output) {
        String accountNumber = command.getAccount();
        String email = command.getEmail();
        int timestamp = command.getTimestamp();

        Account account = getAccountByIBAN(accountNumber);
        if (account.isNull()) {
            System.out.println("Account not found: " + accountNumber);
            return;
        } else if (account.getBalance() != 0) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("error",
                         "Account couldn't be deleted - see org.poo.transactions for details");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);

            Transaction transaction = TransactionFactory.createTransaction(command,
                                                             "deleteAccountError",
                                                                "");
            User user = getUserByIBAN(accountNumber);
            user.addTransaction(transaction);
            return;
        }

        User user = getUserByEmail(email);
        // if the account is for a business, check if the user is the owner
        if (account.getType().equals("business")) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            if (!businessAccount.getOwner().equals(email)) {
                // TODO: create the transaction
                System.out.println("You are not the owner of the account: " + accountNumber);
                return;
            }
        }

        // delete the account from the owner, managers, and employees
        if (account.getType().equals("business")) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            for (String manager : businessAccount.getManagers()) {
                User managerUser = getUserByEmail(manager);
                managerUser.deleteAccount(accountNumber);
            }
            for (String employee : businessAccount.getEmployees()) {
                User employeeUser = getUserByEmail(employee);
                employeeUser.deleteAccount(accountNumber);
            }
        }

        user.deleteAccount(accountNumber);

        ObjectNode commandOutput = output.objectNode();
        commandOutput.put("command", command.getCommand());
        ObjectNode accountNode = commandOutput.putObject("output");
        accountNode.put("success", "Account deleted");
        accountNode.put("timestamp", timestamp);
        commandOutput.put("timestamp", timestamp);
        output.add(commandOutput);
    }

    private void deleteCard(final CommandInput command, final ArrayNode output) {
        String cardNumber = command.getCardNumber();
        String email = command.getEmail();

        Account account = getAccountByCardNumber(cardNumber);
        User user = getUserByIBAN(account.getAccountNumber());

        // check if the user is the owner of the card or if the user is a manager/owner of the business account
        if (account.getType().equals("business")) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            if (!businessAccount.getOwner().equals(email) && !businessAccount.getManagers().contains(email)
                && !businessAccount.getCard(cardNumber).getOwnerEmail().equals(email)) {
                System.out.println("You are not authorized to delete the card: " + cardNumber);
                return;
            }
        }

        command.setAccount(account.getAccountNumber());
        command.setEmail(user.getEmail());
        Transaction transaction = TransactionFactory.createTransaction(command, "deleteCard", "");
        user.addTransaction(transaction);

        account.deleteCard(cardNumber);
    }

    private void setMinBalance(final CommandInput command, final ArrayNode output) {
        double minBalance = command.getAmount();
        String accountNumber = command.getAccount();
        String email = command.getEmail();
        int timestamp = command.getTimestamp();

        Account account = getAccountByIBAN(accountNumber);


        if (account.isNull()) {
            System.out.println("Account not found: " + accountNumber);
            return;
        }

        //if it is a business account, the owner account should be checked
        if (account.getType().equals("business")) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            if (!businessAccount.getOwner().equals(email)) {
                // TODO : create the transaction
                System.out.println("You are not the owner of the account: " + accountNumber);
                return;
            }
        }

        account.setMinBalance(minBalance);
        account.setMinBalanceTimestamp(timestamp);
        if (account.getBalance() < minBalance) {
            for (Card card : account.getCards()) {
                card.setStatus("frozen");
            }
        }
    }

    private void checkCardStatus(final CommandInput command, final ArrayNode output) {
        String cardNumber = command.getCardNumber();
        int timestamp = command.getTimestamp();

        Account account = getAccountByCardNumber(cardNumber);
        if (account.isNull()) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode cardNode = commandOutput.putObject("output");
            cardNode.put("timestamp", timestamp);
            cardNode.put("description", "Card not found");
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        Card card = account.getCard(cardNumber);
        if (card == null) {
            System.out.println("Card not found: " + cardNumber);
            return;
        }

        if (account.getBalance() < account.getMinBalance() || account.getBalance() == 0) {
            card.setStatus("frozen");
            Transaction transaction = TransactionFactory.createTransaction(command,
                                                             "cardWillBeBlocked",
                                                                "");
            User user = getUserByIBAN(account.getAccountNumber());
            user.addTransaction(transaction);
        }
    }

    private void payOnline(final CommandInput command, final ArrayNode output) {
        String cardNumber = command.getCardNumber();
        double amount = command.getAmount();
        String currency = command.getCurrency();
        int timestamp = command.getTimestamp();
        String commerciantName = command.getCommerciant();
        String email = command.getEmail();

        Account account = getAccountByCardNumber(cardNumber);
        if (account.isNull()) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode cardNode = commandOutput.putObject("output");
            cardNode.put("timestamp", timestamp);
            cardNode.put("description", "Card not found");
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        Card card = account.getCard(cardNumber);
        if (card.getStatus().equals("frozen")) {
            Transaction transaction = TransactionFactory.createTransaction(command,
                                                             "cardBlocked",
                                                                "");
            User user = getUserByIBAN(account.getAccountNumber());
            user.addTransaction(transaction);
            return;
        }

        String senderCurrency = account.getCurrency();
        double exchangeRate = getExchangeRate(currency, senderCurrency);
        if (exchangeRate == -1) {
            System.out.println("Exchange rate not found for " + currency + " to " + senderCurrency);
            return;
        }

        double convertedAmount = amount * exchangeRate;
        double convertedAmountPreCommission = convertedAmount;

        // add the commission // TODO: check if it is correct
        double exchangeRateRON = getExchangeRate(senderCurrency, "RON");
        double spentAmountInRON = amount * exchangeRateRON;
        String planType = account.getPlan();
        switch (planType) {
            case "standard":
                convertedAmount *= 1.002;
                break;
            case "silver":
                if (spentAmountInRON >= 500) {
                    convertedAmount *= 1.001;
                }
                break;
            default:
                break;
        }

        // if it is a business acount check the spending limit
        if (account.getType().equals("business")) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            // TODO : maybe the owner can pay more than the limit
            if (businessAccount.getSpendingLimit() < convertedAmount) { // TODO : maybe check the transaction type
                Transaction transaction = TransactionFactory.createTransaction(command, "noFunds", "");
                User user = getUserByEmail(email);
                user.addTransaction(transaction);
                return;
            }
        }

        if (account.getBalance() < convertedAmount) {
            Transaction transaction = TransactionFactory.createTransaction(command, "noFunds", "");
            User user = getUserByEmail(email);
            user.addTransaction(transaction);
            return;
        } else if (account.getBalance() - convertedAmount < account.getMinBalance()) {
            account.getCard(cardNumber).setStatus("frozen");
            command.setTimestamp(account.getMinBalanceTimestamp());
            Transaction transaction = TransactionFactory.createTransaction(command,
                                                             "cardWillBeBlocked",
                                                                "");
            Transaction transaction2 = TransactionFactory.createTransaction(command,
                                                              "cardBlocked",
                                                                 "");

            User user = getUserByIBAN(account.getAccountNumber());
            user.addTransaction(transaction);
            user.addTransaction(transaction2);
            return;
        }
        // else the user has enough funds
        User userByEmail = getUserByEmail(email);
        if (userByEmail.isNull() || userByEmail.getAccount(account.getAccountNumber()) == null) {
            System.out.println("User not found for email: " + email);
            return;
        }

        account.deductFunds(convertedAmount);

        command.setAmmount(convertedAmountPreCommission);
        command.setAccount(account.getAccountNumber());
        Transaction transaction = TransactionFactory.createTransaction(command,
                                                         "commerciantTransaction",
                                                            "");
        userByEmail.addTransaction(transaction);

        //Map<String, Double> cashbackMap = account.getCashbackMap(); // get the cashback map for the account
        Commerciant commerciant = account.findCommerciant(commerciantName);
        if (commerciant != null) { // can check for cashback
            TransactionInfoForCashback transactionInfo = account.getTransactionInfoForCashback(commerciant);
            String comerciantType = commerciant.getType();
            double cashback = commerciant.getCashbackStrategy().getCashback(transactionInfo, comerciantType, account) * amount * exchangeRate;
            account.addFunds(cashback);
            System.out.println("Found cashback for user: " + email + " and commerciant: " + commerciantName + " of: " + cashback + " - " + cashback / amount / exchangeRate + "%");
        }

//        for (Commerciant c : commerciants) {
//            if (c.getName().equals(commerciantName)) {
//                commerciant = c; // find the commerciant in the list of commerciants by name
//                break;
//            }
//        }
//        if (commerciant == null) {
//            System.out.println("ERROR: Commerciant not found: " + commerciantName);
//            return;
//        }

//        if (cashbackMap.get("All") != 0.0 && commerciant.getCashbackStrategy().equals("spendingThreshold")) {
//            double cashback = cashbackMap.get("All") * amount * exchangeRate; // TODO : check currency
//            account.addFunds(cashback);
//        } else if (cashbackMap.containsKey(commerciant.getType())) {
//            double cashback = cashbackMap.get(commerciant.getType()) * amount * exchangeRate; // TODO : check currency
//            account.addFunds(cashback);
//        }

        // adds the sale to the commerciant in the account
        Commerciant commerciantToAdd = account.findCommerciant(commerciantName);
        if (commerciantToAdd == null) { // if the commerciant is not found
            // find the commerciant in the list of commerciants
            for (Commerciant c : commerciants) {
                if (c.getName().equals(commerciantName)) {
                    commerciantToAdd = c;
                    break;
                }
            }
            if (commerciantToAdd == null) {
                System.out.println("ERROR: Commerciant not found: " + commerciantName);
                return;
            }
        }
        commerciantToAdd.addSale(convertedAmount);

        spentAmountInRON = amount * exchangeRateRON;
        account.addCommerciant(commerciantToAdd , spentAmountInRON);

        if (card.isOneTime()) {
//            // delete the card
//            command.setAccount(account.getAccountNumber());
//            command.setEmail(email);
//            Transaction transactionDelete = TransactionFactory.createTransaction(command,
//                                                                   "deleteCard",
//                                                                      "");
//            userByEmail.addTransaction(transactionDelete);
//            account.deleteCard(cardNumber);
            card.setStatus("used");

            Transaction transactionDestroyed = TransactionFactory.createTransaction(command,
                                                                   "cardDestroyed",
                                                                      "");
            userByEmail.addTransaction(transactionDestroyed);

            // create a new one time card
            Card newCard = account.createCard(true);
            String newCardNumber = newCard.getCardNumber();
            command.setCardNumber(newCardNumber);
            Transaction transactionCreate = TransactionFactory.createTransaction(command,
                                                                   "createCard",
                                                                      "");
            userByEmail.addTransaction(transactionCreate);
        }
    }

    private void sendMoney(final CommandInput command, final ArrayNode output) {
        String senderIBAN = command.getAccount();
        double amount = command.getAmount();
        String receiverIBAN = command.getReceiver();

        Account sender = getAccountByIBAN(senderIBAN);
        Account receiver = getAccountByIBAN(receiverIBAN);
        if (receiver.isNull()) {
            String alias = command.getReceiver();
            if (aliases.containsKey(alias)) { // check if an alias is used
                receiverIBAN = aliases.get(alias);
                receiver = getAccountByIBAN(receiverIBAN);
            }
        }

        String senderCurrency = sender.getCurrency();
        String receiverCurrency = receiver.getCurrency();
        double exchangeRate = getExchangeRate(senderCurrency, receiverCurrency);
        if (exchangeRate == -1) {
            System.out.println("Exchange rate not found for " + senderCurrency
                               + " to " + receiverCurrency);
            return;
        }

        // add commission to the amount // TODO: check if it is correct
        double exchangeRateRON = getExchangeRate(senderCurrency, "RON");
        double spentAmountInRON = amount * exchangeRateRON;
        double amountPreCommission = amount;
        String planType = sender.getPlan();
        switch (planType) {
            case "standard":
                amount *= 1.002;
                break;
            case "silver":
                if (spentAmountInRON >= 500) {
                    amount *= 1.001;
                }
                break;
            default:
                break;
        }

        // if it is a business acount check the spending limit
        if (sender.getType().equals("business")) {
            BusinessAccount businessAccount = (BusinessAccount) sender;
            // TODO : maybe the owner can pay more than the limit
            if (businessAccount.getSpendingLimit() < amount) { // TODO : maybe check the transaction type
                Transaction transaction = TransactionFactory.createTransaction(command, "noFunds", "");
                User user = getUserByIBAN(senderIBAN);
                user.addTransaction(transaction);
                return;
            }
        }

        double convertedAmount = amountPreCommission * exchangeRate;
        if (sender.getBalance() < amount) { // checks if the sender has enough funds
            Transaction transaction = TransactionFactory.createTransaction(command, "noFunds", "");
            User user = getUserByIBAN(senderIBAN);
            user.addTransaction(transaction);
            return;
        }

        sender.deductFunds(amount);
        receiver.addFunds(convertedAmount);
        command.setCurrency(senderCurrency);
        Transaction senderTransaction = TransactionFactory.createTransaction(command,
                                                               "normalTransaction",
                                                                  "sent");

        command.setCurrency(receiverCurrency);
        command.setAmmount(convertedAmount);
        Transaction receiverTransaction = TransactionFactory.createTransaction(command,
                                                                 "normalTransaction",
                                                                    "received");

        getUserByIBAN(senderIBAN).addTransaction(senderTransaction);
        getUserByIBAN(receiverIBAN).addTransaction(receiverTransaction);
    }

    private void setAlias(final CommandInput command, final ArrayNode output) {
        String alias = command.getAlias();
        String accountNumber = command.getAccount();
        String email = command.getEmail();

        aliases.put(alias, accountNumber);
    }

    private void rejectSplitPayment(CommandInput command, ArrayNode output) {
        String email = command.getEmail();
        int timestamp = command.getTimestamp();

        // go through the split payment queue and find the first split payment with the given email
        for (SplitPayment splitPayment : splitPaymentQueue) {
            if (splitPayment.containsEmail(email)) {
                splitPaymentQueue.remove(splitPayment); // the split payment is rejected
                break;
            }
        }

        ObjectNode commandOutput = output.objectNode();
        commandOutput.put("command", command.getCommand());
        ObjectNode accountNode = commandOutput.putObject("output");
        accountNode.put("description", "One user rejected the payment");
        accountNode.put("timestamp", timestamp);
        commandOutput.put("timestamp", timestamp);
        output.add(commandOutput);

        //TODO: maybe add a transaction
    }

    private void acceptSplitPayment(CommandInput command, ArrayNode output) {
        String email = command.getEmail();
        int timestamp = command.getTimestamp();

        // go through the split payment queue and find the first split payment with the given email
        for (SplitPayment splitPayment : splitPaymentQueue) {
            if (splitPayment.containsEmail(email)) {
                splitPayment.acceptAccount(email);
                if (splitPayment.isAccepted()) {
                    splitPaymentQueue.remove(splitPayment); // the split payment is accepted
                    executeSplitPayment(splitPayment, output);
                }
                break;
            }
        }
    }

    private void executeSplitPayment(final SplitPayment splitPayment, final ArrayNode output) {
        double amount = splitPayment.getAmount();
        String currency = splitPayment.getCurrency();
        List<String> accounts = splitPayment.getAccounts();
        List<Double> amountsForAccounts = splitPayment.getAmountsForAccounts();
        String type = splitPayment.getSplitPaymentType();

        if (type.equals("equal")) {
            // add the split amount to the list of amounts for accounts
            double splitAmount = amount / accounts.size();
            for (int i = 0; i < accounts.size(); i++) {
                amountsForAccounts.add(splitAmount);
            }
        }

        List<Account> accountList = new ArrayList<>();
        boolean insufficientFunds = false;
        String insufficientAccount = null;

        // go through all the accounts and check if they have enough funds
        int index = 0;
        for (String accountNumber : accounts) {
            Account account = getAccountByIBAN(accountNumber);
            if (account.isNull()) {
                System.out.println("Account not found: " + accountNumber);
                return;
            }
            accountList.add(account);

            double exchangeRate = getExchangeRate(currency, account.getCurrency());
            if (exchangeRate == -1) {
                System.out.println("Exchange rate not found for " + currency
                        + " to " + account.getCurrency());
                return;
            }

            double splitAmount = amountsForAccounts.get(index);
            double convertedAmount = splitAmount * exchangeRate;
            if (account.getBalance() < convertedAmount) { // checks if the account has enough funds
                insufficientFunds = true;
                insufficientAccount = accountNumber;
            }

            index++;
            //TODO: maybe check if the account is a business(it can't do a split payment)
        }

        if (insufficientFunds) {
            for (Account account : accountList) {
                CommandInput command = new CommandInput(currency, amount, accounts, amountsForAccounts, type);

                command.setAccount(insufficientAccount);
                Transaction transaction = TransactionFactory.createTransaction(command,
                        "splitTransaction",
                        "error");
                if (transaction == null) {
                    System.out.println("Transaction not created for account: "
                            + account.getAccountNumber());
                    return;
                }
                transaction.splitAmount(accounts.size());

                User user = getUserByIBAN(account.getAccountNumber());
                user.addTransaction(transaction);
            }
            return;
        }

        index = 0;
        for (Account account : accountList) {
            double exchangeRate = getExchangeRate(currency, account.getCurrency());
            double splitAmount = amountsForAccounts.get(index);
            account.deductFunds(splitAmount * exchangeRate);

            CommandInput command = new CommandInput(currency, amount, accounts, amountsForAccounts, type);
            command.setCurrency(currency);
            Transaction transaction = TransactionFactory.createTransaction(command,
                    "splitTransaction",
                    "sent");
            if (transaction == null) {
                System.out.println("Transaction not created for account: "
                        + account.getAccountNumber());
                return;
            }
            transaction.splitAmount(accounts.size());

            User user = getUserByIBAN(account.getAccountNumber());
            user.addTransaction(transaction);

            index++;
        }
    }

    private void splitPayment(final CommandInput command, final ArrayNode output) { // TODO: CHANGE(check ocw)
        List<String> accounts = command.getAccounts();
        List<Double> amountsForAccounts = command.getAmountForUsers();
        String currency = command.getCurrency();
        double amount = command.getAmount();
        String splitPaymentType = command.getSplitPaymentType(); // equal/custom

        // add the split payment to the queue
        splitPaymentQueue.add(new SplitPayment(command));
    }

    private void addInterest(final CommandInput command, final ArrayNode output) {
        String accountNumber = command.getAccount();
        int timestamp = command.getTimestamp();

        Account account = getAccountByIBAN(accountNumber);
        if (account.isNull()) {
            System.out.println("Account not found: " + accountNumber);
            return;
        }

        if (!account.getType().equals("savings")) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "This is not a savings account");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        command.setAmmount(account.getBalance() * account.getInterestRate());
        command.setCurrency(account.getCurrency());
        account.addInterest();

        Transaction transaction = TransactionFactory.createTransaction(command, "addInterest", "");
        getUserByIBAN(accountNumber).addTransaction(transaction);
    }

    private void changeInterestRate(final CommandInput command, final ArrayNode output) {
        String accountNumber = command.getAccount();
        double interestRate = command.getInterestRate();
        int timestamp = command.getTimestamp();

        Account account = getAccountByIBAN(accountNumber);
        if (account.isNull()) {
            System.out.println("Account not found: " + accountNumber);
            return;
        }

        if (!account.getType().equals("savings")) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "This is not a savings account");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        ((SavingsAccount) account).setInterestRate(interestRate);
        Transaction transaction = TransactionFactory.createTransaction(command,
                                                         "changeInterestRate",
                                                            "");
        User user = getUserByIBAN(accountNumber);
        user.addTransaction(transaction);
    }

    private void classicReport(final CommandInput command, final ArrayNode output) {
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        int timestamp = command.getTimestamp();
        String accountNumber = command.getAccount();

        Account account = getAccountByIBAN(accountNumber);
        if (account.isNull()) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "Account not found");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        User user = getUserByIBAN(accountNumber);
        if (user.isNull()) {
            System.out.println("User not found for account: " + accountNumber);
            return;
        }

        ObjectNode commandOutput = output.objectNode();
        commandOutput.put("command", command.getCommand());
        ObjectNode accountNode = commandOutput.putObject("output");
        accountNode.put("IBAN", account.getAccountNumber());
        accountNode.put("balance", account.getBalance());
        accountNode.put("currency", account.getCurrency());
        ArrayNode transactionsArray = accountNode.putArray("transactions");

        // saved printed transactions by timestamp in a list to avoid duplicates between accounts
        List<Integer> printedTransactions = new ArrayList<>();
        for (Transaction transaction : user.getTransactions()) {
            if (transaction.getTimestamp() >= startTimestamp
                && transaction.getTimestamp() <= endTimestamp
                && transaction.checkTransactionForAccount(accountNumber)) {
                if (printedTransactions.contains(transaction.getTimestamp())) {
                    continue;
                }
                printedTransactions.add(transaction.getTimestamp());
                transaction.printJSONObject(transactionsArray);
            }
        }

        commandOutput.put("timestamp", timestamp);
        output.add(commandOutput);
    }

    private void spendingReport(final CommandInput command, final ArrayNode output) {
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        int timestamp = command.getTimestamp();
        String accountNumber = command.getAccount();

        Account account = getAccountByIBAN(accountNumber);
        if (account.isNull()) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("description", "Account not found");
            accountNode.put("timestamp", timestamp);
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        if (account.getType().equals("savings")) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("error", "This kind of report is not supported for a saving account");
            commandOutput.put("timestamp", timestamp);
            output.add(commandOutput);
            return;
        }

        User user = getUserByIBAN(accountNumber);
        if (user.isNull()) {
            System.out.println("User not found for account: " + accountNumber);
            return;
        }

        ObjectNode commandOutput = output.objectNode();
        commandOutput.put("command", command.getCommand());
        ObjectNode accountNode = commandOutput.putObject("output");
        accountNode.put("IBAN", account.getAccountNumber());
        accountNode.put("balance", account.getBalance());
        accountNode.put("currency", account.getCurrency());
        ArrayNode transactionsArray = accountNode.putArray("transactions");

        List<Commerciant> involvedCommerciants = new ArrayList<>();
        List<String> involvedCommerciantsNames = new ArrayList<>();

        for (Transaction transaction : user.getTransactions()) {
            if (transaction.getTimestamp() >= startTimestamp
                && transaction.getTimestamp() <= endTimestamp
                && transaction.getTransactionType().equals("commerciantTransaction")
                && transaction.checkTransactionForAccount(accountNumber)) {
                transaction.printJSONObject(transactionsArray);
                if (transaction.getTransactionType().equals("commerciantTransaction")) {
                    String commerciantName = ((CommerciantTransaction) transaction).
                                             getCommerciant();
                    // adds new commerciant to the list
                    if (!involvedCommerciantsNames.contains(commerciantName)) {
                        involvedCommerciantsNames.add(commerciantName);
                        Commerciant commerciant = new Commerciant(commerciantName);
                        commerciant.addSale(((CommerciantTransaction) transaction).getAmount());
                        involvedCommerciants.add(commerciant);
                    } else { // adds the sale to the existing commerciant
                        Commerciant commerciant = involvedCommerciants.
                                                  get(involvedCommerciantsNames.
                                                  indexOf(commerciantName));
                        commerciant.addSale(((CommerciantTransaction) transaction).getAmount());
                    }
                }
            }
        }

        //sorts the commerciants alphabetically by name
        involvedCommerciants.sort(Comparator.comparing(Commerciant::getName));

        ArrayNode commerciantsArray = accountNode.putArray("commerciants");
        for (Commerciant commerciant : involvedCommerciants) {
            ObjectNode commerciantNode = commerciantsArray.objectNode();
            commerciantNode.put("commerciant", commerciant.getName());
            commerciantNode.put("total", commerciant.getTotalSales());
            commerciantsArray.add(commerciantNode);
        }

        commandOutput.put("timestamp", timestamp);
        output.add(commandOutput);
    }

    private void businessReport(final CommandInput command, final ArrayNode output) {
        String type = command.getType(); // transaction/commerciant
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        String accountNumber = command.getAccount();
        int timestamp = command.getTimestamp();

        //TODO: continue from here(ocw)
    }

    private User getUserByEmail(final String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return new NullUser();
    }

    private User getUserByIBAN(final String iban) {
        for (User user : users) {
            Account account = user.getAccount(iban);
            if (!account.isNull()) {
                return user;
            }
        }
        return new NullUser();
    }

    private Account getAccountByIBAN(final String iban) {
        for (User user : users) {
            Account account = user.getAccount(iban);
            if (!account.isNull()) {
                return account;
            }
        }
        return new NullAccount();
    }

    private Account getAccountByCardNumber(final String cardNumber) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                Card card = account.getCard(cardNumber);
                if (card != null) {
                    return account;
                }
            }
        }
        return new NullAccount();
    }

    private double getExchangeRate(final String from, final String to) {
        if (from.equals(to)) {
            return 1;
        }
        for (ExchangeRate rate : exchangeRates) { // for direct exchange rates
            if (rate.getFrom().equals(from) && rate.getTo().equals(to)) {
                return rate.getRate();
            }
        }
        for (ExchangeRate rate1 : exchangeRates) { // for indirect exchange rates
            if (rate1.getFrom().equals(from)) {
                for (ExchangeRate rate2 : exchangeRates) {
                    if (rate2.getFrom().equals(rate1.getTo()) && rate2.getTo().equals(to)) {
                        return rate1.getRate() * rate2.getRate();
                    }
                }
            }
        }
        return -1;
    }

    public static double roundToTwoDecimals(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.stripTrailingZeros();
        int scale = bd.scale();

        if (scale <= 2) {
            return value;
        }

        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
