package ua.nure.blockchainservice.thread;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class MiningThread extends Thread {
    @Override
    public void run() {
        while (true) {
            long lastMinedBlock = LocalDateTime.parse(
                    BlockhainData.getInstance()
                            .getCurrentBlockChain().getLast()
                            .getTimeStamp()
                    ).toEpochSecond(ZoneOffset.UTC);
            long now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

            if ((lastMinedBlock + BlockchainData.getTimeoutInterval()) < now) {
                System.out.println("Blockchain is too old for mining! Update from peers");
            } else if (0 < (lastMinedBlock + BlockchainData.getMiningInterval()) - now) {
                System.out.printf("Blockchain is current, mining will commence in %d second\n",
                        ((lastMinedBlock + BlockchainData.getMiningInterval()) - now));
            } else {
                System.out.println("MINING NEW BLOCK");
                BlockchainData.getInstance.mineBlock();
                System.out.println(BlockchainData.getInstance().getWalletBallanceFX());
            }

            System.out.println(LocalDateTime.parse(BlockchainData.getInstance()
                    .getCurrentBlockchain().getLast().getTimeStamp().toEpochSecond(ZoneOffset.UTC)));
            try {
                Thread.sleep(2000);
                if (BlockchainData.getInstance().isExit()) {
                    break;
                }
                BlockchainData.getInstance()
                        .setMiningPoints(BlockchainData.getInstance().getMiningPoints() + 2);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
}
