package com.ying.learneyjourney.service;

import com.ying.learneyjourney.entity.Orders;
import com.ying.learneyjourney.repository.OrdersRepository;
import com.ying.learneyjourney.request.CreateSessionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrdersService {
    private final OrdersRepository ordersRepository;
    public String create(CreateSessionRequest request, String status){
        Orders order = new Orders();
        order.setUserId(request.getUserId());
        order.setAmount(request.getAmount());
        order.setCurrency(request.getCurrency());
        order.setStatus(status);
        ordersRepository.save(order);
        return order.getId().toString();
    }
}
