package com.duastore.service.admin;

import com.duastore.model.Order;
import com.duastore.model.OrderNote;
import com.duastore.model.User;
import com.duastore.repository.OrderNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderNoteService {

    private final OrderNoteRepository repository;

    public OrderNoteService(OrderNoteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OrderNote addNote(Order order, User admin, String noiDung) {
        OrderNote note = new OrderNote();
        note.setOrder(order);
        note.setAdmin(admin);
        note.setNoiDung(noiDung);
        return repository.save(note);
    }

    @Transactional(readOnly = true)
    public List<OrderNote> getNotesByOrder(Integer orderId) {
        return repository.findByOrderIdOrderByNgayTaoAsc(orderId);
    }
}
