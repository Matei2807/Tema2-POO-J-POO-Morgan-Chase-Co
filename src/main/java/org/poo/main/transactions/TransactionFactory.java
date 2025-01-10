package org.poo.main.transactions;

import org.poo.fileio.CommandInput;

public final class TransactionFactory {

    private TransactionFactory() { // should not be instantiated
    }

    /**
     * Create a transaction based on the command input
     * @param command the command input
     * @param transactionType the type of transaction
     * @param transferType the type of transfer
     * @return the transaction
     */
    public static Transaction createTransaction(final CommandInput command,
                                                final String transactionType,
                                                final String transferType) {
        switch (transactionType) {
            case "addAccount":
                return new NewAccountTransaction(command);
            case "noFunds":
                return new NoFundsTransaction(command);
            case "commerciantTransaction":
                return new CommerciantTransaction(command);
            case "normalTransaction":
                return new NormalTransaction(command, transferType);
            case "splitTransaction":
                return new SplitTransaction(command, transferType);
            case "createCard":
                return new CreateCardTransaction(command);
            case "createCardError":
                return new CreateCardErrorTransaction(command);
            case "deleteCard":
                return new DeleteCardTransaction(command);
            case "cardBlocked":
                return new CardBlockedTransaction(command);
            case "cardWillBeBlocked":
                return new CardWillBlockTransaction(command);
            case "deleteAccountError":
                return new DeleteAccountError(command);
            case "changeInterestRate":
                return new ChangeInterestTransaction(command);
            case "upgradePlan":
                return new UpgradePlanTransaction(command);
            case "withdrawSavingsAgeError":
                return new WithdrawSavingsAgeErrorTransaction(command);
            case "cashWithdrawal":
                return new CashWithdrawalTransaction(command);
            case "cardDestroyed":
                return new CardDestroyedTransaction(command);
            case "addInterest":
                return new AddInterestTransaction(command);
            case "noCurrentAccountError":
                return new NoCurrentAccountErrorTransaction(command);
            case "upgradePlanError":
                return new UpgradePlanErrorTransaction(command, transferType);
            case "withdrawSavings":
                return new WithdrawSavingsTransaction(command);
            default:
                return null;
        }
    }
}
