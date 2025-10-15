package profect.group1.goormdotcom.user.presentation.dto.request;

import profect.group1.goormdotcom.common.dto.PaginationRequestDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import java.util.List;

@Getter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ListRequestDto extends PaginationRequestDto {
    private String searchField = "name";
    private String filter; // filterField:filterValue,filterField:filterValue,...

    public void setSearchField(String searchField) {
        if (searchField != null && (searchField.equals("name") || searchField.equals("email")))
            this.searchField = searchField;
        else 
            this.searchField = "name";
    }

    public void setFilter(String filter) {
        if (filter != null && !filter.isBlank())
            this.filter = filter;
        else 
            this.filter = null;
        
    }
}