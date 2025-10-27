package profect.group1.goormdotcom.review.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import profect.group1.goormdotcom.review.controller.dto.CreateReviewRequestDto;
import profect.group1.goormdotcom.review.controller.dto.ProductReviewListResponseDto;
import profect.group1.goormdotcom.review.controller.dto.ReviewResponseDto;
import profect.group1.goormdotcom.review.controller.dto.UpdatedReviewRequestDto;
import profect.group1.goormdotcom.review.controller.mapper.ReviewDtoMapper;
import profect.group1.goormdotcom.review.controller.v1.ProductReviewListResponse;
import profect.group1.goormdotcom.review.domain.Review;
import profect.group1.goormdotcom.review.repository.ReviewImageRepository;
import profect.group1.goormdotcom.review.repository.ReviewRepository;
import profect.group1.goormdotcom.review.repository.entity.ReviewEntity;
import profect.group1.goormdotcom.review.repository.entity.ReviewImageEntity;
import profect.group1.goormdotcom.review.repository.mapper.ReviewMapper;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;

    // TODO: 다른 도메인 서비스 통신용 (나중에 추가)

    // 용도 : presigned client 받아오기
    /*
    private final PresignedClient presignedClient;

    public ReviewService(PresignedClient presignedClient) {
        this.presignedClient = presignedClient;
    }
     */


    /**
     * 리뷰 생성 (POST)
     *
     * @param request 리뷰 요청 데이터
     * @param userId 인증된 사용자 ID
     * @return 생성된 리뷰 응답
     */
    @Transactional
    public ReviewResponseDto createReview(CreateReviewRequestDto request, UUID userId) {

        // 1. productId로 deliveryId 조회 (실제론 외부 서비스 호출)
        UUID deliveryId = getDeliveryIdByProduct(request.getProductId(), userId);

        // 2. 배송 상태 검증 (배송 완료 여부)
        validateDeliveryStatus(deliveryId);

        // 3. 중복 리뷰 검증 (같은 배송에 대해 이미 리뷰 작성했는지)
        if (reviewRepository.existsByDeliveryId(deliveryId)) {
            throw new IllegalStateException("이미 해당 주문에 대한 리뷰를 작성하셨습니다.");
        }

        // 4. Domain 객체 생성 (비즈니스 로직 검증 포함)
        Review review = ReviewDtoMapper.toDomain(request, userId, deliveryId);

        // 5. Review 저장 (p_review 테이블)
        ReviewEntity savedReviewEntity = reviewRepository.save(
                ReviewMapper.toEntity(review)
        );

        // 6. 이미지가 있으면 ReviewImage 저장 (p_review_image 테이블)
        String savedImageUrl = null;
        if (review.getImageUrl() != null && !review.getImageUrl().isBlank()) {
            ReviewImageEntity imageEntity = ReviewMapper.toImageEntity(
                    savedReviewEntity.getId(),
                    review.getImageUrl()
            );
            ReviewImageEntity savedImageEntity = reviewImageRepository.save(imageEntity);
            savedImageUrl = savedImageEntity.getImageObject();

            // S3 temp -> main 이동 (업로드 확정)
            /*if (savedReview.getFileId() != null) {
                presignedClient.confirmUpload(savedReview.getFileId());
            }*/
        }

        // 7. Domain으로 재구성 후 응답 반환
        Review savedReview = ReviewMapper.toDomain(savedReviewEntity, savedImageUrl);
        return ReviewDtoMapper.toResponseDto(savedReview);
    }


    /**
     * 상품 ID와 사용자 ID로 배송 ID 조회
     * (실제론 Order/Delivery 서비스 호출)
     */
    private UUID getDeliveryIdByProduct(UUID productId, UUID userId) {
        // TODO: 외부 서비스 호출
        // DeliveryDto delivery = deliveryServiceClient.getDeliveryByProduct(productId, userId);
        // return delivery.getDeliveryId();

        return UUID.randomUUID(); // 임시
    }

    /**
     * 배송 상태 검증 (배송 완료 여부)
     */
    private void validateDeliveryStatus(UUID deliveryId) {
        // TODO: Delivery 서비스 호출
        // DeliveryDto delivery = deliveryServiceClient.getDelivery(deliveryId);
        //
        // if (delivery.getStatus() != DeliveryStatus.COMPLETED) {
        //     throw new IllegalStateException("배송이 완료된 주문만 리뷰를 작성할 수 있습니다.");
        // }

        // 임시: 검증 통과
    }

    /**
     * 상품별 리뷰 목록 조회 (GET)
     *
     * @param productId 상품 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sortBy 정렬 기준
     * @return 리뷰 목록 응답
     */
    public ProductReviewListResponseDto getProductReviews(UUID productId, int page, int size, String sortBy) {
        // 1. 페이징 및 정렬 설정
        Sort sort = sortBy.equals("rating")
                ? Sort.by(Sort.Direction.DESC, "rating")
                : Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        // 2. 리뷰 목록 조회
        Page<ReviewEntity> reviewPage = reviewRepository.findByProductId(productId, pageable);

        // 3. 각 리뷰에 이미지 URL 매핑
        List<ReviewResponseDto> reviewResponses = reviewPage.getContent().stream()
                .map(reviewEntity -> {
                    String imageUrl = reviewImageRepository.findByReviewId(reviewEntity.getId())
                            .map(ReviewImageEntity::getImageObject)
                            .orElse(null);
                    Review review = ReviewMapper.toDomain(reviewEntity, imageUrl);
                    return ReviewDtoMapper.toResponseDto(review);
                })
                .toList();

        // 4. 통계 정보 조회
        Double averageRating = reviewRepository.findAverageRatingByProductId(productId);
        long totalReviews = reviewRepository.countByProductId(productId);

        // 5. 상품 이름 조회 TODO:추후 ProductService 연동하여 상품 이름 가져오기
        String productName = "";

        // 6. 응답 생성
        return new ProductReviewListResponseDto(
                productId,
                productName,
                averageRating != null ? averageRating : 0.0,
                (int) totalReviews,
                reviewResponses,
                page,
                size,
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages()
        );
    }

    /**
     * 리뷰 단건 조회 (GET)
     *
     * @param reviewId 리뷰 ID
     * @return 리뷰 응답
     */
    public ReviewResponseDto getReview(UUID reviewId) {
        // 1. 리뷰 조회
        ReviewEntity reviewEntity = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."
                ));

        // 2. 이미지 조회 (있으면)
        String imageUrl = reviewImageRepository.findByReviewId(reviewId)
                .map(ReviewImageEntity::getImageObject)
                .orElse(null);

        // 3. Domain 변환 후 응답
        Review review = ReviewMapper.toDomain(reviewEntity, imageUrl);
        return ReviewDtoMapper.toResponseDto(review);
    }

    /**
     * 리뷰 수정 (put service)
     * @param reviewId 리뷰 ID
     * @param request 수정 요청 데이터
     * @param userId 인증된 사용자 ID
     * @return 수정된 리뷰 응답
     */

    @Transactional
    public ReviewResponseDto updateReview(UUID reviewId, UpdatedReviewRequestDto request, UUID userId) {
        // 1. 리뷰 조회
        ReviewEntity reviewEntity = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        // 2. 권한 확인 (본인 리뷰인지)
        if (!reviewEntity.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 리뷰만 수정할 수 있습니다.");
        }

        // 3. 기존 이미지 조회
        String existingImageUrl = reviewImageRepository.findByReviewId(reviewId)
                .map(ReviewImageEntity::getImageObject)
                .orElse(null);

        // 4. Domain으로 변환 후 수정
        Review review = ReviewMapper.toDomain(reviewEntity, existingImageUrl);
        Review updatedReview = review.update(
                request.getRating(),
                request.getContent(),
                request.getImageUrl()
        );

        // 5. Entity 저장
        ReviewEntity updatedEntity = reviewRepository.save(
                ReviewMapper.toEntity(updatedReview)
        );

        // 6. 이미지 처리 (변경/삭제/추가)
        handleImageUpdate(reviewId, request.getImageUrl(), existingImageUrl);

        // 7. 응답 반환
        String finalImageUrl = request.getImageUrl() != null
                ? request.getImageUrl()
                : existingImageUrl;
        Review savedReview = ReviewMapper.toDomain(updatedEntity, finalImageUrl);
        return ReviewDtoMapper.toResponseDto(savedReview);
    }


    /**
     * 리뷰 삭제 (DELETE) - Soft Delete
     *
     * @param reviewId 리뷰 ID
     * @param userId 인증된 사용자 ID
     */
    @Transactional
    public void deleteReview(UUID reviewId, UUID userId) {
        // 1. 리뷰 조회
        ReviewEntity reviewEntity = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "리뷰를 찾을 수 없습니다."));

        // 2. 권한 확인 (본인 리뷰인지)
        if (!reviewEntity.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"본인의 리뷰만 삭제할 수 있습니다.");
        }

        // 3. Soft Delete (JPA @SQLDelete 어노테이션이 자동 처리)
        reviewRepository.delete(reviewEntity);

        // 4. 이미지도 Soft Delete
        reviewImageRepository.findByReviewId(reviewId)
                .ifPresent(reviewImageRepository::delete);
    }

    /**
     * 이미지 업데이트 처리
     * - null: 변경 없음 (기존 유지)
     * - 빈 문자열: 이미지 삭제
     * - URL: 이미지 변경/추가
     */
    private void handleImageUpdate(UUID reviewId, String newImageUrl, String existingImageUrl) {
        if (newImageUrl == null) {
            // null이면 변경 없음
            return;
        }

        if (newImageUrl.isBlank()) {
            // 빈 문자열이면 이미지 삭제
            reviewImageRepository.findByReviewId(reviewId)
                    .ifPresent(reviewImageRepository::delete);
        } else {
            // URL이 있으면 변경 또는 추가
            reviewImageRepository.findByReviewId(reviewId)
                    .ifPresentOrElse(
                            // 기존 이미지 있으면 삭제 후 새로 추가
                            imageEntity -> {
                                reviewImageRepository.delete(imageEntity);
                                ReviewImageEntity newImage = ReviewMapper.toImageEntity(reviewId, newImageUrl);
                                reviewImageRepository.save(newImage);
                            },
                            // 기존 이미지 없으면 새로 추가
                            () -> {
                                ReviewImageEntity newImage = ReviewMapper.toImageEntity(reviewId, newImageUrl);
                                reviewImageRepository.save(newImage);
                            }
                    );
        }
    }





}
