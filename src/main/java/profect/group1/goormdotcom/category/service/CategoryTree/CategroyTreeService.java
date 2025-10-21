package profect.group1.goormdotcom.category.service.CategoryTree;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import profect.group1.goormdotcom.category.repository.CategoryRepository;
import profect.group1.goormdotcom.category.repository.entity.CategoryEntity;

@Service
@Transactional
@RequiredArgsConstructor
public class CategroyTreeService {
    private final CategoryRepository categoryRepository;

    private static final UUID ROOT_ID =
        UUID.fromString("00000000-0000-0000-0000-000000000000");


    public CategoryTree getCategoryTree() {
        List<CategoryEntity> entities = categoryRepository.findAll();

        // Build tree
        Map<UUID, CategoryNode> map = new LinkedHashMap<>(entities.size());
        for (CategoryEntity e : entities) {
            map.put(e.getId(), CategoryNode.of(e.getId(), e.getParentId(),e.getName()));
        }
        
        map.computeIfAbsent(ROOT_ID, id -> CategoryNode.of(ROOT_ID, null, "ROOT"));

        CategoryNode root = map.get(ROOT_ID);
        if (root == null) throw new IllegalStateException("super_root not found!");

        for (CategoryNode n : map.values()) {
            if (n.id().equals(ROOT_ID)) continue;
             
            CategoryNode p = map.get(n.parentId());
            p.children().add(n);
        } 

        CategoryTree categoryTree = new CategoryTree(root, map);
        return categoryTree;
    }

}
