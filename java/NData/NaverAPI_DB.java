package NData;

import java.sql.*;
import java.net.*;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import NData.NaverValues;

public class NaverAPI_DB {
	
	//네이버 개발자 센터에서 발급받은 키
	private static final String CLIENT_ID = "WQfRfJsNk9w69Iy_xJdf";
	private static final String CLIENT_SECRET = "dD0Ac8QkSn";
	
	String text = null;
	//1. 검색어(keyword)를 받아 네이버 API를 호출하고 결과를 파싱하는 메서드
	public List<NaverValues> searchNaverImages(String keyword){
		List<NaverValues> list = new ArrayList<>();
		try {
			//한글은 URL에 그냥 넣으면 깨지므로 UTF-8 등으로 설정
			text = URLEncoder.encode(keyword, "UTF-8");
			// URL로 요청해서 받아올 데이터를 display=100은 한 번에 100개의 결과를 달라는 뜻
			String apiURL = "https://openapi.naver.com/v1/search/image.json?query="+text+"&display=100";
			// 네이버 개발자 센터에서 발급받은 ID와 Secret을 담음 (인증용)
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("X-Naver-Client-Id", CLIENT_ID);
            requestHeaders.put("X-Naver-Client-Secret", CLIENT_SECRET);
			
            // 실제 통신 수행: get() 메서드를 호출해 JSON 문자열을 받아옴
            String jsonResponse = get(apiURL, requestHeaders);
            // console에 네이버로 부터 응답을 확인 가능
            System.out.println("네이버 응답 데이터: " + jsonResponse);
		
            //Gson 라이브러리를 이용한 JSON 파싱
            //전체 문자열을 JSON 객체로 변환
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            // "items" 키값에 해당하는 배열(이미지 목록)을 가져옴
            JsonArray items = jsonObject.get("items").getAsJsonArray();
           
            //배열을 반복하며 필요한 데이터(제목, 링크, 썸네일)만 추출
            for(int i=0; i < items.size();i++) {
            	JsonObject item = items.get(i).getAsJsonObject();
            	
            	String title = item.get("title").getAsString(); //제목 
            	String originalLink = item.get("link").getAsString();//원본 이미지
            	String thumbnail = item.get("thumbnail").getAsString();//썸네일 이미지
            	
            	// 객체에 담아서 리스트에 추가(i 가 0이기에 + 1)
            	list.add(new NaverValues((i+1), title, originalLink, thumbnail));
            }
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	/*
	 	설정된 URL로 실제 접속을 시도하고, 요청(Request)을 보내고, 응답(Response)을 받아오는 역할
	 	apiUrl : 접속할 네이버 API 주소
	 	requestHeaders : 요청 시 같이 보낼 인증 정보 (Client-ID, Client-Secret)
	 	String : 네이버 서버가 보내준 결과물 (JSON 문자열)
	 */
	private String get(String apiUrl, Map<String, String> requestHeaders){
		//URL 주소를 가지고 연결 통로(Connection)를 만듬
        HttpURLConnection con = connect(apiUrl);
        try {
        	//전송 방식 설정
            con.setRequestMethod("GET");
            
            //헤더 설정 (요청 편지 봉투에 인증 도장을 찍는 과정)
            // requestHeaders 맵에 들어있는 ID와 Secret을 하나씩 꺼내서 연결 객체에 심기
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }
            
            //응답 코드 확인 (200번이면 성공, 400/500번대면 에러)
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출(200)
            	// 성공했으므로 정상 데이터 스트림(InputStream)을 읽기
                return readBody(con.getInputStream());
            } else { // 에러 발생(400/500)
            	// 실패했으므로 에러 메시지 스트림(ErrorStream)을 읽기
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }
	
	/*
	 [연결 생성 메서드]
	 문자열로 된 주소(apiUrl)를 자바가 이해하는 URL 객체로 만들고, 연결을 염
	 */
	private static HttpURLConnection connect(String apiUrl){
        try {
        	URL url = new URL(apiUrl);
        	// url.openConnection()은 URLConnection을 리턴하므로, 
            // HTTP 통신을 위해 HttpURLConnection으로 형변환(casting) 해줌.
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }
	/*
	 	[데이터 읽기 메서드]
	 	서버가 보내준 데이터(Stream)를 우리가 읽을 수 있는 문자열(String)로 변환
	 	body : 서버로부터 들어오는 데이터의 흐름 (InputStream)
	 	responseBody.toString() : 한 줄 한 줄 읽어서 합친 전체 결과 문자열
	 */
	private String readBody(InputStream body){
		// InputStream(바이트 단위) -> InputStreamReader(문자 단위) -> BufferedReader(버퍼링 사용)
        // 이렇게 감싸주어야 속도가 빠르고 줄 단위로 읽기 편함.
        InputStreamReader streamReader = new InputStreamReader(body);
        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();
            
            String line;
         // 한 줄씩 읽어서(readLine) 내용이 없을 때(null)까지 반복
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }
            return responseBody.toString(); // 최종 결과 반환
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는 데 실패했습니다.", e);
        }
    }
	
	// 가져온 리스트를 DB에 저장하는 메서드 (중복 방지 로직 포함)
	public void saveDB(String keyword, List<NaverValues> list) {
			PreparedStatement stmt = null;
		try {
			// DB 연결 준비
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection conn = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/naver_image", "root", "rootroot");// DB경로, DB아이디, DB비번
			
			//중복 확인: 이미 해당 키워드로 저장된 데이터가 있는지 확인
			stmt = conn.prepareStatement(
					"select count(*) from search_results where keyword = ?");
			stmt.setString(1, keyword);
			ResultSet rs = stmt.executeQuery();
			
			// 결과가 있다면 개수를 확인
			if(rs.next()) {
				int count = rs.getInt(1); //개수 확인
				if(count > 0) {
					System.out.println("이미 " + keyword + "데이터가 존재하므로 저장을 건너뜀.");
					return; // 데이터가 있으면 여기서 메서드 종료 (저장 안 함)
				}
			}
			rs.close(); stmt.close();
			
			//데이터가 없을 때만 저장 실행(insert)
			stmt = conn.prepareStatement(
					"insert into search_results (keyword, rank_num, title, original_image_url, thumbnail_url) values (?,?,?,?,?)");
			
			// 리스트에 담긴 100개를 반복하며 저장
			for(NaverValues item : list) {
				stmt.setString(1, keyword);
				stmt.setInt(2, item.getRank());
				stmt.setString(3, item.getTitle());
				stmt.setString(4, item.getLink());
				stmt.setString(5, item.getThumbnail());
				
				stmt.executeUpdate();
			}
			//저장 확인
			System.out.println("'" + keyword + "' 검색 결과 " + list.size() + "건 새로 저장 완료");
			
			stmt.close(); conn.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
