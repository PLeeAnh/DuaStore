package com.duastore.service.admin;

import com.duastore.model.*;
import com.duastore.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminCustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final CustomerNoteRepository customerNoteRepository;
    private final CustomerTagRepository customerTagRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;

    public AdminCustomerService(UserRepository userRepository,
            OrderRepository orderRepository,
            AddressRepository addressRepository,
            CustomerNoteRepository customerNoteRepository,
            CustomerTagRepository customerTagRepository,
            LoyaltyTransactionRepository loyaltyTransactionRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.customerNoteRepository = customerNoteRepository;
        this.customerTagRepository = customerTagRepository;
        this.loyaltyTransactionRepository = loyaltyTransactionRepository;
    }

    private static final String ROLE_USER = "USER";

    public Page<User> searchCustomers(String keyword, String status, String city,
            String spendingTier, Pageable pageable) {
        if (city != null && !city.isBlank()) {
            return userRepository.searchByKeywordStatusAndCity(keyword, status, city, ROLE_USER, pageable);
        }
        if (spendingTier != null && !spendingTier.isBlank()) {
            return userRepository.searchByKeywordStatusAndSpending(keyword, status, spendingTier, ROLE_USER, pageable);
        }
        boolean searching = (keyword != null && !keyword.isBlank())
                || (status != null && !status.isBlank());
        if (searching) {
            return userRepository.searchByKeywordAndStatus(keyword, status, ROLE_USER, pageable);
        }
        return userRepository.findByRole(ROLE_USER, pageable);
    }

    public Map<Integer, Long> getOrderCountMap(List<Integer> userIds) {
        Map<Integer, Long> orderCountMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            orderRepository.countByUserIds(userIds).forEach(row
                    -> orderCountMap.put((Integer) row[0], (Long) row[1]));
        }
        return orderCountMap;
    }

    public Map<Integer, Integer> getLoyaltyBalanceMap(List<Integer> userIds) {
        Map<Integer, Integer> map = new HashMap<>();
        for (Integer uid : userIds) {
            map.put(uid, loyaltyTransactionRepository.findCurrentBalanceByUserId(uid));
        }
        return map;
    }

    public List<String> getAllDistinctCities() {
        return addressRepository.findAllDistinctCities();
    }

    public List<CustomerNote> getNotes(Integer userId) {
        return customerNoteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public CustomerNote addNote(Integer userId, String content, String createdBy) {
        CustomerNote note = new CustomerNote();
        note.setUserId(userId);
        note.setContent(content);
        note.setCreatedBy(createdBy);
        return customerNoteRepository.save(note);
    }

    public void deleteNote(Integer noteId, Integer userId) {
        customerNoteRepository.deleteByIdAndUserId(noteId, userId);
    }

    public List<CustomerTag> getTags(Integer userId) {
        return customerTagRepository.findByUserId(userId);
    }

    public List<String> getAllTags() {
        return customerTagRepository.findDistinctTags();
    }

    public CustomerTag addTag(Integer userId, String tag) {
        if (customerTagRepository.findByUserId(userId).stream().anyMatch(t -> t.getTag().equals(tag))) {
            return null;
        }
        CustomerTag ct = new CustomerTag();
        ct.setUserId(userId);
        ct.setTag(tag);
        return customerTagRepository.save(ct);
    }

    public void removeTag(Integer tagId, Integer userId) {
        customerTagRepository.deleteByIdAndUserId(tagId, userId);
    }

    public int getLoyaltyBalance(Integer userId) {
        return loyaltyTransactionRepository.findCurrentBalanceByUserId(userId);
    }

    public List<LoyaltyTransaction> getLoyaltyHistory(Integer userId) {
        return loyaltyTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
