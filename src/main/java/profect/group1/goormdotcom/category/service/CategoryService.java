package profect.group1.goormdotcom.category.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import profect.group1.goormdotcom.category.service.CategoryTree.CategoryTreeHolder;
import profect.group1.goormdotcom.category.service.CategoryTree.CategoryTree;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryTreeHolder categoryHolder;

    // Get all category data as a tree
    public CategoryTree getCategoryTree() {
        return categoryHolder.get();
    }
}
