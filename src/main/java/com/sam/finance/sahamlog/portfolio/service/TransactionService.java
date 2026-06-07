package com.sam.finance.sahamlog.portfolio.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sam.finance.sahamlog.auth.service.CurrentUserService;
import com.sam.finance.sahamlog.portfolio.domain.TransactionEntry;
import com.sam.finance.sahamlog.portfolio.dto.TransactionFilter;
import com.sam.finance.sahamlog.portfolio.dto.TransactionRequest;
import com.sam.finance.sahamlog.portfolio.dto.TransactionResponse;
import com.sam.finance.sahamlog.portfolio.repository.TransactionEntryRepository;
import com.sam.finance.sahamlog.portfolio.repository.TransactionSpecifications;
import com.sam.finance.sahamlog.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionEntryRepository transactionEntryRepository;
    private final CurrentUserService currentUserService;
    private final StockService stockService;
    private final TransactionStateValidator transactionStateValidator;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        Long userId = currentUserService.getCurrentUserId();

        TransactionEntry entry = new TransactionEntry();
        populateEntry(entry, request);

        transactionStateValidator.validateCandidateState(userId, entry, null, false);
        return transactionMapper.toResponse(transactionEntryRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> findPage(TransactionFilter filter, Pageable pageable) {
        Long userId = currentUserService.getCurrentUserId();
        Specification<TransactionEntry> specification = buildSpecification(userId, filter);

        return transactionEntryRepository.findAll(specification, pageable)
            .map(transactionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll(TransactionFilter filter) {
        Long userId = currentUserService.getCurrentUserId();
        Specification<TransactionEntry> specification = buildSpecification(userId, filter);

        return transactionEntryRepository.findAll(specification)
            .stream()
            .sorted((left, right) -> right.getTransactionDate().equals(left.getTransactionDate())
                ? right.getId().compareTo(left.getId())
                : right.getTransactionDate().compareTo(left.getTransactionDate()))
            .map(transactionMapper::toResponse)
            .toList();
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        TransactionEntry entry = findOwnedTransaction(id, userId);
        Long originalStockId = entry.getStock().getId();

        populateEntry(entry, request);

        transactionStateValidator.validateCandidateState(userId, entry, originalStockId, false);
        return transactionMapper.toResponse(transactionEntryRepository.save(entry));
    }

    @Transactional
    public void delete(Long id) {
        Long userId = currentUserService.getCurrentUserId();
        TransactionEntry entry = findOwnedTransaction(id, userId);
        transactionStateValidator.validateCandidateState(userId, entry, entry.getStock().getId(), true);
        transactionEntryRepository.delete(entry);
    }

    @Transactional(readOnly = true)
    public List<TransactionEntry> findUserTransactionsOrdered(Long userId) {
        return transactionEntryRepository.findByUser_IdOrderByStock_CodeAscTransactionDateAscIdAsc(userId);
    }

    private TransactionEntry findOwnedTransaction(Long id, Long userId) {
        return transactionEntryRepository.findByIdAndUser_Id(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    private void populateEntry(TransactionEntry entry, TransactionRequest request) {
        entry.setUser(currentUserService.getCurrentAppUser());
        entry.setStock(stockService.findEntityById(request.stockId()));
        entry.setType(request.type());
        entry.setTransactionDate(request.transactionDate());
        entry.setQuantityLot(request.quantityLot());
        entry.setPrice(request.price());
        entry.setFee(request.fee());
        entry.setNotes(request.notes());
    }

    private Specification<TransactionEntry> buildSpecification(Long userId, TransactionFilter filter) {
        return TransactionSpecifications.hasUserId(userId)
            .and(TransactionSpecifications.hasStockCode(filter.stockCode()))
            .and(TransactionSpecifications.hasType(filter.type()))
            .and(TransactionSpecifications.hasDateFrom(filter.dateFrom()))
            .and(TransactionSpecifications.hasDateTo(filter.dateTo()));
    }
}
