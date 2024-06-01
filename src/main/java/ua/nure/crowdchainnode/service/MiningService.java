package ua.nure.crowdchainnode.service;

import ua.nure.crowdchainnode.model.Block;
import ua.nure.crowdchainnode.model.Transaction;
import ua.nure.crowdchainnode.model.Wallet;
import ua.nure.crowdchainnode.repository.BlockchainRepository;
import ua.nure.crowdchainnode.repository.WalletRepository;
import ua.nure.crowdchainnode.util.Crypto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class MiningService {
    private static final MiningService instance;
    private ValidationService validationService;
    private WalletRepository walletRepository;
    private BlockchainRepository blockchainRepository;
    private AtomicBoolean stopped = new AtomicBoolean(false);

    static {
        instance = new MiningService();
    }

    private MiningService() {
        validationService = ValidationService.getInstance();
        walletRepository = WalletRepository.getInstance();
    }

    public static MiningService getInstance() {
        return instance;
    }

    public void stop() {
        stopped.set(true);
    }

    public void start() {
        stopped.set(false);
    }

    public Block mineBlock(ArrayList<Transaction> newBlockTransactions) {
        try {
            Wallet minersWallet = walletRepository.getWallet();
            LinkedList<Block> currentBC = blockchainRepository.findAll();
            Optional<Block> latestBlock = currentBC.isEmpty()
                    ? Optional.empty()
                    : Optional.ofNullable(currentBC.getLast());
            Optional<Block> latestPrevBlock = currentBC.size() < 2
                    ? Optional.empty()
                    : Optional.ofNullable(currentBC.get(currentBC.size() - 2));

            while (!stopped.get()) {
                Block newBlock = new Block(currentBC);
                latestBlock.ifPresentOrElse(
                        (block) -> newBlock.setPrevHash(block.getCurrHash()),
                        () -> newBlock.setPrevHash(new byte[]{0}));
                newBlock.setTransactionLedger(new ArrayList<>(newBlockTransactions));
                newBlock.setTimeStamp(LocalDateTime.now().toString());
                newBlock.setMinedBy(minersWallet.getPublicKey().getEncoded());
                newBlock.setCurrHash(new Crypto()
                        .applySHA256(newBlock.toString(), minersWallet.getPrivateKey()));

                if (validationService.isProved(newBlock, latestBlock, latestPrevBlock)) {
                    currentBC.add(newBlock);
                    newBlockTransactions.clear();

                    return newBlock;
                }
            }
        } catch (Exception e) {
            System.err.println("Problem with mining: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean isStopped() {
        return stopped.get();
    }
}
