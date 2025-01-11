package org.poo.main;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.fileio.ExchangeInput;
import org.poo.fileio.ObjectInput;
import org.poo.fileio.UserInput;
import org.poo.fileio.CommerciantInput;
import org.poo.main.accounts.CurrentAccount;
import org.poo.main.accounts.SavingsAccount;
import org.poo.main.accounts.BusinessAccount;
import org.poo.main.accounts.NullAccount;
import org.poo.main.accounts.Account;
import org.poo.main.transactions.Transaction;
import org.poo.main.transactions.TransactionFactory;
import org.poo.main.transactions.BusinessAccountTransaction;
import org.poo.main.transactions.CommerciantTransaction;
import org.poo.main.users.NullUser;
import org.poo.main.users.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

public final class Bank {
    private List<User> users = new ArrayList<>();
    private List<ExchangeRate> exchangeRates = new ArrayList<>();
    private Map<String, String> aliases = new HashMap<>(); // map alias -> accountNumber
    private List<Commerciant> commerciants = new ArrayList<>();
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
                    splitPayment(command, output);
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
        double amountInRon = command.getAmount();
        String email = command.getEmail();
        User userEmail = getUserByEmail(email);
        Account account = getAccountByCardNumber(cardNumber);

        if (email.isEmpty()) {
            putSimpleOutput("description", "User not found", command, output);
            return;
        } else if (account.isNull() || !userEmail.getAccounts().contains(account)) {
            putSimpleOutput("description", "Card not found", command, output);
            return;
        }

        Card card = account.getCard(cardNumber);

        if (card.getStatus().equals("frozen")) {
            return;
        }

        // add commission to the amount
        double exchangeRateFromRON = getExchangeRate("RON", account.getCurrency());
        double withdrawAmountInCurrency = amountInRon * exchangeRateFromRON;
        User user = getUserByIBAN(account.getAccountNumber());

        if (account.getBalance() < withdrawAmountInCurrency) {
            Transaction transaction = TransactionFactory.createTransaction(command,
                    "noFunds",
                    "");
            user.addTransaction(transaction);
            return;
        }

        String planType = user.getPlan();
        if (account.getType().equals("business")) { // change the plan to the owner's plan
            User owner = getUserByEmail(((BusinessAccount) account).getOwner());
            System.out.println("Changing plan " + planType + " to " + owner.getPlan());
            planType = owner.getPlan();
        }

        withdrawAmountInCurrency = addCommission(withdrawAmountInCurrency, amountInRon, planType);

        account.deductFunds(withdrawAmountInCurrency);

        Transaction transaction = TransactionFactory.createTransaction(command, "cashWithdrawal", "");
        user.addTransaction(transaction);
    }

    private void changeDepositLimit(CommandInput command, ArrayNode output) {
        String account = command.getAccount();
        String userEmail = command.getEmail();
        double amount = command.getAmount();

        User user = getUserByEmail(userEmail);
        Account userAccount = user.getAccount(account);
        if (userAccount.isNull()) {
            putSimpleOutput("description", "Account not found", command, output);
            return;
        }

        if (!userAccount.getType().equals("business")) {
            putSimpleOutput("description", "This is not a business account", command, output);
            return;
        }

        //check if the user is the owner of the account
        BusinessAccount businessAccount = (BusinessAccount) userAccount;
        if (!businessAccount.getOwner().equals(userEmail)) {
            putSimpleOutput("description", "You must be owner in order to change deposit limit.", command, output);
            return;
        }

        businessAccount.setDepositLimit(amount);
    }

    private void changeSpendingLimit(CommandInput command, ArrayNode output) {
        String userEmail = command.getEmail();
        String accountNumber = command.getAccount();
        double amount = command.getAmount();

        User user = getUserByEmail(userEmail);
        Account account = user.getAccount(accountNumber);
        if (account.isNull()) {
            putSimpleOutput("description", "Account not found", command, output);
            return;
        }

        if (!account.getType().equals("business")) {
            putSimpleOutput("description", "This is not a business account", command, output);
            return;
        }

        BusinessAccount businessAccount = (BusinessAccount) account;
        // check if the user is the owner of the account
        if (!businessAccount.getOwner().equals(userEmail)) {
            putSimpleOutput("description", "You must be owner in order to change spending limit.", command, output);
            return;
        }

        businessAccount.setSpendingLimit(amount);
    }

    private void addNewBusinessAssociate(CommandInput command, ArrayNode output) {
        String account = command.getAccount();
        String role = command.getRole();
        String associateEmail = command.getEmail();
        int timestamp = command.getTimestamp();

        User user = getUserByIBAN(account);
        Account userAccount = user.getAccount(account);
        if (userAccount.isNull()) {
            putSimpleOutput("description", "Account not found", command, output);
            return;
        }

        //check if it is a business account
        if (!userAccount.getType().equals("business")) {
            putSimpleOutput("description", "This is not a business account", command, output);
            return;
        }

        BusinessAccount businessAccount = (BusinessAccount) userAccount;
        //check if the associate is already added
        if ((role.equals("manager") && businessAccount.getManagers().contains(associateEmail))
            || (role.equals("employee") && businessAccount.getEmployees().contains(associateEmail))) {
            putSimpleOutput("description", "Associate already added", command, output);
            return;
        } else if (businessAccount.hasUser(associateEmail)) {
            return;
        }

        //add the associate
        if (role.equals("manager")) {
            businessAccount.addManager(associateEmail);
        } else {
            businessAccount.addEmployee(associateEmail);
        }

        // add the account to the associate
        User associate = getUserByEmail(associateEmail);
        if (!associate.getAccounts().contains(userAccount)) {
            associate.addAccount(userAccount);
        }

        // the transactionInfoForSpendingThreshold will now be shared with the new associate(but first we combine the two)
        User owner = getUserByEmail(businessAccount.getOwner());
        owner.getTransactionInfoForSpendingThreshold().addTransactionInfo(associate.getTransactionInfoForSpendingThreshold());
        associate.setTransactionInfoForSpendingThreshold(owner.getTransactionInfoForSpendingThreshold());
    }

    private void withdrawSavings(CommandInput command, ArrayNode output) {
        String account = command.getAccount();
        double amount = command.getAmount();
        String currency = command.getCurrency();
        int timestamp = command.getTimestamp();

        User user = getUserByIBAN(account);
        Account savingsAccount = user.getAccount(account);
        if (savingsAccount.isNull()) {
            putSimpleOutput("description", "Account not found", command, output);
            return;
        }

        if (user.getAge() < 21) {
            Transaction transaction = TransactionFactory.createTransaction(command, "withdrawSavingsAgeError", "");
            user.addTransaction(transaction);
            return;
        }

        if (!savingsAccount.getType().equals("savings")) {
            putSimpleOutput("description", "Account is not of type savings", command, output);
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

        if (savingsAccount.getBalance() < amount) {
            return;
        }

        savingsAccount.deductFunds(amount);
        currentAccount.addFunds(convertedAmount);

        List<String> accounts = new ArrayList<>();
        accounts.add(savingsAccount.getAccountNumber());
        accounts.add(currentAccount.getAccountNumber());
        command.setAccounts(accounts);
        Transaction transaction = TransactionFactory.createTransaction(command, "withdrawSavings", "");
        user.addTransaction(transaction);
        user.addTransaction(transaction);
    }

    private void upgradePlan(CommandInput command, ArrayNode output) {
        String newPlanType = command.getNewPlanType();
        String account = command.getAccount();
        int timestamp = command.getTimestamp();

        User user = getUserByIBAN(account);
        Account userAccount = user.getAccount(account);
        if (userAccount.isNull()) {
            putSimpleOutput("description", "Account not found", command, output);
            return;
        }

        String currentPlan = user.getPlan();
        if (currentPlan.equals(newPlanType)) {
            Transaction transaction = TransactionFactory.createTransaction(command, "upgradePlanError", "samePlan");
            user.addTransaction(transaction);
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
            //TODO: create the transaction??
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
            Transaction transaction = TransactionFactory.createTransaction(command, "noFunds", "");
            user.addTransaction(transaction);
            return;
        }

        userAccount.deductFunds(amount);
        user.setPlan(newPlanType);

        Transaction transaction = TransactionFactory.createTransaction(command, "upgradePlan", "");
        user.addTransaction(transaction);
    }

    private void printUsers(final CommandInput command, final ArrayNode output) {
        int timestamp = command.getTimestamp();

        ObjectNode commandOutput = output.objectNode();
        commandOutput.put("command", command.getCommand());
        ArrayNode usersArray = commandOutput.putArray("output");

        if (users.isEmpty()) {
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
                    // if account is for business, we only print for the owner
                    if (account.getType().equals("business") && !((BusinessAccount) account).getOwner().equals(user.getEmail())) {
                        continue;
                    }

                    ObjectNode accountNode = accountsArray.objectNode();
                    accountNode.put("IBAN", account.getAccountNumber());
                    accountNode.put("balance", account.getBalance());
                    accountNode.put("currency", account.getCurrency());
                    accountNode.put("type", account.getType());
                    ArrayNode cardsArray = accountNode.putArray("cards");

                    if (!Objects.isNull(account.getCards())) {
                        for (Card card : account.getCards()) {
                            ObjectNode cardNode = cardsArray.objectNode();
                            cardNode.put("cardNumber", card.getCardNumber());
                            cardNode.put("status", card.getStatus());
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
            return;
        }

        //sort transactions by timestamp
        user.getTransactions().sort(Comparator.comparing(Transaction::getTimestamp));

        for (Transaction transaction : user.getTransactions()) {
            transaction.printJSONObject(transactionsArray);
        }
        commandOutput.put("timestamp", startTimestamp);
        output.add(commandOutput);
    }

    private void addAccount(final CommandInput command, final ArrayNode output) {
        String email = command.getEmail();
        User user = getUserByEmail(email);
        String currency = command.getCurrency();
        if (user.isNull()) {
            return;
        }

        Account newAccount;
        if (command.getAccountType().equals("savings")) {
            newAccount = new SavingsAccount(command, user);
        } else if (command.getAccountType().equals("classic")) {
            newAccount = new CurrentAccount(command, user);
        } else { // business account
            newAccount = new BusinessAccount(command, user);
            double exchangeRate = getExchangeRate("RON", currency); // set the deposit and spending limit
            ((BusinessAccount) newAccount).setDepositLimit(500 * exchangeRate);
            ((BusinessAccount) newAccount).setSpendingLimit(500 * exchangeRate);
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
        int timestamp = command.getTimestamp();

        Account account = getAccountByIBAN(accountNumber);

        // checks if it is a business account and the deposit limit
        if (account.getType().equals("business")) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            if (businessAccount.getDepositLimit() < amount && businessAccount.getEmployees().contains(email)) {
                // TODO: create the transaction??
                return;
            } else if (!businessAccount.hasUser(email)) {
                return;
            }
            // adds special transaction for business account
            BusinessAccountTransaction businessTransaction = new BusinessAccountTransaction(timestamp, email, "", -amount);
            businessAccount.addBusinessTransaction(businessTransaction);
        }

        if(account.isNull()) {
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
            return;
        } else if (account.getBalance() != 0) { // if the account has funds - error
            putSimpleOutput("error", "Account couldn't be deleted - see org.poo.transactions for details", command, output);
            Transaction transaction = TransactionFactory.createTransaction(command,
                                                             "deleteAccountError",
                                                                "");
            User user = getUserByIBAN(accountNumber);
            user.addTransaction(transaction);
            return;
        }

        User user = getUserByEmail(email);
        // if the account is for a business, checks if the user is the owner
        if (account.getType().equals("business")) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            if (!businessAccount.getOwner().equals(email)) {
                // TODO: create the transaction??
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
        putSimpleOutput("success", "Account deleted", command, output);
    }

    private void deleteCard(final CommandInput command, final ArrayNode output) {
        String cardNumber = command.getCardNumber();
        String email = command.getEmail();

        Account account = getAccountByCardNumber(cardNumber);
        User user = getUserByIBAN(account.getAccountNumber());

        if (!Objects.equals(user.getEmail(), email)) {
            return;
        }

        if (account.getBalance() > 0) {
            return;
        }

        // checks if the user is the owner of the card or if the user is a manager/owner of the business account
        if (account.getType().equals("business")) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            if (!businessAccount.getOwner().equals(email) && !businessAccount.getManagers().contains(email)
                && !businessAccount.getCard(cardNumber).getOwnerEmail().equals(email)) {
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
            return;
        }

        // if it is a business account, the owner account should be checked
        if (account.getType().equals("business")) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            if (!businessAccount.getOwner().equals(email)) {
                // TODO : create the transaction ??
                return;
            }
        }

        account.setMinBalance(minBalance);
        account.setMinBalanceTimestamp(timestamp);
//        if (account.getBalance() < minBalance) { // TODO : check if it should be blocked
//            for (Card card : account.getCards()) {
//                System.out.println("Card " + card.getCardNumber() + " will be blocked because of low balance time: " + timestamp);
//                card.setStatus("frozen");
//            }
//        }
    }

    private void checkCardStatus(final CommandInput command, final ArrayNode output) {
        String cardNumber = command.getCardNumber();
        int timestamp = command.getTimestamp();

        Account account = getAccountByCardNumber(cardNumber);
        if (account.isNull()) {
            putSimpleOutput("description", "Card not found", command, output);
            return;
        }

        Card card = account.getCard(cardNumber);
        if (card == null) {
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

        if (amount == 0) {
            return;
        }

        User user = getUserByEmail(email);
        Account account = getAccountByCardNumber(cardNumber);
        if (account.isNull() || !user.getAccounts().contains(account)) {
            putSimpleOutput("description", "Card not found", command, output);
            return;
        }

        Card card = account.getCard(cardNumber);
        if (card.getStatus().equals("frozen")) {
            Transaction transaction = TransactionFactory.createTransaction(command,
                                                             "cardBlocked",
                                                                "");
            user.addTransaction(transaction);
            return;
        }

        String senderCurrency = account.getCurrency();
        double exchangeRate = getExchangeRate(currency, senderCurrency);
        if (exchangeRate == -1) {
            return;
        }

        double convertedAmount = amount * exchangeRate;
        double convertedAmountPreCommission = convertedAmount;

        double exchangeRateRON = getExchangeRate(currency, "RON");
        double spentAmountInRON = amount * exchangeRateRON;
        String planType = user.getPlan();

        // if the account is bussines we get the plan from the owner
        if (account.getType().equals("business")) {
            User owner = getUserByEmail(((BusinessAccount) account).getOwner());
            System.out.println("Changing plan " + planType + " to " + owner.getPlan());
            planType = owner.getPlan();
        }
        convertedAmount = addCommission(convertedAmount, spentAmountInRON, planType);

        // if it is a business acount check the spending limit
        if (account.getType().equals("business")) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            // check if the user is the owner or a manager or an employee
            if (!businessAccount.hasUser(email)) {
                putSimpleOutput("description", "Card not found", command, output);
                return;
            } else if (businessAccount.getSpendingLimit() < convertedAmount && !businessAccount.getOwner().equals(email) && !businessAccount.getManagers().contains(email)) {
                Transaction transaction = TransactionFactory.createTransaction(command, "noFunds", "");
                user.addTransaction(transaction);
                return;
            }
        }

        if (account.getBalance() < convertedAmount) {
            Transaction transaction = TransactionFactory.createTransaction(command, "noFunds", "");
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

            user.addTransaction(transaction);
            user.addTransaction(transaction2);
            return;
        }
        // else the user has enough funds

        if (user.isNull() || user.getAccount(account.getAccountNumber()) == null) {
            return;
        }

        account.deductFunds(convertedAmount);

        command.setAmmount(convertedAmountPreCommission);
        command.setAccount(account.getAccountNumber());
        Transaction transaction = TransactionFactory.createTransaction(command,
                                                         "commerciantTransaction",
                                                            "");
        user.addTransaction(transaction);

        // adds the sale to the commerciant in the account
        Commerciant commerciantToAdd = account.findCommerciant(commerciantName);
        if (commerciantToAdd == null) { // if the commerciant is not found
            commerciantToAdd = findCommerciantByName(commerciantName);
            if (commerciantToAdd == null) {
                return;
            }
        }
        commerciantToAdd.addSale(amount);

        spentAmountInRON = amount * exchangeRateRON;
        System.out.println("Spent amount in RON: " + spentAmountInRON);
        double cashback = account.addCommerciantTransaction(commerciantToAdd , spentAmountInRON, user, command) * convertedAmountPreCommission;
        account.addFunds(cashback);
        System.out.println("Found cashback: " + cashback + " for: " + email + " and " + commerciantName + " time: " + timestamp + " percent: " + cashback / convertedAmountPreCommission * 100 + " strategy: " + commerciantToAdd.getCashbackStrategy().getClass().getSimpleName());

        if (account.getAccountNumber().equals("RO53POOB7122855990652257"))
            System.out.println("ACCOUNT!!!");

        if (card.isOneTime()) {
            // delete the card
            account.deleteCard(cardNumber);
            Transaction transactionDestroyed = TransactionFactory.createTransaction(command,
                                                                   "cardDestroyed",
                                                                      "");
            user.addTransaction(transactionDestroyed);

            // create a new one time card
            Card newCard = account.createCard(true);
            String newCardNumber = newCard.getCardNumber();
            command.setCardNumber(newCardNumber);
            Transaction transactionCreate = TransactionFactory.createTransaction(command,
                                                                   "createCard",
                                                                      "");
            user.addTransaction(transactionCreate);
        }

        // if it is a business account, add the transaction to the business account
        if (account.getType().equals("business")) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            BusinessAccountTransaction businessTransaction = new BusinessAccountTransaction(timestamp, email, commerciantName, convertedAmountPreCommission);
            businessAccount.addBusinessTransaction(businessTransaction);
        }
    }

    private void sendMoney(final CommandInput command, final ArrayNode output) {
        String senderIBAN = command.getAccount();
        double amount = command.getAmount();
        String receiverIBAN = command.getReceiver();
        int timestamp = command.getTimestamp();
        String email = command.getEmail();

        if (amount == 0) {
            return;
        }

        User senderUser = getUserByEmail(email);
        Account sender = getAccountByIBAN(senderIBAN);
        Account receiver = getAccountByIBAN(receiverIBAN);
        if (receiver.isNull()) {
            String alias = command.getReceiver();
            if (aliases.containsKey(alias)) { // check if an alias is used
                receiverIBAN = aliases.get(alias);
                receiver = getAccountByIBAN(receiverIBAN);
            } else {
                Commerciant commerciant = findCommerciantByAccount(receiverIBAN);
                if (commerciant == null) {
                    putSimpleOutput("description", "User not found", command, output);
                    return;
                }
                receiver = new NullAccount(); // if the receiver is a commerciant
                receiver.setCurrency(sender.getCurrency());
            }
        }

        String senderCurrency = sender.getCurrency();
        String receiverCurrency = receiver.getCurrency();
        double exchangeRate = getExchangeRate(senderCurrency, receiverCurrency);
        if (exchangeRate == -1) {
            return;
        }

        double exchangeRateRON = getExchangeRate(senderCurrency, "RON");
        double spentAmountInRON = amount * exchangeRateRON;
        double amountPreCommission = amount;
        String planType = senderUser.getPlan();

        // if the account is bussines we get the plan from the owner
        if (sender.getType().equals("business")) {
            User owner = getUserByEmail(((BusinessAccount) sender).getOwner());
            System.out.println("Changing plan " + planType + " to " + owner.getPlan());
            planType = owner.getPlan();
        }

        amount = addCommission(amount, spentAmountInRON, planType);

        // if it is a business acount check the spending limit
        if (sender.getType().equals("business")) {
            String senderEmail = command.getEmail();
            BusinessAccount businessAccount = (BusinessAccount) sender;
            if (businessAccount.getSpendingLimit() < amount && !businessAccount.getOwner().equals(senderEmail) && !businessAccount.getManagers().contains(senderEmail)) {
                Transaction transaction = TransactionFactory.createTransaction(command, "noFunds", "");
                User user = getUserByEmail(senderEmail);
                user.addTransaction(transaction);
                return;
            }
        }

        double convertedAmount = amountPreCommission * exchangeRate;
        if (sender.getBalance() < amount) { // checks if the sender has enough funds
            Transaction transaction = TransactionFactory.createTransaction(command, "noFunds", "");
            User user = getUserByEmail(email);
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

        getUserByEmail(email).addTransaction(senderTransaction);
        getUserByIBAN(receiverIBAN).addTransaction(receiverTransaction);

        Commerciant commerciant = findCommerciantByAccount(receiverIBAN);
        if (commerciant != null) {
            // add the sale to the commerciant in the account and add the cashback
            commerciant.addSale(convertedAmount);
            double cashback = sender.addCommerciantTransaction(commerciant, spentAmountInRON, senderUser, command) * amountPreCommission;
            System.out.println("Found cashback: " + cashback + " for: " + senderUser.getEmail() + " and " + commerciant.getName() + " time: " + timestamp + " on sendMoney");
            sender.addFunds(cashback);
        }

        if (sender.getAccountNumber().equals("RO53POOB7122855990652257"))
            System.out.println("ACCOUNT!!!");

        // if it is a business account, add the transaction to the business account
        if (sender.getType().equals("business")) {
            BusinessAccount businessAccount = (BusinessAccount) sender;
            String commerciantName = (commerciant == null) ? "" : commerciant.getName();
            BusinessAccountTransaction businessTransaction = new BusinessAccountTransaction(timestamp, senderUser.getEmail(), commerciantName, amountPreCommission);
            businessAccount.addBusinessTransaction(businessTransaction);
        }
    }

    private void setAlias(final CommandInput command, final ArrayNode output) {
        String alias = command.getAlias();
        String accountNumber = command.getAccount();
        aliases.put(alias, accountNumber);
    }

    private void rejectSplitPayment(CommandInput command, ArrayNode output) {
        String email = command.getEmail();
        int timestamp = command.getTimestamp();

        // goes through the split payment queue and find the first split payment with the given email
        User user = getUserByEmail(email);
        SplitPayment splitPaymentToRemove = null;
        for (SplitPayment splitPayment : splitPaymentQueue) {
            if (splitPayment.hasUser(user) && !splitPayment.hasAccepted(user) && !splitPayment.hasRejected(user)) {
                splitPayment.rejectAccount(email);
                if (!splitPayment.isRejected()) {
                    return;
                }
                // else the split payment is rejected
                splitPaymentQueue.remove(splitPayment);
                splitPaymentToRemove = splitPayment;
                break;
            }
        }

        if (splitPaymentToRemove == null) {
            putSimpleOutput("description", "User not found", command, output);
            return;
        }

        String currency = splitPaymentToRemove.getCurrency();
        double amount = splitPaymentToRemove.getAmount();
        List<String> accounts = splitPaymentToRemove.getAccounts();
        List<Double> amountsForAccounts = splitPaymentToRemove.getAmountsForAccounts();
        String type = splitPaymentToRemove.getSplitPaymentType();
        timestamp = splitPaymentToRemove.getTimestamp();
        CommandInput commandReject = new CommandInput(currency, amount, accounts, amountsForAccounts, type, timestamp);
        Transaction transaction = TransactionFactory.createTransaction(commandReject, "splitTransaction", "errorRejected");

        // add the transaction to all the users that have to pay
        for (String account : splitPaymentToRemove.getAccounts()) {
            User userToPay = getUserByIBAN(account);
            userToPay.addTransaction(transaction);
        }
    }

    private void acceptSplitPayment(CommandInput command, ArrayNode output) {
        String email = command.getEmail();
        int timestamp = command.getTimestamp();

        // goes through the split payment queue and find the first split payment with the given email
        User user = getUserByEmail(email);
        SplitPayment acceptedSplitPayment = null;
        for (SplitPayment splitPayment : splitPaymentQueue) {
            // also check if this user has already accepted the split payment
            if (splitPayment.hasUser(user) && !splitPayment.hasAccepted(user) && !splitPayment.hasRejected(user)) {
                splitPayment.acceptAccount(email);
                acceptedSplitPayment = splitPayment;
                if (splitPayment.isAccepted()) { // the split payment is accepted
                    splitPaymentQueue.remove(splitPayment);
                    executeSplitPayment(splitPayment, output);
                }
                break;
            }
        }

        if (acceptedSplitPayment == null) {
            putSimpleOutput("description", "User not found", command, output);
        }
    }

    private void executeSplitPayment(final SplitPayment splitPayment, final ArrayNode output) {
        double amount = splitPayment.getAmount();
        String currency = splitPayment.getCurrency();
        List<String> accounts = splitPayment.getAccounts();
        List<Double> amountsForAccounts = splitPayment.getAmountsForAccounts();
        String type = splitPayment.getSplitPaymentType();
        int timestamp = splitPayment.getTimestamp();

        if (type.equals("equal")) {
            // add the split amount to the list of amounts for accounts
            double splitAmount = amount / accounts.size();
            amountsForAccounts = new ArrayList<>();
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
                return;
            }
            accountList.add(account);

            double exchangeRate = getExchangeRate(currency, account.getCurrency());
            if (exchangeRate == -1) {
                return;
            }

            double splitAmount = amountsForAccounts.get(index);
            double convertedAmount = splitAmount * exchangeRate;
            if (account.getBalance() < convertedAmount && !insufficientFunds) { // checks if the account has enough funds
                insufficientFunds = true;
                insufficientAccount = accountNumber;
            }
            index++;
        }

        if (insufficientFunds) {
            for (Account account : accountList) {
                CommandInput command = new CommandInput(currency, amount, accounts, amountsForAccounts, type, timestamp);

                command.setAccount(insufficientAccount);
                Transaction transaction = TransactionFactory.createTransaction(command,
                        "splitTransaction",
                        "errorNoFunds");
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

            CommandInput command = new CommandInput(currency, amount, accounts, amountsForAccounts, type, timestamp);
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

    private void splitPayment(final CommandInput command, final ArrayNode output) {
        splitPaymentQueue.add(new SplitPayment(command));
    }

    private void addInterest(final CommandInput command, final ArrayNode output) {
        String accountNumber = command.getAccount();
        int timestamp = command.getTimestamp();

        Account account = getAccountByIBAN(accountNumber);
        if (account.isNull()) {
            return;
        }

        if (!account.getType().equals("savings")) {
            putSimpleOutput("description", "This is not a savings account", command, output);
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
            return;
        }

        if (!account.getType().equals("savings")) {
            putSimpleOutput("description", "This is not a savings account", command, output);
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
            putSimpleOutput("description", "Account not found", command, output);
            return;
        }

        User user = getUserByIBAN(accountNumber);
        if (user.isNull()) {
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
            putSimpleOutput("description", "Account not found", command, output);
            return;
        }

        if (account.getType().equals("savings")) {
            ObjectNode commandOutput = output.objectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode accountNode = commandOutput.putObject("output");
            accountNode.put("error", "This kind of report is not supported for a saving account");
            commandOutput.put("timestamp", command.getTimestamp());
            output.add(commandOutput);
            return;
        }

        User user = getUserByIBAN(accountNumber);
        if (user.isNull()) {
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
        String type = command.getType();

        if (type.equals("transaction")) {
            businessTransactionReport(command, output);
        } else if (type.equals("commerciant")) {
            businessCommerciantReport(command, output);
        }
    }

    private void businessTransactionReport(final CommandInput command, final ArrayNode output) {
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        String accountNumber = command.getAccount();
        int timestamp = command.getTimestamp();

        Account account = getAccountByIBAN(accountNumber);
        if (account.isNull()) {
            putSimpleOutput("description", "Account not found", command, output);
            return;
        } else if (!account.getType().equals("business")) {
            putSimpleOutput("description", "This is not a business account", command, output);
            return;
        }

        BusinessAccount businessAccount = (BusinessAccount) account;
        ObjectNode commandOutput = output.objectNode();
        commandOutput.put("command", command.getCommand());
        ObjectNode accountNode = commandOutput.putObject("output");
        accountNode.put("IBAN", account.getAccountNumber());
        accountNode.put("balance", account.getBalance());
        accountNode.put("currency", account.getCurrency());
        accountNode.put("spending limit", businessAccount.getSpendingLimit());
        accountNode.put("deposit limit", businessAccount.getDepositLimit());
        accountNode.put("statistics type", "transaction");
        ArrayNode managersArray = accountNode.putArray("managers");
        ArrayNode employeesArray = accountNode.putArray("employees");

        List<String> managers = businessAccount.getManagers();
        List<String> employees = businessAccount.getEmployees();

        double totalSpent = 0;
        double totalDeposited = 0;

        for (String manager : managers) {
            double spent = businessAccount.getUserSpentAmount(manager, startTimestamp, endTimestamp);
            double deposited = businessAccount.getUserDeposit(manager, startTimestamp, endTimestamp);
            totalSpent += spent;
            totalDeposited += deposited;

            ObjectNode managerNode = managersArray.objectNode();
            User user = getUserByEmail(manager);
            String name = user.getLastName() + " " + user.getFirstName();
            managerNode.put("username", name);
            managerNode.put("spent", spent);
            managerNode.put("deposited", deposited);
            managersArray.add(managerNode);
        }

        for (String employee : employees) {
            double spent = businessAccount.getUserSpentAmount(employee, startTimestamp, endTimestamp);
            double deposited = businessAccount.getUserDeposit(employee, startTimestamp, endTimestamp);
            totalSpent += spent;
            totalDeposited += deposited;

            ObjectNode employeeNode = employeesArray.objectNode();
            User user = getUserByEmail(employee);
            String name = user.getLastName() + " " + user.getFirstName();
            employeeNode.put("username", name);
            employeeNode.put("spent", spent);
            employeeNode.put("deposited", deposited);
            employeesArray.add(employeeNode);
        }

        accountNode.put("total spent", totalSpent);
        accountNode.put("total deposited", totalDeposited);

        commandOutput.put("timestamp", timestamp);
        output.add(commandOutput);
    }

    private void businessCommerciantReport(final CommandInput command, final ArrayNode output) {
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        String accountNumber = command.getAccount();
        int timestamp = command.getTimestamp();

        Account account = getAccountByIBAN(accountNumber);
        if (account.isNull()) {
            putSimpleOutput("description", "Account not found", command, output);
            return;
        } else if (!account.getType().equals("business")) {
            putSimpleOutput("description", "This is not a business account", command, output);
            return;
        }

        BusinessAccount businessAccount = (BusinessAccount) account;
        ObjectNode commandOutput = output.objectNode();
        commandOutput.put("command", command.getCommand());
        commandOutput.put("timestamp", timestamp);
        ObjectNode accountNode = commandOutput.putObject("output");
        accountNode.put("IBAN", account.getAccountNumber());
        accountNode.put("balance", account.getBalance());
        accountNode.put("currency", account.getCurrency());
        accountNode.put("spending limit", businessAccount.getSpendingLimit());
        accountNode.put("deposit limit", businessAccount.getDepositLimit());
        accountNode.put("statistics type", "commerciant");

        ArrayNode commerciantsArray = accountNode.putArray("commerciants");
        List<String> commerciantsNames = businessAccount.getAllPaidCommerciants(startTimestamp, endTimestamp);
        for (String commerciantName : commerciantsNames) {
            double amount = businessAccount.getCommerciantSpentAmount(commerciantName, startTimestamp, endTimestamp);
            ObjectNode commerciantNode = commerciantsArray.objectNode();
            commerciantNode.put("commerciant", commerciantName);
            commerciantNode.put("total received", amount);
            ArrayNode managersArray = commerciantNode.putArray("managers");
            ArrayNode employeesArray = commerciantNode.putArray("employees");

            List<String> managers = businessAccount.getManagersForCommerciant(commerciantName, startTimestamp, endTimestamp);
            List<String> employees = businessAccount.getEmployeesForCommerciant(commerciantName, startTimestamp, endTimestamp);

            for (String manager : managers) {
                User user = getUserByEmail(manager);
                String name = user.getLastName() + " " + user.getFirstName();
                managersArray.add(name);
            }

            for (String employee : employees) {
                User user = getUserByEmail(employee);
                String name = user.getLastName() + " " + user.getFirstName();
                employeesArray.add(name);
            }

            commerciantsArray.add(commerciantNode);
        }
        output.add(commandOutput);
    }

    private void putSimpleOutput(final String propertyName, final String description, final CommandInput command, final ArrayNode output) {
        ObjectNode commandOutput = output.objectNode();
        commandOutput.put("command", command.getCommand());
        ObjectNode accountNode = commandOutput.putObject("output");
        accountNode.put(propertyName, description);
        accountNode.put("timestamp", command.getTimestamp());
        commandOutput.put("timestamp", command.getTimestamp());
        output.add(commandOutput);
    }

    private double addCommission(final double amount, final double amountInRon, final String plan) {
        switch (plan) {
            case "standard":
                System.out.println("Adding commision : " + 0.002 * amount);
                return amount * 1.002;
            case "silver":
                if (amountInRon >= 500) {
                    System.out.println("Adding commision : " + 0.001 * amount);
                    return amount * 1.001;
                }
            default:
                break;
        }
        return amount;
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

    private Commerciant findCommerciantByName(final String name) {
        for (Commerciant commerciant : commerciants) {
            if (commerciant.getName().equals(name)) {
                return commerciant;
            }
        }
        return null;
    }

    private Commerciant findCommerciantByAccount(final String accountNumber) {
        for (Commerciant commerciant : commerciants) {
            if (commerciant.getAccount().equals(accountNumber)) {
                return commerciant;
            }
        }
        return null;
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
}
