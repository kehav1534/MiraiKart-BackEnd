package com.eshu.OnlineShopping.service;

import com.eshu.OnlineShopping.enums.OrderStatus;
import com.eshu.OnlineShopping.enums.PaymentMethod;
import com.eshu.OnlineShopping.enums.PaymentStatus;
import com.eshu.OnlineShopping.enums.ReturnStatus;
import com.eshu.OnlineShopping.exceptions.InsufficientStockException;
import com.eshu.OnlineShopping.exceptions.NotFoundException;
import com.eshu.OnlineShopping.model.Cart;
import com.eshu.OnlineShopping.model.CartItem;
import com.eshu.OnlineShopping.model.Order;
import com.eshu.OnlineShopping.model.OrderItem;
import com.eshu.OnlineShopping.model.Product;
import com.eshu.OnlineShopping.model.User;
import com.eshu.OnlineShopping.repository.CartRepository;
import com.eshu.OnlineShopping.repository.OrderRepository;
import com.eshu.OnlineShopping.repository.ProductRepository;
import com.eshu.OnlineShopping.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartRepository cartRepository;

    /**
     * Places an order for everything currently in the buyer's cart, then
     * empties the cart. Stock is checked and decremented per line item
     * inside this one transaction, so a partial failure (one item out of
     * stock) rolls back the whole order rather than leaving some items
     * deducted and others not.
     */
    @Transactional
    public Order placeOrderFromCart(int userId, PaymentMethod paymentMode){
        if(paymentMode == null){
            throw new IllegalArgumentException("Payment method is required.");
        }

        User user = userInfoRepository.findById(userId).orElseThrow(()-> new NotFoundException("User", userId));
        Cart cart = user.getUserCart();
        if(cart == null || cart.getItems().isEmpty()){
            throw new IllegalArgumentException("Your cart is empty.");
        }

        Order order = new Order();
        order.setUserId(user);
        order.setPaymentMode(paymentMode);
        // No real payment gateway is wired up here - COD is genuinely
        // unpaid until delivery, so it starts PENDING; any other method is
        // treated as paid immediately (simulating a successful online
        // payment) so the order flow has something meaningful to show.
        order.setStatus(paymentMode == PaymentMethod.COD ? PaymentStatus.PENDING : PaymentStatus.SUCCESS);

        for(CartItem cartItem : new ArrayList<>(cart.getItems())){
            order.getOrderItems().add(buildOrderItem(cartItem.getProduct(), cartItem.getQuantity(), order));
        }

        orderRepository.save(order); // cascades to save every OrderItem

        cart.getItems().clear(); // orphanRemoval on Cart.items deletes the now-unlinked CartItem rows
        cartRepository.save(cart);

        return order;
    }

    /** Places a single-product order directly, without touching the cart. */
    @Transactional
    public Order buyNow(int userId, int productId, int quantity, PaymentMethod paymentMode){
        if(paymentMode == null){
            throw new IllegalArgumentException("Payment method is required.");
        }
        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        User user = userInfoRepository.findById(userId).orElseThrow(()-> new NotFoundException("User", userId));
        Product product = productRepository.findById(productId).orElseThrow(()-> new NotFoundException("Product", productId));

        Order order = new Order();
        order.setUserId(user);
        order.setPaymentMode(paymentMode);
        order.setStatus(paymentMode == PaymentMethod.COD ? PaymentStatus.PENDING : PaymentStatus.SUCCESS);
        order.getOrderItems().add(buildOrderItem(product, quantity, order));

        return orderRepository.save(order);
    }

    public List<Order> getMyOrders(int userId, int limit){
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId, PageRequest.of(0, limit));
    }

    /** Validates stock, decrements it, and builds the OrderItem line - shared by both purchase paths above. */
    private OrderItem buildOrderItem(Product product, int quantity, Order order){
        int available = product.getQuantity() == null ? 0 : product.getQuantity();
        if(available < quantity){
            throw new InsufficientStockException(
                    "Not enough stock for \"" + product.getName() + "\". Available: " + available);
        }

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQty(quantity);
        orderItem.setPurchasePrice(effectivePrice(product));
        orderItem.setStatus(OrderStatus.PROCESSING);
        orderItem.setReturnStatus(ReturnStatus.NONE);
        orderItem.setOrderId(order);

        product.setQuantity(available - quantity);
        productRepository.save(product);

        return orderItem;
    }

    /** Snapshots the price actually paid (after any discount) at the moment of purchase. */
    private BigDecimal effectivePrice(Product product){
        BigDecimal price = product.getPrice();
        int discount = product.getDiscount();
        if(discount <= 0) return price;
        BigDecimal multiplier = BigDecimal.valueOf(100 - discount).divide(BigDecimal.valueOf(100));
        return price.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
