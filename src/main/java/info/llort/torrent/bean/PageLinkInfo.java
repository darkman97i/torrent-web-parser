package info.llort.torrent.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageLinkInfo implements Serializable {
	private String url;
	private String referer;
}
