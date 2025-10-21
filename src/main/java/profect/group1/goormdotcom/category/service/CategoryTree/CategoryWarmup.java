package profect.group1.goormdotcom.category.service.CategoryTree;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import profect.group1.goormdotcom.category.service.CategoryService;

@Component
@RequiredArgsConstructor
public class CategoryWarmup implements ApplicationRunner{
    private final CategoryService categoryService;
    private final CategoryTreeHolder holder;

    // 스프링의 시동 버튼 - 건들지말자.
    // redis에 적재하자.
    @Override
    public void run(ApplicationArguments args) {
        holder.refresh(categoryService.getCategoryTree());
        System.out.println("[CategoryWarmup] CategoryTree preloaded into memory");
    }
    
}

