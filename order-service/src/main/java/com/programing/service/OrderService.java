package com.programing.service;

import com.programing.dto.InventoryResponse;
import com.programing.dto.OrderLineItemsDto;
import com.programing.dto.OrderRequest;
import com.programing.model.Order;
import com.programing.model.OrderLineItems;
import com.programing.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItems()
                .stream()
                .map(this::maoToDto)
                .toList();

        order.setOrderLineItems(orderLineItems);

        List<String> skuCodes = order.getOrderLineItems()
                .stream()
                .map(OrderLineItems::getSkuCode)
                .toList();
        // Call inventory service, and place order if product is in stock
        InventoryResponse[] inventoryResponses = webClientBuilder
                .build()
                .get()
                .uri("http://inventory-service/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCodes", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        log.info("info of inventory {}", (Object) inventoryResponses);


        assert inventoryResponses != null;
        boolean result = Arrays.stream(inventoryResponses)
                .allMatch(inventoryResponse -> inventoryResponse.isInStock);
        if (Boolean.TRUE.equals(result)) {
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Product is not in stock, please try again later");
        }
    }

    private OrderLineItems maoToDto(OrderLineItemsDto orderLineItemDto) {

        OrderLineItems orderLineItem = new OrderLineItems();
        orderLineItem.setPrice(orderLineItemDto.getPrice());
        orderLineItem.setQuantity(orderLineItemDto.getQuantity());
        orderLineItem.setSkuCode(orderLineItemDto.getSkuCode());
        return orderLineItem;
    }

}
