package org.poo.main;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.fileio.ExchangeInput;
import org.poo.fileio.ObjectInput;
import org.poo.fileio.UserInput;
import org.poo.main.Accounts.Account;
import org.poo.main.Accounts.CurrentAccount;
import org.poo.main.Accounts.NullAccount;
import org.poo.main.Accounts.SavingsAccount;
import org.poo.main.Transactions.CommerciantTransaction;
import org.poo.main.Transactions.Transaction;
import org.poo.main.Transactions.TransactionFactory;
import org.poo.main.Users.NullUser;
import org.poo.main.Users.User;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Comparator;

public final class Bank {
    private List<User> users = new ArrayList<>();
    private List<ExchangeRate> exchangeRates = new ArrayList<>();
    private Map<String, String> aliases = new HashMap<>(); // map alias -> accountNumber

    public Bank(final ObjectInput inputData) {
        for (UserInput userInput : inputData.getUsers()) {
            users.add(new User(userInput));
        }
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
                default:
                    System.out.println("Invalid command: " + command.getCommand());
                    break;
            }
        }
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
            newAccount = new SavingsAccount(command);
        } else {
            newAccount = new CurrentAccount(command);
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

        Account account = getAccountByIBAN(accountNumber);
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
        String cardNumber = card.getCardNumber();
        command.setCardNumber(cardNumber);

        Transaction transaction = TransactionFactory.createTransaction(command, "createCard", "");
        user.addTransaction(transaction);
    }

    private void deleteAccount(final CommandInput command, final ArrayNode output) {
        String accountNumber = command.getAccount();
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

        User user = getUserByIBAN(accountNumber);
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

        Account account = getAccountByCardNumber(cardNumber);
        User user = getUserByIBAN(account.getAccountNumber());

        command.setAccount(account.getAccountNumber());
        command.setEmail(user.getEmail());
        Transaction transaction = TransactionFactory.createTransaction(command, "deleteCard", "");
        user.addTransaction(transaction);

        account.deleteCard(cardNumber);
    }

    private void setMinBalance(final CommandInput command, final ArrayNode output) {
        double minBalance = command.getAmount();
        String accountNumber = command.getAccount();
        int timestamp = command.getTimestamp();

        Account account = getAccountByIBAN(accountNumber);

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

        User userByEmail = getUserByEmail(email);
        if (userByEmail.isNull() || userByEmail.getAccount(account.getAccountNumber()) == null) {
            System.out.println("User not found for email: " + email);
            return;
        }

        account.deductFunds(convertedAmount);

        command.setAmmount(convertedAmount);
        command.setAccount(account.getAccountNumber());
        Transaction transaction = TransactionFactory.createTransaction(command,
                                                         "commerciantTransaction",
                                                            "");
        userByEmail.addTransaction(transaction);

        // adds new commerciant to the account
        if (account.findCommerciant(commerciantName) == null) {
            Commerciant commerciant = new Commerciant(commerciantName);
            commerciant.addSale(convertedAmount);
            account.addCommerciant(commerciant);
        } else { // adds the sale to the existing commerciant
            account.findCommerciant(commerciantName).addSale(convertedAmount);
        }

        if (card.isOneTime()) {
            // delete the card
            command.setAccount(account.getAccountNumber());
            command.setEmail(email);
            Transaction transactionDelete = TransactionFactory.createTransaction(command,
                                                                   "deleteCard",
                                                                      "");
            userByEmail.addTransaction(transactionDelete);
            account.deleteCard(cardNumber);

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

        double convertedAmount = amount * exchangeRate;
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
        aliases.put(alias, accountNumber);
    }

    private void splitPayment(final CommandInput command, final ArrayNode output) {
        List<String> accounts = command.getAccounts();
        String currency = command.getCurrency();
        double amount = command.getAmount();
        double splitAmount = amount / accounts.size();
        List<Account> accountList = new ArrayList<>();
        boolean insufficientFunds = false;
        String insufficientAccount = null;

        // go through all the accounts and check if they have enough funds
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
            double convertedAmount = splitAmount * exchangeRate;
            if (account.getBalance() < convertedAmount) { // checks if the account has enough funds
                insufficientFunds = true;
                insufficientAccount = accountNumber;
            }
        }

        if (insufficientFunds) {
            for (Account account : accountList) {
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

        for (Account account : accountList) {
            double exchangeRate = getExchangeRate(currency, account.getCurrency());
            account.deductFunds(splitAmount * exchangeRate);

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
        }
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

        account.addInterest();
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
            if (account != null) {
                return user;
            }
        }
        return new NullUser();
    }

    private Account getAccountByIBAN(final String iban) {
        for (User user : users) {
            Account account = user.getAccount(iban);
            if (account != null) {
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
}
