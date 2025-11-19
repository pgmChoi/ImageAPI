package NData;

public class NaverValues {
	private int rank;
	private String title;
	private String link;
	private String thumbnail;

	// 생성자 생성(네이버 API 이미지에서 가져온 값 세팅)
	public NaverValues(int rank, String title, String link, String thumbnail) {
		this.rank = rank;
		this.title = title;
		this.link = link;
		this.thumbnail = thumbnail;
	}
	//  getter, setter로 네이버로 받아온 값 접근
	public int getRank() { return rank;}
	public String getTitle() { return title;}
	public String getLink() { return link;}
	public String getThumbnail() { return thumbnail;}
}
