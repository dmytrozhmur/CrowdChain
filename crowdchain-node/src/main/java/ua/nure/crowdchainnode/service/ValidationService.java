package ua.nure.crowdchainnode.service;

import ua.nure.crowdchainnode.model.Block;
import ua.nure.crowdchainnode.util.Crypto;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;

import static ua.nure.crowdchainnode.util.Constant.*;

public class ValidationService {
    private static final ValidationService instance;

    static {
        instance = new ValidationService();
    }

    private ValidationService() {}

    public static ValidationService getInstance() {
        return instance;
    }

    public boolean isProved(LinkedList<Block> blockChain) {
        try {
            for (int i = 0; i < blockChain.size(); i++) {
                Optional<Block> prevBlock = i > 0 ? Optional.of(blockChain.get(i - 1)) : Optional.empty();
                Optional<Block> prevPrevBlock = i > 1 ? Optional.of(blockChain.get(i - 2)) : Optional.empty();
                if (!isProved(blockChain.get(i), prevBlock, prevPrevBlock)) {
                    return false;
                }
            }
        }
        catch (Exception e) {
            System.err.println("Problem with blockchain validation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean isProved(Block block, Optional<Block> prevBlock, Optional<Block> prevPrevBlock) {
        try {
            Crypto crypto = new Crypto();
            if (!Arrays.equals(
                    prevBlock.map(Block::getCurrHash).orElse(new byte[]{0}),
                    block.getPrevHash()) || !crypto.isVerified(block)) {
                return false;
            }

            if (prevBlock.isPresent() && prevPrevBlock.isPresent()) {
                long difficulty =
                    LocalDateTime.parse(prevBlock.get().getTimeStamp()).getSecond() -
                    LocalDateTime.parse(prevPrevBlock.get().getTimeStamp()).getSecond();
                if (difficulty > MAX_MINING_DIFFICULTY
                        && getLeadingZerosLength(block) - getLeadingZerosLength(prevBlock.get()) == 1) {
                    return true;
                } else if (difficulty < MIN_MINING_DIFFICULTY
                        && getLeadingZerosLength(block) - getLeadingZerosLength(prevBlock.get()) == 1) {
                    return true;
                } else  {
                    return MIN_MINING_DIFFICULTY <= difficulty && difficulty <= MAX_MINING_DIFFICULTY
                            && getLeadingZerosLength(block) == getLeadingZerosLength(prevBlock.get());
                }
            } else {
                return getLeadingZerosLength(block) == START_ZEROS_QUANTITY;
            }
        } catch (Exception exc) {
            System.err.println("Problem with block validation: " + exc.getMessage());
            exc.printStackTrace();
            return false;
        }
    }

    public LinkedList<Block> compareChains(LinkedList<Block> firstChain, LinkedList<Block> secondChain) {
        if (firstChain.size() < secondChain.size() && isProved(secondChain)) {
            System.out.println("Second chain won");
            return secondChain;
        } else if (isProved(firstChain)) {
            System.out.println("First chain won");
            return firstChain;
        } else {
            System.out.println("Both chains are invalid");
            return new LinkedList<>();
        }
    }

    public int getLeadingZerosLength(Block block) throws NoSuchAlgorithmException {
        Crypto crypto = new Crypto();
        String hexHash = crypto.byteArrayToHexString(block.getCurrHash());
        int zeros = 0;
        for (int i = 0; i < hexHash.length(); i++) {
            if (hexHash.charAt(i) == '0') {
                zeros++;
            } else {
                break;
            }
        }
        return zeros;
    }
}
