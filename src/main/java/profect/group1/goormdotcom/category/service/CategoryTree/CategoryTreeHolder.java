package profect.group1.goormdotcom.category.service.CategoryTree;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

@Component
public class CategoryTreeHolder {
    private final AtomicReference<CategoryTree> ref = new AtomicReference<>();

    public CategoryTree get() {
        return ref.get();
    }

    public void refresh(CategoryTree newTree) {
        ref.set(newTree);
    }
}

