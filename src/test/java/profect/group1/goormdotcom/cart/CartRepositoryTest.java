package profect.group1.goormdotcom.cart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import profect.group1.goormdotcom.cart.repository.CartItemRepository;
import profect.group1.goormdotcom.cart.repository.CartRepository;
import profect.group1.goormdotcom.cart.repository.entity.CartEntity;
import profect.group1.goormdotcom.cart.repository.entity.CartItemEntity;
import profect.group1.goormdotcom.common.config.JpaAuditingConfig;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
public class CartRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    @DisplayName("CartRepository - 사용자 ID로 장바구니 찾기 테스트")
    void findByCustomerId_Cart() {
        // given
        UUID customerId = UUID.randomUUID();
        CartEntity cartEntity = new CartEntity(customerId);
        entityManager.persist(cartEntity);
        entityManager.flush();

        // when
        Optional<CartEntity> foundCart = cartRepository.findByCustomerId(customerId);

        // then
        assertThat(foundCart).isPresent();
        assertThat(foundCart.get().getCustomerId()).isEqualTo(customerId);
    }

    @Test
    @DisplayName("CartRepository - 사용자 ID로 장바구니 존재 여부 확인 테스트")
    void existsByCustomerId_Cart() {
        // given
        UUID customerId = UUID.randomUUID();
        CartEntity cartEntity = new CartEntity(customerId);
        entityManager.persist(cartEntity);
        entityManager.flush();

        // when
        boolean exists = cartRepository.existsByCustomerId(customerId);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("CartItemRepository - 장바구니 ID로 장바구니 아이템 목록 찾기 테스트")
    void findByCartId_CartItem() {
        // given
        CartEntity cartEntity = new CartEntity(UUID.randomUUID());
        entityManager.persist(cartEntity);

        CartItemEntity item1 = new CartItemEntity(null, cartEntity.getId(), UUID.randomUUID(), 1, 1000);
        CartItemEntity item2 = new CartItemEntity(null, cartEntity.getId(), UUID.randomUUID(), 2, 2000);
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();

        // when
        List<CartItemEntity> foundItems = cartItemRepository.findByCartId(cartEntity.getId());

        // then
        assertThat(foundItems).hasSize(2);
    }

    @Test
    @DisplayName("CartItemRepository - 장바구니 ID로 장바구니 아이템 삭제 테스트")
    void deleteAllByCartId_CartItem() {
        // given
        CartEntity cartEntity = new CartEntity(UUID.randomUUID());
        entityManager.persist(cartEntity);

        CartItemEntity item1 = new CartItemEntity(null, cartEntity.getId(), UUID.randomUUID(), 1, 1000);
        entityManager.persist(item1);
        entityManager.flush();

        // when
        cartItemRepository.deleteAllByCartId(cartEntity.getId());
        entityManager.flush();

        // then
        List<CartItemEntity> foundItems = cartItemRepository.findByCartId(cartEntity.getId());
        assertThat(foundItems).isEmpty();
    }

    @Test
    @DisplayName("CartItemRepository - 상품 ID로 장바구니 아이템 목록 찾기 테스트")
    void findByProductId_CartItem() {
        // given
        UUID productId = UUID.randomUUID();
        CartEntity cartEntity = new CartEntity(UUID.randomUUID());
        entityManager.persist(cartEntity);

        CartItemEntity item1 = new CartItemEntity(null, cartEntity.getId(), productId, 1, 1000);
        entityManager.persist(item1);
        entityManager.flush();

        // when
        List<CartItemEntity> foundItems = cartItemRepository.findByProductId(productId);

        // then
        assertThat(foundItems).hasSize(1);
    }

    @Test
    @DisplayName("CartItemRepository - 상품 ID와 장바구니 ID로 장바구니 아이템 목록 찾기 테스트")
    void findByProductIdAndCartId_CartItem() {
        // given
        UUID productId = UUID.randomUUID();
        CartEntity cartEntity = new CartEntity(UUID.randomUUID());
        entityManager.persist(cartEntity);

        CartItemEntity item1 = new CartItemEntity(null, cartEntity.getId(), productId, 1, 1000);
        entityManager.persist(item1);
        entityManager.flush();

        // when
        List<CartItemEntity> foundItems = cartItemRepository.findByProductIdAndCartId(productId, cartEntity.getId());

        // then
        assertThat(foundItems).hasSize(1);
    }
}