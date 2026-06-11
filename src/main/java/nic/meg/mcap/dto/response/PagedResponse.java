package nic.meg.mcap.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
	private List<T> data;
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean last;
}
