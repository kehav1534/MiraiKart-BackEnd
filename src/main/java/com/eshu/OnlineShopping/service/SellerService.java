package com.eshu.OnlineShopping.service;


import com.eshu.OnlineShopping.dto.ProductMetricsDto;
import com.eshu.OnlineShopping.dto.SellerDto;
import com.eshu.OnlineShopping.dto.SellerMetricsDto;
import com.eshu.OnlineShopping.dto.SellerOrderDto;
import com.eshu.OnlineShopping.dto.SellerRegisterDto;
import com.eshu.OnlineShopping.enums.OrderStatus;
import com.eshu.OnlineShopping.enums.ProductListingStatus;
import com.eshu.OnlineShopping.exceptions.DuplicateException;
import com.eshu.OnlineShopping.exceptions.InsufficientStockException;
import com.eshu.OnlineShopping.exceptions.NotFoundException;
import com.eshu.OnlineShopping.model.Order;
import com.eshu.OnlineShopping.model.OrderItem;
import com.eshu.OnlineShopping.model.Product;
import com.eshu.OnlineShopping.model.Seller;
import com.eshu.OnlineShopping.model.SellerAuth;
import com.eshu.OnlineShopping.repository.OrderItemRepository;
import com.eshu.OnlineShopping.repository.ProductRepository;
import com.eshu.OnlineShopping.repository.SellerAuthRepository;
import com.eshu.OnlineShopping.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SellerService {

    /**
     * The only order-status transitions a seller is allowed to make, keyed by
     * the item's current status. This is deliberately a forward-only pipeline:
     *  - No backward moves (e.g. SHIPPED -> PACKED) once fulfilment has
     *    progressed, since that would misrepresent what actually happened to
     *    the shipment.
     *  - CANCELLED is only reachable before the item has left the seller
     *    (PROCESSING/CONFIRMED/PACKED). Once SHIPPED/TRANSIT, the courier
     *    already has it, so "cancel" is no longer a truthful state - a
     *    return/refund flow (ReturnStatus) is the correct path instead.
     *  - DELIVERED and CANCELLED are terminal: nothing may change after them.
     * SellerController and SellerOrderDto both lean on this same map (via
     * getAllowedNextStatuses) so the UI and the enforcement never drift apart.
     */
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);
    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.PROCESSING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.CONFIRMED, Set.of(OrderStatus.PACKED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.PACKED, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.SHIPPED, Set.of(OrderStatus.TRANSIT));
        ALLOWED_TRANSITIONS.put(OrderStatus.TRANSIT, Set.of(OrderStatus.DELIVERED));
        ALLOWED_TRANSITIONS.put(OrderStatus.DELIVERED, Set.of());
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, Set.of());
    }

    private static List<OrderStatus> allowedNextStatuses(OrderStatus current) {
        return List.copyOf(ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()));
    }

    @Autowired
    SellerRepository sellerRepository;

    @Autowired
    SellerAuthRepository sellerAuthRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    ProductRepository productRepository;

    public List<Seller> getAllSellerList(){
        return sellerRepository.findAll();
    }

    public Seller findSellerById(int sellerId){
        return sellerRepository.findById(sellerId)
                .orElseThrow(()-> new NotFoundException("Seller", sellerId));
    }

    /**
     * Single signup entry point: creates the SellerAuth credential row and
     * the Seller profile row together and links them, so a token issued at
     * login always resolves back to a real, complete seller profile.
     */
    @Transactional
    public String registerSeller(SellerRegisterDto dto) {
        if (sellerAuthRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateException("Email", dto.getEmail());
        }
        if (dto.getContactNo() != null && sellerRepository.existsByContactNo(dto.getContactNo())) {
            throw new DuplicateException("Contact number", dto.getContactNo());
        }
        if (dto.getEntityName() != null && sellerRepository.existsByEntityName(dto.getEntityName())) {
            throw new DuplicateException("Entity name", dto.getEntityName());
        }

        SellerAuth sellerAuth = new SellerAuth();
        sellerAuth.setEmail(dto.getEmail());
        sellerAuth.setPassword(passwordEncoder.encode(dto.getPassword()));
        sellerAuthRepository.save(sellerAuth);

        Seller seller = new Seller();
        seller.setFullName(dto.getFullName());
        seller.setContactNo(dto.getContactNo());
        seller.setEntityName(dto.getEntityName());
        seller.setSellerAuth(sellerAuth);
        sellerRepository.save(seller);

        return "Seller registered successfully";
    }

    public void updateSellerDetails(int sellerId, SellerDto sellerDto){
        Seller seller = sellerRepository.findById(sellerId).orElseThrow(()-> new NotFoundException("Seller", sellerId));

        if(sellerDto.getContactNo()!=null && !sellerDto.getContactNo().equals(seller.getContactNo()) &&
                sellerRepository.existsByContactNo(sellerDto.getContactNo())){
            throw new DuplicateException("Contact number", sellerDto.getContactNo());
        }
        seller.setContactNo(sellerDto.getContactNo()!=null?sellerDto.getContactNo():seller.getContactNo());

        if(sellerDto.getEntityName()!=null && !seller.getEntityName().equals(sellerDto.getEntityName()) &&
                sellerRepository.existsByEntityName(sellerDto.getEntityName())){
            throw new DuplicateException("Entity name", sellerDto.getEntityName());
        }
        seller.setEntityName(sellerDto.getEntityName()!=null?sellerDto.getEntityName():seller.getEntityName());
        seller.setFullName(sellerDto.getFullName()!=null?sellerDto.getFullName():seller.getFullName());
        sellerRepository.save(seller);
    }

    public SellerMetricsDto sellerDashBoard(int sellerId){
        sellerRepository.existsById(sellerId);

        long totalReturned = orderItemRepository.getTotalReturnedProducts(sellerId);
        long totalSales = orderItemRepository.getTotalSales(sellerId);
        BigDecimal returnRate = BigDecimal.ZERO;
        if(totalSales>0){
            returnRate = BigDecimal.valueOf(totalReturned)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalSales), 2, RoundingMode.HALF_UP);
        }

        return new SellerMetricsDto(
                    orderItemRepository.getTotalOrders(sellerId),
                    totalSales,
                    orderItemRepository.getTotalGrossRevenue(sellerId),
                    orderItemRepository.getTotalNetRevenue(sellerId),
                    totalReturned,
                    returnRate,
                    orderItemRepository.getTopSellingProducts(sellerId, org.springframework.data.domain.PageRequest.of(0, 5)),
                    productRepository.findBySellerIdIdAndQuantity(sellerId, 0)
        );
    }

    public ProductMetricsDto productMetrics(int sellerId, int productId){
        sellerRepository.existsById(sellerId);

        Product item = productRepository
                        .findByIdAndSellerIdId(productId, sellerId)
                        .orElseThrow(()-> new NotFoundException("Product", productId));

        long totalReturned = orderItemRepository.getReturnedProduct(sellerId, productId);
        long totalSales = orderItemRepository.getProductTotalSales(sellerId, productId);

        BigDecimal returnRate = BigDecimal.ZERO;
        if(totalSales>0){
            returnRate = BigDecimal.valueOf(totalReturned)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalSales), 2, RoundingMode.HALF_UP);
        }
        return new ProductMetricsDto(
                    orderItemRepository.getProductOrders(sellerId, productId),
                    totalSales,
                    orderItemRepository.getProductGrossRevenue(sellerId, productId),
                    orderItemRepository.getProductNetRevenue(sellerId, productId),
                    totalReturned,
                    returnRate,
                    item.getQuantity() == null ? 0 : item.getQuantity()
        );
    }

    /// add and subtract in existing stock
    public String adjustStock(int sellerId, int productId, int qty){
        Product product = productRepository.findByIdAndSellerIdId(productId, sellerId)
                .orElseThrow(()-> new NotFoundException("Product", productId));

        int updatedQty = product.getQuantity()+qty;
        if(updatedQty>=0){
            product.setQuantity(updatedQty);
            productRepository.save(product);
        }
        else throw new InsufficientStockException("Adjustment exceeds available stock. Current stock: " + product.getQuantity());

        return "Stock quantity updated successfully";
    }
    /// Updates the stock value as-it-is
    public String updateStock(int sellerId, int productId, int qty){
        Product product = productRepository.findByIdAndSellerIdId(productId, sellerId)
                .orElseThrow(()-> new NotFoundException("Product", productId));
        if(qty>=0){
            product.setQuantity(qty);
            productRepository.save(product);
        }
        else throw new InsufficientStockException("Adjustment exceeds available stock. Current stock: " + product.getQuantity());

        return "Stock quantity adjusted.";
    }


    public String updateListingStatus(int sellerId, int productId, ProductListingStatus status){
        if(status != ProductListingStatus.LIVE
                && status != ProductListingStatus.DRAFT
                && status != ProductListingStatus.CLOSED){
            throw new IllegalArgumentException("Sellers may only set listing status to LIVE, DRAFT, or CLOSED.");
        }

        Product product = productRepository.findByIdAndSellerIdId(productId, sellerId)
                .orElseThrow(()-> new NotFoundException("Product", productId));

        product.setListingStatus(status);
        productRepository.save(product);

        return "Listing status updated to " + status;
    }

    public List<Product> getAllProducts(int sellerId){
        sellerRepository.existsById(sellerId);

        return productRepository.findBySellerIdId(sellerId).orElse(Collections.emptyList());
    }

    public boolean isValidProduct(int productId){
        productRepository.findById(productId).orElseThrow(()-> new NotFoundException("Product", productId));
        return true;
    }

    @Transactional
    public String updateOrderStatus(int sellerId, int orderItemId, OrderStatus updatedStatus){
        OrderItem oi = orderItemRepository.getOrderItemForSeller(sellerId, orderItemId);
        if(oi == null){
            throw new NotFoundException("Order item", orderItemId);
        }

        OrderStatus current = oi.getStatus();
        Set<OrderStatus> allowedNext = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());

        if(!allowedNext.contains(updatedStatus)){
            String allowedDescription = allowedNext.isEmpty()
                    ? "no further changes are allowed - this order is finalized."
                    : "allowed next status(es): " + allowedNext;
            throw new IllegalArgumentException(
                    "Cannot change order status from " + current + " to " + updatedStatus + "; " + allowedDescription);
        }

        oi.setStatus(updatedStatus);
        orderItemRepository.save(oi);
        return "Order item #" + orderItemId + " status updated to " + updatedStatus;
    }

    public List<SellerOrderDto> getOrders(int sellerId, OrderStatus status, int limit){
        List<SellerOrderDto> orders = orderItemRepository.getSellerOrderDtos(
                sellerId, status, org.springframework.data.domain.PageRequest.of(0, limit));
        orders.forEach(o -> o.setAllowedNextStatuses(allowedNextStatuses(o.getStatus())));
        return orders;
    }

    /** Single order line's full detail, scoped to the caller's own products so one seller can never view another's order. */
    public SellerOrderDto getOrderDetail(int sellerId, int orderItemId){
        SellerOrderDto order = orderItemRepository.getSellerOrderDto(sellerId, orderItemId)
                .orElseThrow(() -> new NotFoundException("Order item", orderItemId));
        order.setAllowedNextStatuses(allowedNextStatuses(order.getStatus()));
        return order;
    }

}
