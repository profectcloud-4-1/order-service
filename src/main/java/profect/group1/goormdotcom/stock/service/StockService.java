package profect.group1.goormdotcom.stock.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import profect.group1.goormdotcom.stock.domain.Stock;
import profect.group1.goormdotcom.stock.repository.StockRepository;
import profect.group1.goormdotcom.stock.repository.entity.StockEntity;
import profect.group1.goormdotcom.stock.repository.mapper.StockMapper;

@Service
@RequiredArgsConstructor
public class StockService {
    
    private final StockRepository stockRepository;

    public Stock registerStock(UUID productId, int stockQuantity) {
        UUID id = UUID.randomUUID();
        StockEntity entity = new StockEntity(id, productId, stockQuantity);
        stockRepository.save(entity);
        return StockMapper.toDomain(entity);
    }
    
    public Stock updateStock(UUID productId, int stockQuantity) {
        StockEntity entity = stockRepository.findByProductId(productId);
        entity.updateQuantity(stockQuantity);
        stockRepository.save(entity);
        return StockMapper.toDomain(entity);
    }

    public Stock getStock(UUID productId) {
        StockEntity entity = stockRepository.findByProductId(productId);
        return StockMapper.toDomain(entity);
    }

    // TODO: 동시성 구현
    public void decreaseStock(UUID productId) {
        
        
    }

    public void increaseStock(UUID productId) {
        
    }
}
