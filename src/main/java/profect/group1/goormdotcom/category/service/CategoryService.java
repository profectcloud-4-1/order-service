package profect.group1.goormdotcom.category.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import profect.group1.goormdotcom.category.service.CategoryTree.CategoryTree;
import profect.group1.goormdotcom.category.service.CategoryTree.CategroyTreeService;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategroyTreeService categoryTreeService;

    // Get all category data as a tree
    public CategoryTree getCategoryTree() {
        return categoryTreeService.getCategoryTree();
    }
}
