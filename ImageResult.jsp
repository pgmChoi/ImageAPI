<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.List, NData.*, java.net.URLEncoder" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Image View</title>
<link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
<%
 request.setCharacterEncoding("UTF-8");

 String keyword = request.getParameter("_keyword");//검색어
 String pageStr = request.getParameter("_page");//현재 페이지 번호
 String mode = request.getParameter("_mode");// 검색 버튼인지 저장 버튼인지 구별
 
 //페이지 번호가 없으면 1페이지로, 있으면 숫자로 변환
 int currentPage = (pageStr == null) ? 1 : Integer.parseInt(pageStr);
 int pageSize = 10; // 한 페이지에 보여줄 개수
 List<NaverValues> resultList = null; // API로 받아온 데이터 저장소 선언
 
 // 검색어가 있을 때만 실행
 if(keyword != null && !keyword.trim().isEmpty()){
	 
	 //DB객체 생성
	 NaverAPI_DB nad = new NaverAPI_DB();
	//api 호출
	resultList = nad.searchNaverImages(keyword);
	
	// 사용자가 누른 버튼의 value가 'save'일 때만 실행
	if("save".equals(mode)){
		//DB에 저장
		nad.saveDB(keyword, resultList);
		//저장 후 알람창
		out.println("<script>alert('DB저장 완료!');</script>");
	}
 }
 
 
 %>
 <!-- 검색창 영역 , required(공백이면 경고창이 뜸) -->
 <div class="search-box">
     <form action="ImageResult.jsp" method="get">
         상품명: <input type="text" name="_keyword" value="<%= (keyword != null) ? keyword : "" %>" required>
         <button type="submit" name="_mode" value="search">검색</button>
         <button type="submit" name="_mode" value="save">저장</button>
     </form>
 </div>
 
 
 	
  <%// 본격적인 table로 API로 받아온 데이터 보여주기
  	if(resultList != null && resultList.size() > 0) { 
	  // 페이징 계산 
      // 예: 1페이지면 0~10, 2페이지면 10~20
      int totalCount = resultList.size(); // 100개
      
      // 시작 인덱스: (1페이지-1)*10 = 0, (2페이지-1)*10 = 10 ...
      int startRow = (currentPage - 1) * pageSize;
   // 끝 인덱스: 0+10=10, 10+10=20 ... (단, 전체 개수를 넘지 않게 Math.min 사용)
		   //없는 번호를 찾지 않게 막아주는 브레이크 역할
      int endRow = Math.min(startRow + pageSize, totalCount);
  %>
  	<p>
  		<b>'<%= keyword %>'</b> 검색 결과 : 총 <%=totalCount %>건 중
  		<%= startRow + 1 %> ~ <%= endRow %>번째 항목 
  	</p>
  	
  	<table>
  		<tr>
  			<th width="50">순위</th>
  			<th>Title</th>
  			<th width="50">Link</th>
  			<th width="120">Image</th>
  		</tr>
  		<tbody>
  			
  			<%// 3.  계산된 범위(startRow ~ endRow)만큼만 반복 출력
  			for(int i = startRow; i < endRow; i++) { 
  				//API에서 받아온 배열을 item에 넣기
  				NaverValues item = resultList.get(i);
  			%>
  				<tr> <!-- item에 있는 NaverValues타입 값을 get으로 가져와서 화면에 출력 -->
  					<td><%=item.getRank() %></td>
  					<td style="text-align: left;"><%= item.getTitle() %></td>
  					<td><a href="<%= item.getLink() %>" target="_blank">Link</a></td>
  					<td><img src="<%= item.getThumbnail() %>" alt="사진"></td>
  				</tr>
  			<%} %>
  		</tbody>
  	</table>
  	<div class="pagination">
  	<%
  		// 전체 페이지 수 계산 (올림 처리) 
  		//예를 들어 : 83개의 데이터를 가져왔는데 Math.ceil을 쓰면 올림처리되서 9페이지까지 보여줌(python round 같은 느낌?)
  		int totalPage = (int)Math.ceil((double)totalCount / pageSize);
  	
  		for(int i=1; i<=totalPage;i++){
  		// activeClass는 CSS 사용하기 위해서, safeKeyword는 페이지를 넘길 때 keyword도 같이 넘길 때 한국어 안깨지려고
  			String activeClass = (i == currentPage) ? "class='active'" : ""; 
  			String safeKeyword = URLEncoder.encode(keyword, "UTF-8");
  			%>
  			
  		
  			<a href="ImageResult.jsp?_keyword=<%=safeKeyword %>&_page=<%=i %>&_mode=search" <%=activeClass %>>
  			<%=i %>
  			</a>
  	<% } %>
  	</div>
  <%} else if(keyword != null) out.println("<p>검색 결과가 없습니다.</p>"); %>


</body>
</html>