package antifraud.controller;

import antifraud.api.dto.*;
import antifraud.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


@RestController
@RequestMapping("/api/antifraud")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/history/{number}")
    public ResponseEntity<List<AllTransactionResponse>> listCardTransactions(@PathVariable String number) {
        return new ResponseEntity<>(transactionService.listCardTransactions(number), HttpStatus.OK);

    }

    @GetMapping("/history")
    public ResponseEntity<List<AllTransactionResponse>> listAllCardTransactions() {
        return new ResponseEntity<>(transactionService.listAllCardTransactions(), HttpStatus.OK);
    }

    @PostMapping("/transaction")
    public ResponseEntity<FraudTransactionResponse> postTransaction(@NotNull @Valid @RequestBody TransactionRequest transaction) {
        return transactionService.postFraudTransaction(transaction);
    }

    @PutMapping("/transaction")
    public ResponseEntity<AllTransactionResponse> updateTransaction(@RequestBody UpdateTransactionRequest updateTransactionRequest) {
        AllTransactionResponse updatedTransactionResponse = transactionService.updateTransaction(updateTransactionRequest);
        return new ResponseEntity<>(updatedTransactionResponse, HttpStatus.OK);
    }

    @PostMapping("/suspicious-ip")
    public ResponseEntity<SuspiciousIPResponse> saveIP(@RequestBody SuspiciousIPRequest suspiciousIPRequest) {
        return transactionService.saveIP(suspiciousIPRequest);
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    public ResponseEntity<SuspiciousIPDelete> deleteIP(@PathVariable String ip) {
        return transactionService.deleteIP(ip);
    }

    @GetMapping("/suspicious-ip")
    public ResponseEntity<List<SuspiciousIPListResponse>> listIPs() {
        return new ResponseEntity<>(transactionService.listIps(), HttpStatus.OK);
    }

    @PostMapping("/stolencard")
    public ResponseEntity<StolenCardResponse> saveCard(@RequestBody StolenCardRequest stolenCardRequest) {
        return transactionService.saveCard(stolenCardRequest);
    }

    @DeleteMapping("/stolencard/{number}")
    public ResponseEntity<StolenCardDeleteResponse> deleteCard(@PathVariable String number) {
        return transactionService.deleteCard(number);
    }

    @GetMapping("/stolencard")
    public ResponseEntity<List<StolenCardListResponse>> listCards() {
        return new ResponseEntity<>(transactionService.listCards(), HttpStatus.OK);
    }
}
