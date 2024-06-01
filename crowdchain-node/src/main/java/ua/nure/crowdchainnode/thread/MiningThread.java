package ua.nure.crowdchainnode.thread;

import ua.nure.crowdchainnode.model.Block;
import ua.nure.crowdchainnode.model.Transaction;
import ua.nure.crowdchainnode.repository.BlockchainRepository;
import ua.nure.crowdchainnode.repository.TransactionRepository;
import ua.nure.crowdchainnode.repository.WalletRepository;
import ua.nure.crowdchainnode.service.MiningService;
import ua.nure.crowdchainnode.util.RequestHandler;

import java.util.ArrayList;

public class MiningThread extends Thread {
    private MiningService miningService = MiningService.getInstance();
    private RequestHandler requestHandler = RequestHandler.getInstance();
    private WalletRepository walletRepository = WalletRepository.getInstance();
    private BlockchainRepository blockchainRepository = BlockchainRepository.getInstance();
    private TransactionRepository transactionRepository = TransactionRepository.getInstance();

    @Override
    public void run() {
        while (true) {
            ArrayList<Transaction> transactionsPool = requestHandler.fetchTransactionsPool();
            ArrayList<Transaction> existingTransactions = transactionRepository.findAll();
            transactionsPool.removeAll(existingTransactions);
            if (!transactionsPool.isEmpty()) {
                miningService.start();
                Block minedBlock = miningService.mineBlock(transactionsPool);
                if (minedBlock != null) {
                    requestHandler.notifyServer(minedBlock);
                    new Thread(() -> requestHandler.notifyNodes(minedBlock)).start();
                    blockchainRepository.save(minedBlock);
                    walletRepository.updateBalance(
                            walletRepository.getWallet().getBalance() +
                            minedBlock.getTransactionLedger().stream()
                                    .mapToDouble(Transaction::getAmount).sum());
                }
            } else {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
