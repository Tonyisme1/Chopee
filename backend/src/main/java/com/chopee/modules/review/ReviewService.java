package com.chopee.modules.review;

import com.chopee.common.dto.PageResponse;
import com.chopee.entity.*;
import com.chopee.entity.enums.OrderStatus;
import com.chopee.modules.review.dto.CreateReviewRequest;
import com.chopee.modules.review.dto.ReplyReviewRequest;
import com.chopee.modules.review.dto.ReviewResponse;
import com.chopee.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getProductReviews(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findByProductIdAndIsDeletedFalseOrderByCreatedAtDesc(productId, pageable);

        List<ReviewResponse> content = reviewPage.getContent().stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());

        return PageResponse.from(reviewPage, content);
    }

    @Transactional
    public ReviewResponse createReview(Long userId, CreateReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin người dùng"));

        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mục đơn hàng không tồn tại"));

        Order order = orderItem.getOrder();
        if (order == null || !order.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền đánh giá đơn hàng của người khác");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể đánh giá sản phẩm sau khi đơn hàng đã được giao thành công");
        }

        if (reviewRepository.existsByOrderItemId(orderItem.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mục đơn hàng này đã được bạn đánh giá trước đó");
        }

        Product product = orderItem.getProduct();

        Review review = Review.builder()
                .product(product)
                .user(user)
                .orderItem(orderItem)
                .rating(request.getRating())
                .comment(request.getComment())
                .imagesJson(request.getImagesJson())
                .isDeleted(false)
                .build();

        Review saved = reviewRepository.save(review);

        // Cập nhật lại rating_avg và review_count trên sản phẩm
        long count = reviewRepository.countByProductIdAndIsDeletedFalse(product.getId());
        Double avg = reviewRepository.calculateAverageRating(product.getId());
        if (avg != null) {
            product.setRatingAvg(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
        }
        product.setReviewCount((int) count);
        productRepository.save(product);

        return ReviewResponse.fromEntity(saved);
    }

    @Transactional
    public ReviewResponse replyReview(Long sellerUserId, Long reviewId, ReplyReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Đánh giá không tồn tại"));

        Shop shop = review.getProduct().getShop();
        if (shop == null || !shop.getUser().getId().equals(sellerUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền phản hồi đánh giá sản phẩm của shop khác");
        }

        review.setShopReply(request.getShopReply());
        Review updated = reviewRepository.save(review);
        return ReviewResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getShopReviews(Long sellerUserId, int page, int size) {
        Shop shop = shopRepository.findByUserId(sellerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn chưa đăng ký gian hàng người bán"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findByProductShopIdAndIsDeletedFalseOrderByCreatedAtDesc(shop.getId(), pageable);

        List<ReviewResponse> content = reviewPage.getContent().stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());

        return PageResponse.from(reviewPage, content);
    }
}
