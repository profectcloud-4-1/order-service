package profect.group1.goormdotcom.common.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PaginationRequestDto {
    private int page = 1;
    private int size = 10;
    private String sort = "createdAt";
    private String order = "desc";
    private String keyword = "";

    public void setSize(int size) {
        if (size == 10 || size == 30 || size == 50) {
            this.size = size;
        } else {
            this.size = 10;
        }
    }

    public void setPage(int page) {
        this.page = page > 0 ? page : 1;
    }

    public void setSort(String sort) {
        this.sort = (sort == null || sort.isBlank()) ? "createdAt" : sort;
    }

    public void setOrder(String order) {
        this.order = (order == null || order.isBlank()) ? "desc" : order;
    }

    public void setKeyword(String keyword) {
        this.keyword = (keyword == null) ? "" : keyword;
    }
}