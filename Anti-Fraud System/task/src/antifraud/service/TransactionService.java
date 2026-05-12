package antifraud.service;

import antifraud.AntiFraudApplication;
import antifraud.api.dto.*;
import antifraud.api.exception.BadRequest;
import antifraud.api.exception.InvalidRequest;
import antifraud.api.exception.NotFound;
import antifraud.api.exception.UnProcessable;
import antifraud.model.FeedbackLimits;
import antifraud.model.StolenCard;
import antifraud.model.SuspiciousIP;
import antifraud.model.Transaction;
import antifraud.config.FraudDetectionConfig;
import antifraud.repository.FeedbackLimitsRepository;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.SuspiciousIPRepository;
import antifraud.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class TransactionService {

    private final SuspiciousIPRepository suspiciousIPRepository;
    private final TransactionRepository transactionRepository;
    private final StolenCardRepository stolenCardRepository;
    private final FeedbackLimitsRepository feedbackLimitsRepository;
    private final FraudDetectionConfig fraudConfig;

    public TransactionService(SuspiciousIPRepository suspiciousIPRepository, TransactionRepository transactionRepository, StolenCardRepository stolenCardRepository, FeedbackLimitsRepository feedbackLimitsRepository, FraudDetectionConfig fraudConfig) {
        this.suspiciousIPRepository = suspiciousIPRepository;
        this.transactionRepository = transactionRepository;
        this.stolenCardRepository = stolenCardRepository;
        this.feedbackLimitsRepository = feedbackLimitsRepository;
        this.fraudConfig = fraudConfig;
    }

    public ResponseEntity<SuspiciousIPResponse> saveIP(SuspiciousIPRequest suspiciousIPRequest) {
        InetAddressValidator inetAddressValidator = new InetAddressValidator();
        boolean ipPresent = suspiciousIPRepository.existsByIpIgnoreCase(suspiciousIPRequest.getIp());
        if (inetAddressValidator.isValid(suspiciousIPRequest.getIp())) {
            if (ipPresent) throw new InvalidRequest("IP already present");
            SuspiciousIP suspiciousIP = suspiciousIPRepository.save(new SuspiciousIP(suspiciousIPRequest.getIp()));
            SuspiciousIPResponse suspiciousIPResponse = SuspiciousIPResponse.builder().id(suspiciousIP.getId()).ip(suspiciousIP.getIp()).build();
            return new ResponseEntity<>(suspiciousIPResponse, HttpStatus.OK);
        } else {
            throw new BadRequest("IP not in correct format");
        }
    }

    public ResponseEntity<SuspiciousIPDelete> deleteIP(String ip) {
        log.debug("Delete: /suspicious-ip/{}", ip);
        InetAddressValidator inetAddressValidator = new InetAddressValidator();
        boolean ipPresent = suspiciousIPRepository.existsByIpIgnoreCase(ip);
        if (inetAddressValidator.isValid(ip)) {
            if (!ipPresent) throw new NotFound("IP not found!");
            suspiciousIPRepository.deleteByIpIgnoreCase(ip);
            SuspiciousIPDelete suspiciousIPDelete = SuspiciousIPDelete.builder().status("IP " + ip + " " + "successfully removed!").build();
            return new ResponseEntity<>(suspiciousIPDelete, HttpStatus.OK);
        } else {
            throw new BadRequest("IP not in correct format");
        }
    }

    public List<SuspiciousIPListResponse> listIps() {
        log.debug("Get: /suspicious-ip");
        Iterable<SuspiciousIP> ipsList = suspiciousIPRepository.findAll();
        List<SuspiciousIPListResponse> responseIPList = new ArrayList<>();

        for (SuspiciousIP ip : ipsList) {
            responseIPList.add(new SuspiciousIPListResponse(ip.getId(), ip.getIp()));
        }
        responseIPList.sort(Comparator.comparing(SuspiciousIPListResponse::getId));
        return responseIPList;
    }

    public ResponseEntity<StolenCardResponse> saveCard(StolenCardRequest stolenCardRequest) {
        log.debug("Post: /stolencard");
        boolean result = LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(stolenCardRequest.getNumber());

        boolean cardPresent = stolenCardRepository.existsByNumberIgnoreCase(stolenCardRequest.getNumber());
        if (cardPresent) throw new InvalidRequest("Card already exists");

        if (result) {
            StolenCard savedCard = stolenCardRepository.save(new StolenCard(stolenCardRequest.getNumber()));
            StolenCardResponse stolenCardResponse = StolenCardResponse.builder().id(savedCard.getId()).number(savedCard.getNumber()).build();
            return new ResponseEntity<>(stolenCardResponse, HttpStatus.OK);
        } else {
            throw new BadRequest("Invalid card number");
        }

    }

    public ResponseEntity<StolenCardDeleteResponse> deleteCard(String number) {
        log.debug("Delete: /stolencard/{}", number);
        boolean result = LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(number);
        boolean cardPresent = stolenCardRepository.existsByNumberIgnoreCase(number);

        if (result) {
            if (!cardPresent) throw new NotFound("Card not found!");
            stolenCardRepository.deleteByNumberIgnoreCase(number);
            StolenCardDeleteResponse stolenCardDeleteResponse = StolenCardDeleteResponse.builder().status("Card " + number + " successfully removed!").build();
            return new ResponseEntity<>(stolenCardDeleteResponse, HttpStatus.OK);
        } else {
            throw new BadRequest("Invalid card number");
        }
    }

    public List<StolenCardListResponse> listCards() {
        log.debug("Get: /stolencard");
        Iterable<StolenCard> cardList = stolenCardRepository.findAll();
        List<StolenCardListResponse> responseCardList = new ArrayList<>();

        for (StolenCard stolenCard : cardList) {
            responseCardList.add(new StolenCardListResponse(stolenCard.getId(), stolenCard.getNumber()));
        }
        responseCardList.sort(Comparator.comparing(StolenCardListResponse::getId));
        return responseCardList;
    }


    @Transactional
    public ResponseEntity<FraudTransactionResponse> postFraudTransaction(TransactionRequest transaction) {
        log.debug("Post: /transaction - {}", transaction);

        boolean recordExists = feedbackLimitsRepository.existsByNumberIgnoreCase(transaction.getNumber());

        if (!recordExists) {
            log.debug("Record does not exist in feedback limits table for card {}", transaction.getNumber());
            feedbackLimitsRepository.save(new FeedbackLimits(transaction.getNumber(), null, null, null, null, null));
        }

        InetAddressValidator inetAddressValidator = new InetAddressValidator();
        boolean stolenCardExists = stolenCardRepository.existsByNumberIgnoreCase(transaction.getNumber());
        boolean suspiciousIpExists = suspiciousIPRepository.existsByIpIgnoreCase(transaction.getIp());
        boolean validCard = LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(transaction.getNumber());
        boolean validIp = inetAddressValidator.isValid(transaction.getIp());

        log.debug("stolenCard={}, suspiciousIp={}, validCard={}, validIp={}", stolenCardExists, suspiciousIpExists, validCard, validIp);

        List<Transaction> validTransactions = listTransactions(transaction.getNumber(), transaction.getDate());

        boolean validRegionCorrelation;
        boolean validIpCorrelation;

        int noOfTransaction = validTransactions.size();
        log.debug("Transactions in last hour for card {}: {}", transaction.getNumber(), noOfTransaction);

        List<String> lastRegionList = new ArrayList<>();
        List<String> lastIpList = new ArrayList<>();

        long distinctValidRegions = 0;
        long distinctValidIP = 0;
        long distinctValidIpCount = 0;
        long distinctValidRegionCount = 0;
        if (noOfTransaction > 2) {

            for (Transaction validTransaction : validTransactions) {
                lastRegionList.add(String.valueOf(validTransaction.getRegion()));
                lastIpList.add(validTransaction.getIp());
            }

            log.debug("Region list: {}, IP list: {}", lastRegionList, lastIpList);

            distinctValidRegions = lastRegionList.stream().filter(region -> !Objects.equals(String.valueOf(transaction.getRegion()), region)).distinct().count();
            distinctValidIP = lastIpList.stream().filter(ip -> !Objects.equals(transaction.getIp(), ip)).distinct().count();

            distinctValidRegionCount = distinctValidRegions;
            distinctValidIpCount = distinctValidIP;

            log.debug("Distinct regions: {}, distinct IPs: {}", distinctValidRegionCount, distinctValidIpCount);

            validRegionCorrelation = distinctValidRegionCount < 2;
            validIpCorrelation = distinctValidIpCount < 2;

        } else {
            validRegionCorrelation = true;
            validIpCorrelation = true;
        }

        log.debug("validRegion={}, validIp={}", validRegionCorrelation, validIpCorrelation);
        FraudTransactionResponse finalTransResponse = getTransactionState(transaction, stolenCardExists, suspiciousIpExists, validCard, validIp, validRegionCorrelation, validIpCorrelation, distinctValidRegionCount, distinctValidIpCount);

        log.debug("Transaction result: {}", finalTransResponse.getResult());
        FeedbackLimits feedbackForNumber = feedbackLimitsRepository.findByNumberIgnoreCase(transaction.getNumber());

        Long maxAllowedAmount = feedbackForNumber.getMaxAllowedAmount();
        Long maxManualAmount = feedbackForNumber.getMaxManualAmount();
        Long allowedRange = feedbackForNumber.getAllowed();
        Long manualRange = feedbackForNumber.getManual();
        Long transactionAmount = transaction.getAmount();
        String number = transaction.getNumber();

        if (Objects.equals(finalTransResponse.getResult().toString(), "ALLOWED")) {
            if (allowedRange == null || transactionAmount > allowedRange) {
                feedbackLimitsRepository.updateAllowedByNumberIgnoreCase(transactionAmount, number);
            }
        } else if (Objects.equals(finalTransResponse.getResult().toString(), "MANUAL_PROCESSING")) {
            if (manualRange == null || transactionAmount > manualRange) {
                feedbackLimitsRepository.updateManualByNumberIgnoreCase(transactionAmount, number);
            }
        }

        Transaction saveTrans = new Transaction(transactionAmount, transaction.getIp(), transaction.getNumber(), String.valueOf(transaction.getRegion()), transaction.getDate(), String.valueOf(finalTransResponse.getResult()), "");
        log.debug("Saving transaction: {}", saveTrans);
        transactionRepository.save(saveTrans);

        return new ResponseEntity<>(finalTransResponse, HttpStatus.OK);
    }


    private FraudTransactionResponse getTransactionState(TransactionRequest transaction, boolean stolenCardExists, boolean suspiciousIpExists, boolean validCard, boolean validIp, boolean validRegionCorrelation, boolean validIpCorrelation, long distinctValidRegionCount, long distinctValidIpCount) {
        Long transactionAmount = transaction.getAmount();

        log.debug("getTransactionState: amount={}, regions={}, ips={}, stolen={}, suspiciousIp={}, validIpCorrelation={}, validRegionCorrelation={}",
                transactionAmount, distinctValidRegionCount, distinctValidIpCount, stolenCardExists, suspiciousIpExists, validIpCorrelation, validRegionCorrelation);

        String info = stolenCardExists ? suspiciousIpExists ? "card-number, " + "ip" : "card-number" : suspiciousIpExists ? "ip" : "";

        String info1 = !validIpCorrelation ? !validRegionCorrelation ? "ip" + "-correlation, " + "region-correlation" : "ip" + "-correlation" : !validRegionCorrelation ? "region" + "-correlation" : "";

        String newInfo = Objects.equals(info, "") ? info1 : Objects.equals(info1, "") ? info : info + ", " + info1;
        log.debug("Info: {}", newInfo);

        if (!validCard || !validIp || Objects.isNull(transactionAmount))
            throw new BadRequest("Invalid card or ip or transaction amount");

        boolean state = stolenCardExists || suspiciousIpExists;
        boolean suspiciousCorrelation = !validRegionCorrelation || !validIpCorrelation;

        AntiFraudApplication.TransactionState finalState;

        AntiFraudApplication.TransactionState prohibitedOrManualProcessingOrAllowed;

        if (distinctValidRegionCount >= 3 || distinctValidIpCount >= 3) {
            prohibitedOrManualProcessingOrAllowed = AntiFraudApplication.TransactionState.PROHIBITED;
        } else if (distinctValidRegionCount >= 2 || distinctValidIpCount >= 2) {
            prohibitedOrManualProcessingOrAllowed = AntiFraudApplication.TransactionState.MANUAL_PROCESSING;
        } else {
            prohibitedOrManualProcessingOrAllowed = AntiFraudApplication.TransactionState.ALLOWED;
        }


        FeedbackLimits feedbackLimits = feedbackLimitsRepository.findByNumberIgnoreCase(transaction.getNumber());
        Long maxAllowedRangeAmount = feedbackLimits.getAllowed() == null ? fraudConfig.getMaxAllowedAmount() : feedbackLimits.getAllowed();

        if (feedbackLimits.getAllowed() == null) {
            feedbackLimitsRepository.updateAllowedByNumberIgnoreCase(fraudConfig.getMaxAllowedAmount(), transaction.getNumber());
        }

        Long maxManualRangeAmount = feedbackLimits.getManual() == null ? fraudConfig.getMaxManualAmount() : feedbackLimits.getManual();

        if (feedbackLimits.getManual() == null) {
            feedbackLimitsRepository.updateManualByNumberIgnoreCase(fraudConfig.getMaxManualAmount(), feedbackLimits.getNumber());
        }

        String finalInfo = "";

        if (transactionAmount > 0 && transactionAmount <= maxAllowedRangeAmount) {
            finalState = state ? AntiFraudApplication.TransactionState.PROHIBITED : suspiciousCorrelation ? prohibitedOrManualProcessingOrAllowed : AntiFraudApplication.TransactionState.ALLOWED;
            return new FraudTransactionResponse(finalState, (finalState == AntiFraudApplication.TransactionState.ALLOWED) ? "none" : newInfo);
        } else if (transactionAmount > maxAllowedRangeAmount && transactionAmount <= maxManualRangeAmount) {
            finalState = state ? AntiFraudApplication.TransactionState.PROHIBITED : suspiciousCorrelation ? prohibitedOrManualProcessingOrAllowed : AntiFraudApplication.TransactionState.MANUAL_PROCESSING;
            finalInfo = (stolenCardExists || suspiciousIpExists) ? newInfo : "amount";
            return new FraudTransactionResponse(finalState, finalInfo);
        } else if (transactionAmount > maxManualRangeAmount) {
            finalState = AntiFraudApplication.TransactionState.PROHIBITED;
            finalInfo = (stolenCardExists || suspiciousIpExists) ? "amount, " + newInfo : "amount";
            return new FraudTransactionResponse(finalState, finalInfo);
        } else {
            throw new BadRequest("Invalid amount!");
        }
    }

    public List<Transaction> listTransactions(String number, LocalDateTime timeNow) {
        log.debug("Listing transactions in last hour for card {}", number);

        Iterable<Transaction> transactionList = transactionRepository.findAll();

        List<Transaction> testTransResponse = new ArrayList<>();

        transactionList.forEach(transaction -> {
            if (Objects.equals(transaction.getNumber(), number)) {
                long minDiff = Duration.between(transaction.getDate(), timeNow).toMinutes();
                if (minDiff >= 0 && minDiff <= fraudConfig.getCorrelationWindowMinutes()) {
                    testTransResponse.add(transaction);
                }
            }
        });

        testTransResponse.sort(Comparator.comparing(Transaction::getId).reversed());
        return testTransResponse;
    }

    public List<AllTransactionResponse> listAllCardTransactions() {
        log.debug("GET /history");
        List<Transaction> allCardTransactions;
        List<AllTransactionResponse> allTransactionResponses = new ArrayList<>();
        try {
            allCardTransactions = (List<Transaction>) transactionRepository.findAll();

            for (Transaction trans : allCardTransactions) {
                allTransactionResponses.add(new AllTransactionResponse(trans.getId(), trans.getAmount(), trans.getIp(), trans.getNumber(), trans.getRegion(), trans.getDate(), trans.getResult(), trans.getFeedback()));
            }


        } catch (Exception e) {
            log.error("Exception fetching all transactions", e);
            throw new UnProcessable("Issue fetching records from transaction table");
        }
        allTransactionResponses.sort(Comparator.comparing(AllTransactionResponse::getTransactionId));
        return allTransactionResponses;
    }

    public List<AllTransactionResponse> listCardTransactions(String number) {
        log.debug("GET /history/{}", number);
        List<Transaction> allCardTransactions;

        boolean validCard = LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(number);

        if (!validCard) throw new BadRequest("Invalid Card number");
        List<AllTransactionResponse> allTransactionResponses = new ArrayList<>();

        allCardTransactions = transactionRepository.findByNumber(number);

        if (allCardTransactions.isEmpty()) {
            throw new NotFound("No transactions associated with the card number");
        }

        try {
            for (Transaction trans : allCardTransactions) {
                allTransactionResponses.add(new AllTransactionResponse(trans.getId(), trans.getAmount(), trans.getIp(), trans.getNumber(), trans.getRegion(), trans.getDate(), trans.getResult(), trans.getFeedback()));
            }

        } catch (Exception e) {
            log.error("Exception pulling records from transactions table", e);
            throw new UnProcessable("Exception pulling records from transactions table");
        }
        allTransactionResponses.sort(Comparator.comparing(AllTransactionResponse::getTransactionId));
        return allTransactionResponses;
    }

    long adjustLimit(long defaultValue, Long currentLimit, Long transactionValue, boolean increase) {
        long current = currentLimit == null ? defaultValue : currentLimit;
        double result = increase
                ? Math.ceil(0.8 * current + 0.2 * transactionValue)
                : Math.ceil(0.8 * current - 0.2 * transactionValue);
        log.debug("adjustLimit: current={}, transactionValue={}, increase={}, result={}", current, transactionValue, increase, result);
        return (long) result;
    }

    @Transactional
    public AllTransactionResponse updateTransaction(UpdateTransactionRequest updateTransactionRequest) {
        log.debug("PUT /transaction - feedback={}", updateTransactionRequest.getFeedback());
        Optional<Transaction> fetchTransaction = transactionRepository.findById(updateTransactionRequest.getTransactionId());

        if (fetchTransaction.isEmpty())
            throw new NotFound("Transaction not found!");

        List<String> validFeedbacks = new ArrayList<>();
        for (AntiFraudApplication.TransactionState val : AntiFraudApplication.TransactionState.values()) {
            validFeedbacks.add(val.toString());
        }

        if (!validFeedbacks.contains(updateTransactionRequest.getFeedback()))
            throw new BadRequest("Incorrect feedback");

        fetchTransaction.ifPresent(transaction -> {
            if (!Objects.equals(transaction.getFeedback(), ""))
                throw new InvalidRequest("Feedback already exists!");
        });

        fetchTransaction.ifPresent(transaction -> {
            if (Objects.equals(transaction.getResult(), updateTransactionRequest.getFeedback()))
                throw new UnProcessable("Feedback same as result!");
        });

        String feedback = updateTransactionRequest.getFeedback();

        switch (feedback) {
            case "ALLOWED" -> {
                log.debug("Feedback: {}", feedback);
                fetchTransaction.ifPresent(transaction -> {
                    FeedbackLimits feedbackLimits = feedbackLimitsRepository.findByNumberIgnoreCase(transaction.getNumber());

                    Long transactionAmount = transaction.getAmount();
                    Long allowedAmount = feedbackLimits.getAllowed();
                    Long manualAmount = feedbackLimits.getManual();
                    Long maxAllowedAmount = feedbackLimits.getMaxAllowedAmount();
                    Long maxManualAmount = feedbackLimits.getMaxManualAmount();
                    String number = transaction.getNumber();

                    Long newMaxAllowed = maxAllowedAmount == null ? transactionAmount : transactionAmount > maxAllowedAmount ? transactionAmount : maxAllowedAmount;
                    Long newMaxManual = maxManualAmount == null ? transactionAmount : transactionAmount > maxManualAmount ? transactionAmount : maxManualAmount;

                    if (Objects.equals(transaction.getResult(), "MANUAL_PROCESSING")) {
                        long allowedRange = adjustLimit(fraudConfig.getMaxAllowedAmount(), allowedAmount, transactionAmount, true);
                        feedbackLimitsRepository.updateAllowedAndMaxAllowedAmountByNumberIgnoreCase(allowedRange, newMaxAllowed, number);

                    } else if (Objects.equals(transaction.getResult(), "PROHIBITED")) {
                        long allowedRange = adjustLimit(fraudConfig.getMaxAllowedAmount(), allowedAmount, transactionAmount, true);
                        long manualRange = adjustLimit(fraudConfig.getMaxManualAmount(), manualAmount, transactionAmount, true);
                        feedbackLimitsRepository.updateAllowedAndManualAndMaxAllowedAmountAndMaxManualAmountByNumberIgnoreCase(allowedRange, manualRange, newMaxAllowed, newMaxManual, number);

                    } else {
                        throw new UnProcessable("Result same as feedback");
                    }
                });
            }
            case "MANUAL_PROCESSING" -> {
                log.debug("Feedback: {}", feedback);
                fetchTransaction.ifPresent(transaction -> {
                    FeedbackLimits feedbackLimits = feedbackLimitsRepository.findByNumberIgnoreCase(transaction.getNumber());

                    Long transactionAmount = transaction.getAmount();
                    Long allowedAmount = feedbackLimits.getAllowed();
                    Long manualAmount = feedbackLimits.getManual();
                    Long maxAllowedAmount = feedbackLimits.getMaxAllowedAmount();
                    Long maxManualAmount = feedbackLimits.getMaxManualAmount();
                    String number = transaction.getNumber();

                    Long newMaxAllowed = maxAllowedAmount == null ? transactionAmount : transactionAmount < maxAllowedAmount ? transactionAmount : maxAllowedAmount;
                    Long newMaxManual = maxManualAmount == null ? transactionAmount : transactionAmount > maxManualAmount ? transactionAmount : maxManualAmount;

                    if (Objects.equals(transaction.getResult(), "ALLOWED")) {
                        long allowedRange = adjustLimit(fraudConfig.getMaxAllowedAmount(), allowedAmount, transactionAmount, false);
                        feedbackLimitsRepository.updateAllowedAndMaxAllowedAmountByNumberIgnoreCase(allowedRange, newMaxAllowed, number);

                    } else if (Objects.equals(transaction.getResult(), "PROHIBITED")) {
                        long manualRange = adjustLimit(fraudConfig.getMaxManualAmount(), manualAmount, transactionAmount, true);
                        feedbackLimitsRepository.updateManualAndMaxManualAmountByNumberIgnoreCase(manualRange, newMaxManual, number);

                    } else {
                        throw new UnProcessable("Result same as feedback");
                    }
                });
            }
            case "PROHIBITED" -> {
                log.debug("Feedback: {}", feedback);
                fetchTransaction.ifPresent(transaction -> {
                    FeedbackLimits feedbackLimits = feedbackLimitsRepository.findByNumberIgnoreCase(transaction.getNumber());

                    Long transactionAmount = transaction.getAmount();
                    Long allowedAmount = feedbackLimits.getAllowed();
                    Long manualAmount = feedbackLimits.getManual();
                    Long maxAllowedAmount = feedbackLimits.getMaxAllowedAmount();
                    Long maxManualAmount = feedbackLimits.getMaxManualAmount();
                    String number = transaction.getNumber();

                    Long newMaxAllowed = maxAllowedAmount == null ? transactionAmount : transactionAmount < maxAllowedAmount ? transactionAmount : maxAllowedAmount;
                    Long newMaxManual = maxManualAmount == null ? transactionAmount : transactionAmount < maxManualAmount ? transactionAmount : maxManualAmount;

                    if (Objects.equals(transaction.getResult(), "ALLOWED")) {
                        long allowedRange = adjustLimit(fraudConfig.getMaxAllowedAmount(), allowedAmount, transactionAmount, false);
                        long manualRange = adjustLimit(fraudConfig.getMaxManualAmount(), manualAmount, transactionAmount, false);
                        feedbackLimitsRepository.updateAllowedAndManualAndMaxAllowedAmountAndMaxManualAmountByNumberIgnoreCase(allowedRange, manualRange, newMaxAllowed, newMaxManual, number);

                    } else if (Objects.equals(transaction.getResult(), "MANUAL_PROCESSING")) {
                        long manualRange = adjustLimit(fraudConfig.getMaxManualAmount(), manualAmount, transactionAmount, false);
                        feedbackLimitsRepository.updateManualAndMaxManualAmountByNumberIgnoreCase(manualRange, maxManualAmount, number);

                    } else {
                        throw new UnProcessable("Result same as feedback");
                    }
                });
            }
        }

        log.debug("Updating feedback for transaction {}", updateTransactionRequest.getTransactionId());
        transactionRepository.updateFeedbackById(updateTransactionRequest.getFeedback(), updateTransactionRequest.getTransactionId());

        Transaction resultTransaction = transactionRepository.findById(updateTransactionRequest.getTransactionId())
                .orElseThrow(() -> new NotFound("Transaction not found after update"));

        return new AllTransactionResponse(resultTransaction.getId(), resultTransaction.getAmount(), resultTransaction.getIp(), resultTransaction.getNumber(), resultTransaction.getRegion(), resultTransaction.getDate(), resultTransaction.getResult(), resultTransaction.getFeedback());
    }


}
