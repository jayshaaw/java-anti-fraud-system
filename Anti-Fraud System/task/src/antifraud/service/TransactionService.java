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
import antifraud.repository.FeedbackLimitsRepository;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.SuspiciousTransactionRepository;
import antifraud.repository.TransactionRepository;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TransactionService {

    private final SuspiciousTransactionRepository suspiciousTransactionRepository;

    private final TransactionRepository transactionRepository;

    private final StolenCardRepository stolenCardRepository;
    private final FeedbackLimitsRepository feedbackLimitsRepository;

    private final Long DEFAULT_MAX_ALLOWED_VALUE = 200L;

    private final Long DEFAULT_MAX_MANUAL_VALUE = 1500L;


    public TransactionService(SuspiciousTransactionRepository suspiciousTransactionRepository, TransactionRepository transactionRepository, StolenCardRepository stolenCardRepository, FeedbackLimitsRepository feedbackLimitsRepository) {
        this.suspiciousTransactionRepository = suspiciousTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.stolenCardRepository = stolenCardRepository;
        this.feedbackLimitsRepository = feedbackLimitsRepository;
    }

    public ResponseEntity<SuspiciousIPResponse> saveIP(SuspiciousIPRequest suspiciousIPRequest) {
        InetAddressValidator inetAddressValidator = new InetAddressValidator();
        boolean ipPresent = suspiciousTransactionRepository.existsByIpIgnoreCase(suspiciousIPRequest.getIp());
        if (inetAddressValidator.isValid(suspiciousIPRequest.getIp())) {
            if (ipPresent) throw new InvalidRequest("IP already present");
            SuspiciousIP suspiciousIP = suspiciousTransactionRepository.save(new SuspiciousIP(suspiciousIPRequest.getIp()));
            SuspiciousIPResponse suspiciousIPResponse = SuspiciousIPResponse.builder().id(suspiciousIP.getId()).ip(suspiciousIP.getIp()).build();
            return new ResponseEntity<>(suspiciousIPResponse, HttpStatus.OK);
        } else {
            throw new BadRequest("IP not in correct format");
        }
    }

    public ResponseEntity<SuspiciousIPDelete> deleteIP(String ip) {
        System.out.println("Delete: /suspicious-ip/{ip}");
        InetAddressValidator inetAddressValidator = new InetAddressValidator();
        boolean ipPresent = suspiciousTransactionRepository.existsByIpIgnoreCase(ip);
        if (inetAddressValidator.isValid(ip)) {
            if (!ipPresent) throw new NotFound("IP not found!");
            suspiciousTransactionRepository.deleteByIpIgnoreCase(ip);
            SuspiciousIPDelete suspiciousIPDelete = SuspiciousIPDelete.builder().status("IP " + ip + " " + "successfully removed!").build();
            return new ResponseEntity<>(suspiciousIPDelete, HttpStatus.OK);
        } else {
            throw new BadRequest("IP not in correct format");
        }
    }

    public List<SuspiciousIPListResponse> listIps() {
        System.out.println("Get: /suspicious-ip");
        Iterable<SuspiciousIP> ipsList = suspiciousTransactionRepository.findAll();
        List<SuspiciousIPListResponse> responseIPList = new ArrayList<>();

        for (SuspiciousIP ip : ipsList) {
            responseIPList.add(new SuspiciousIPListResponse(ip.getId(), ip.getIp()));
        }
        responseIPList.sort(Comparator.comparing(SuspiciousIPListResponse::getId));
        return responseIPList;
    }

    public ResponseEntity<StolenCardResponse> saveCard(StolenCardRequest stolenCardRequest) {
        System.out.println("Post: /stolencard");
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
        System.out.println("Delete: /stolencard/{number}");
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
        System.out.println("Get: /stolencard");
        Iterable<StolenCard> cardList = stolenCardRepository.findAll();
        List<StolenCardListResponse> responseCardList = new ArrayList<>();

        for (StolenCard stolenCard : cardList) {
            responseCardList.add(new StolenCardListResponse(stolenCard.getId(), stolenCard.getNumber()));
        }
        responseCardList.sort(Comparator.comparing(StolenCardListResponse::getId));
        return responseCardList;
    }


    public ResponseEntity<FraudTransactionResponse> postFraudTransaction(TransactionRequest transaction) {
        System.out.println("Post: /transaction");

        boolean recordExists = feedbackLimitsRepository.existsByNumberIgnoreCase(transaction.getNumber());

        if (!recordExists) {
            System.out.println("Record does not exists in feedback limits " + "table");
            feedbackLimitsRepository.save(new FeedbackLimits(transaction.getNumber(), null, null, null, null, null));
        }

        System.out.println("*** Before Feedback result: " + feedbackLimitsRepository.findByNumberIgnoreCase(transaction.getNumber()));
        InetAddressValidator inetAddressValidator = new InetAddressValidator();
        boolean stolenCardExists = stolenCardRepository.existsByNumberIgnoreCase(transaction.getNumber());
        System.out.println("Stolen card: " + stolenCardExists);
        boolean suspiciousIpExists = suspiciousTransactionRepository.existsByIpIgnoreCase(transaction.getIp());
        System.out.println("Suspicious IP: " + suspiciousIpExists);
        boolean validCard = LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(transaction.getNumber());
        boolean validIp = inetAddressValidator.isValid(transaction.getIp());

        System.out.println("Transaction posted: " + transaction);

        List<Transaction> validTransactions = listTransactions(transaction.getNumber(), transaction.getDate());

        boolean validRegionCorrelation;
        boolean validIpCorrelation;

        int noOfTransaction = validTransactions.size();
        System.out.println("No of transaction: " + noOfTransaction);

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

            System.out.println("Region list: " + Arrays.toString(lastRegionList.toArray()));
            System.out.println("Ip List: " + Arrays.toString(lastIpList.toArray()));

            distinctValidRegions = lastRegionList.stream().filter(region -> !Objects.equals(String.valueOf(transaction.getRegion()), region)).distinct().count();
            distinctValidIP = lastIpList.stream().filter(ip -> !Objects.equals(transaction.getIp(), ip)).distinct().count();

            distinctValidRegionCount = distinctValidRegions;
            distinctValidIpCount = distinctValidIP;

            System.out.println("Count distinct regions: " + distinctValidRegionCount);
            System.out.println("Count distinct ip: " + distinctValidIpCount);

            validRegionCorrelation = distinctValidRegionCount < 2;
            validIpCorrelation = distinctValidIpCount < 2;

        } else {
            validRegionCorrelation = true;
            validIpCorrelation = true;
        }

        System.out.println("Valid Region: " + validRegionCorrelation + "; " + "Valid IP: " + validIpCorrelation);
        FraudTransactionResponse finalTransResponse = getTransactionState(transaction, stolenCardExists, suspiciousIpExists, validCard, validIp, validRegionCorrelation, validIpCorrelation, distinctValidRegionCount, distinctValidIpCount);

        System.out.println("Transaction is: " + finalTransResponse.getResult());
        FeedbackLimits feedbackForNumber = feedbackLimitsRepository.findByNumberIgnoreCase(transaction.getNumber());

        System.out.println("*** Feedback is: " + feedbackForNumber);
        Long finalAmount;

        Long maxAllowedAmount = feedbackForNumber.getMaxAllowedAmount();
        Long maxManualAmount = feedbackForNumber.getMaxManualAmount();
        Long allowedRange = feedbackForNumber.getAllowed();
        Long manualRange = feedbackForNumber.getManual();
        Long transactionAmount = transaction.getAmount();
        String number = transaction.getNumber();

        if (Objects.equals(finalTransResponse.getResult().toString(), "ALLOWED")) {
            finalAmount = transactionAmount;
//                    maxAllowedAmount == null ? transactionAmount : maxAllowedAmount;

            if (allowedRange == null || transactionAmount > allowedRange) {
                feedbackLimitsRepository.updateAllowedByNumberIgnoreCase(transactionAmount, number);
            }
        } else if (Objects.equals(finalTransResponse.getResult().toString(), "MANUAL_PROCESSING")) {
            finalAmount = transactionAmount;
//                    maxManualAmount == null ? transactionAmount : maxManualAmount;

            if (manualRange == null || transactionAmount > manualRange) {
                feedbackLimitsRepository.updateManualByNumberIgnoreCase(transactionAmount, number);
            }
        } else {
            finalAmount = transaction.getAmount();
        }

        Transaction saveTrans = new Transaction(finalAmount, transaction.getIp(), transaction.getNumber(), String.valueOf(transaction.getRegion()), transaction.getDate(), String.valueOf(finalTransResponse.getResult()), "");
        System.out.println("Saving to Transaction: " + saveTrans);
        transactionRepository.save(saveTrans);

        return new ResponseEntity<>(finalTransResponse, HttpStatus.OK);
    }


    private FraudTransactionResponse getTransactionState(TransactionRequest transaction, boolean stolenCardExists, boolean suspiciousIpExists, boolean validCard, boolean validIp, boolean validRegionCorrelation, boolean validIpCorrelation, long distinctValidRegionCount, long distinctValidIpCount) {
        Long transactionAmount = transaction.getAmount();

        System.out.println("From: postFraudTransaction");
        System.out.println("Transaction Amount: " + transactionAmount);
        System.out.println("distinct Valid Region: " + distinctValidRegionCount);
        System.out.println("distinct Valid Ip: " + distinctValidIpCount);
        System.out.println("Stolen card exists: " + stolenCardExists);
        System.out.println("Suspicious IP exists: " + suspiciousIpExists);
        System.out.println("Valid IP Correlation: " + validIpCorrelation);
        System.out.println("Valid Region Correlation: " + validRegionCorrelation);

        String info = stolenCardExists ? suspiciousIpExists ? "card-number, " + "ip" : "card-number" : suspiciousIpExists ? "ip" : "";

        String info1 = !validIpCorrelation ? !validRegionCorrelation ? "ip" + "-correlation, " + "region-correlation" : "ip" + "-correlation" : !validRegionCorrelation ? "region" + "-correlation" : "";

        String newInfo = Objects.equals(info, "") ? info1 : Objects.equals(info1, "") ? info : info + ", " + info1;
        System.out.println("Info :" + newInfo);

        if (!validCard || !validIp || Objects.isNull(transactionAmount))
            throw new BadRequest("Invalid card or ip or transaction amount");

        boolean state = stolenCardExists || suspiciousIpExists;

        System.out.println("State :" + state);

        boolean suspiciousCorrelation = !validRegionCorrelation || !validIpCorrelation;

        System.out.println("SuspiciousCorrelation :" + suspiciousCorrelation);

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
        Long maxAllowedRangeAmount = feedbackLimits.getAllowed() == null ? DEFAULT_MAX_ALLOWED_VALUE : feedbackLimits.getAllowed();
        System.out.println("Max allowed amount: " + feedbackLimits.getAllowed() + "; trans allowed amount: " + feedbackLimits.getMaxAllowedAmount() + "; result allowed amount: " + maxAllowedRangeAmount);

        if (feedbackLimits.getAllowed() == null) {
            feedbackLimitsRepository.updateAllowedByNumberIgnoreCase(DEFAULT_MAX_ALLOWED_VALUE, transaction.getNumber());
        }

        Long maxManualRangeAmount = feedbackLimits.getManual() == null ? DEFAULT_MAX_MANUAL_VALUE : feedbackLimits.getManual();
        System.out.println("Max manual amount: " + feedbackLimits.getManual() + "; trans manual amount: " + feedbackLimits.getMaxManualAmount() + "; result manual amount: " + maxManualRangeAmount);

        if (feedbackLimits.getManual() == null) {
            feedbackLimitsRepository.updateManualByNumberIgnoreCase(DEFAULT_MAX_MANUAL_VALUE, feedbackLimits.getNumber());
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
        System.out.println("List transaction in last hour");
        System.out.println("Number entered is: " + number);

        Iterable<Transaction> transactionList = transactionRepository.findAll();

        List<Transaction> testTransResponse = new ArrayList<>();

        transactionList.forEach(transaction -> {
            if (Objects.equals(transaction.getNumber(), number)) {
                long minDiff = Duration.between(transaction.getDate(), timeNow).toMinutes();
                if (minDiff >= 0 && minDiff <= 60) {
                    testTransResponse.add(transaction);
                }
            }
        });

        testTransResponse.sort(Comparator.comparing(Transaction::getId).reversed());
        return testTransResponse;
    }

    public List<AllTransactionResponse> listAllCardTransactions() {
        System.out.println("/history");
        List<Transaction> allCardTransactions;
        List<AllTransactionResponse> allTransactionResponses = new ArrayList<>();
        try {
            allCardTransactions = (List<Transaction>) transactionRepository.findAll();

            for (Transaction trans : allCardTransactions) {
                allTransactionResponses.add(new AllTransactionResponse(trans.getId(), trans.getAmount(), trans.getIp(), trans.getNumber(), trans.getRegion(), trans.getDate(), trans.getResult(), trans.getFeedback()));
            }


        } catch (Exception e) {
            System.out.println("Exception: " + e);
            throw new UnProcessable("Issue fetching records from transaction " + "table");
        }
        allTransactionResponses.sort(Comparator.comparing(AllTransactionResponse::getTransactionId));
        return allTransactionResponses;
    }

    public List<AllTransactionResponse> listCardTransactions(String number) {

        System.out.println("/history/{number}");
        List<Transaction> allCardTransactions;

        boolean validCard = LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(number);

        if (!validCard) throw new BadRequest("Invalid Card number");
        List<AllTransactionResponse> allTransactionResponses = new ArrayList<>();

        allCardTransactions = transactionRepository.findByNumber(number);

        if (allCardTransactions.isEmpty()) {
            throw new NotFound("No " + "transactions associated with the " + "card number");
        }

        try {
            for (Transaction trans : allCardTransactions) {
                allTransactionResponses.add(new AllTransactionResponse(trans.getId(), trans.getAmount(), trans.getIp(), trans.getNumber(), trans.getRegion(), trans.getDate(), trans.getResult(), trans.getFeedback()));
            }

        } catch (Exception e) {
            System.out.println("Exception: " + e);
            throw new UnProcessable("Exception pulling records from " + "transactions table");
        }
        allTransactionResponses.sort(Comparator.comparing(AllTransactionResponse::getTransactionId));
        return allTransactionResponses;
    }

    double increaseLimit(Long DEFAULT_VALUE, Long currentLimit, Long transactionValue) {
        System.out.println("Increase Limit");
        System.out.println("Current before: " + currentLimit + "; transaction" + " Value: " + transactionValue);
        Long current = currentLimit == null ? DEFAULT_VALUE : currentLimit;
        System.out.println("Current: " + current);
        return Math.ceil(0.8 * current + 0.2 * transactionValue);
    }

    double decreaseLimit(Long DEFAULT_VALUE, Long currentLimit, Long transactionValue) {
        System.out.println("Decrease Limit");
        System.out.println("Current before: " + currentLimit + "; transaction" + " Value: " + transactionValue);
        Long current = currentLimit == null ? DEFAULT_VALUE : currentLimit;
        System.out.println("Current: " + current);
        return Math.ceil(0.8 * current - 0.2 * transactionValue);
    }

    public AllTransactionResponse updateTransaction(UpdateTransactionRequest updateTransactionRequest) {
        System.out.println("Put: /transaction");
        Optional<Transaction> fetchTransaction = transactionRepository.findById(updateTransactionRequest.getTransactionId());

        if (fetchTransaction.isEmpty())
            throw new NotFound("Transaction not " + "found!");

        List<String> arrayList = new ArrayList<>();

        for (AntiFraudApplication.TransactionState val : AntiFraudApplication.TransactionState.values()) {
            arrayList.add(val.toString());
        }

        if (!arrayList.contains(updateTransactionRequest.getFeedback()))
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
                System.out.println("Feedback :" + feedback);
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

                        double allowedRange = increaseLimit(DEFAULT_MAX_ALLOWED_VALUE, allowedAmount, transactionAmount);
                        feedbackLimitsRepository.updateAllowedAndMaxAllowedAmountByNumberIgnoreCase((long) allowedRange, newMaxAllowed, number);

                    } else if (Objects.equals(transaction.getResult(), "PROHIBITED")) {

                        double allowedRange = increaseLimit(DEFAULT_MAX_ALLOWED_VALUE, allowedAmount, transactionAmount);
                        double manualRange = increaseLimit(DEFAULT_MAX_MANUAL_VALUE, manualAmount, transactionAmount);

                        feedbackLimitsRepository.updateAllowedAndManualAndMaxAllowedAmountAndMaxManualAmountByNumberIgnoreCase((long) allowedRange, (long) manualRange, newMaxAllowed, newMaxManual, number);

                    } else {
                        throw new UnProcessable("Result same as feedback");
                    }
                });
            }
            case "MANUAL_PROCESSING" -> {
                System.out.println("Feedback :" + feedback);
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

                        double allowedRange = decreaseLimit(DEFAULT_MAX_ALLOWED_VALUE, allowedAmount, transactionAmount);
                        feedbackLimitsRepository.updateAllowedAndMaxAllowedAmountByNumberIgnoreCase((long) allowedRange, newMaxAllowed, number);

                    } else if (Objects.equals(transaction.getResult(), "PROHIBITED")) {

                        double manualRange = increaseLimit(DEFAULT_MAX_MANUAL_VALUE, manualAmount, transactionAmount);
                        feedbackLimitsRepository.updateManualAndMaxManualAmountByNumberIgnoreCase((long) manualRange, newMaxManual, number);

                    } else {
                        throw new UnProcessable("Result same as feedback");
                    }
                });
            }
            case "PROHIBITED" -> {
                System.out.println("Feedback :" + feedback);
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

                        double allowedRange = decreaseLimit(DEFAULT_MAX_ALLOWED_VALUE, allowedAmount, transactionAmount);
                        double manualRange = decreaseLimit(DEFAULT_MAX_MANUAL_VALUE, manualAmount, transactionAmount);
                        feedbackLimitsRepository.updateAllowedAndManualAndMaxAllowedAmountAndMaxManualAmountByNumberIgnoreCase((long) allowedRange, (long) manualRange, newMaxAllowed, newMaxManual, number);

                    } else if (Objects.equals(transaction.getResult(), "MANUAL_PROCESSING")) {

                        double manualRange = decreaseLimit(DEFAULT_MAX_MANUAL_VALUE, manualAmount, transactionAmount);
                        feedbackLimitsRepository.updateManualAndMaxManualAmountByNumberIgnoreCase((long) manualRange, maxManualAmount, number);

                    } else {
                        throw new UnProcessable("Result same as feedback");
                    }
                });
            }
        }


        System.out.println("Updating values");

        transactionRepository.updateFeedbackById(updateTransactionRequest.getFeedback(), updateTransactionRequest.getTransactionId());

        Optional<Transaction> resultTransaction = transactionRepository.findById(updateTransactionRequest.getTransactionId());

        return new AllTransactionResponse(resultTransaction.orElseThrow().getId(), resultTransaction.orElseThrow().getAmount(), resultTransaction.orElseThrow().getIp(), resultTransaction.orElseThrow().getNumber(), resultTransaction.orElseThrow().getRegion(), resultTransaction.orElseThrow().getDate(), resultTransaction.orElseThrow().getResult(), resultTransaction.orElseThrow().getFeedback());

    }


}
