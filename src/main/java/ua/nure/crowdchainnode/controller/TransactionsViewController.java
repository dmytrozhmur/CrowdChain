package ua.nure.crowdchainnode.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Pagination;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import ua.nure.crowdchainnode.model.Block;
import ua.nure.crowdchainnode.model.Transaction;
import ua.nure.crowdchainnode.repository.BlockchainRepository;
import ua.nure.crowdchainnode.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.List;

public class TransactionsViewController {
    @FXML
    private Pagination blockPagination;

    @FXML
    private TableView<Transaction> transactionsTable;

    @FXML
    private TableColumn<Transaction, String> timestampColumn;

    @FXML
    private TableColumn<Transaction, Double> amountColumn;

    @FXML
    private TableColumn<Transaction, String> senderColumn;

    @FXML
    private TableColumn<Transaction, String> recipientColumn;

    @FXML
    private TableColumn<Transaction, String> typeColumn;

    // TODO: Replace with your actual blockchain data
    private BlockchainRepository blockchainRepository = BlockchainRepository.getInstance();
    private List<Block> blockchainData = new ArrayList<>();

    @FXML
    public void initialize() {
        setupBlockchainData();
        setupPagination();
        setupTableColumns();
    }

    private void setupBlockchainData() {
        blockchainData = blockchainRepository.findAll();
        addTestData();
    }

    private void addTestData() {
        Block block1 = new Block();
        block1.setLedgerId(1);
        block1.getTransactionLedger().add(new Transaction("Alice".getBytes(), "Bob".getBytes(), 10.0, ("sign" + Math.random()).getBytes(), 1, "2021-01-01 12:00:00", Transaction.Type.DONATE));
        Block block2 = new Block();
        block2.setLedgerId(2);
        block2.getTransactionLedger().add(new Transaction("Bob".getBytes(), "Alice".getBytes(), 5.0, ("sign" + Math.random()).getBytes(), 2, "2021-01-02 12:00:00", Transaction.Type.REFUND));
        blockchainData.add(block1);
        blockchainData.add(block2);
        TransactionRepository.getInstance().save(block1.getTransactionLedger().get(0));
        TransactionRepository.getInstance().save(block2.getTransactionLedger().get(0));
    }

    private void setupPagination() {
        blockPagination.setPageCount(blockchainData.size());
        blockPagination.setPageFactory(pageIndex -> {
                if (pageIndex >= 0 && pageIndex < blockchainData.size()) {
                    transactionsTable.getItems().setAll(blockchainData.get(pageIndex).getTransactionLedger());
                    return new VBox(new Label("Block " + (pageIndex + 1)), transactionsTable);
                } else {
                    return null;
                }
        });
    }

    private void setupTableColumns() {
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        senderColumn.setCellValueFactory(new PropertyValueFactory<>("fromFX"));
        recipientColumn.setCellValueFactory(new PropertyValueFactory<>("toFX"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeFX"));
    }
}