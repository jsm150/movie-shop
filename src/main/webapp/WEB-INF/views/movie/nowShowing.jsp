<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>현재 상영작 - CINEMA PRO</title>
    <!-- FontAwesome & Google Fonts -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700;900&display=swap" rel="stylesheet">

    <style>
        /* --- 변수 및 리셋 --- */
        :root {
            --primary-color: #e50914;
            --primary-hover: #b20710;
            --bg-color: #141414;
            --card-bg: #181818;
            --text-color: #ffffff;
            --text-sub: #b3b3b3;

            /* 관람 등급 컬러 (DB Enum과 매칭) */
            --age-ALL: #2da771;  /* 전체 */
            --age-PG12: #e8b928; /* 12세 */
            --age-PG15: #dd7430; /* 15세 */
            --age-R18: #d93025;  /* 청불 */
        }

        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Noto Sans KR', sans-serif;
            background-color: var(--bg-color);
            color: var(--text-color);
            overflow-x: hidden;
        }

        a { text-decoration: none; color: inherit; }
        button { border: none; background: none; cursor: pointer; font-family: inherit; }

        /* --- 헤더 --- */
        header {
            position: sticky;
            top: 0;
            z-index: 100;
            padding: 15px 50px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: rgba(20, 20, 20, 0.95);
            backdrop-filter: blur(10px);
            border-bottom: 1px solid rgba(255,255,255,0.1);
        }

        .logo {
            font-size: 26px;
            font-weight: 900;
            color: var(--primary-color);
            letter-spacing: 1px;
        }

        .nav-links { display: flex; gap: 40px; }
        .nav-links a {
            font-size: 15px;
            font-weight: 500;
            color: var(--text-sub);
            transition: 0.3s;
        }
        .nav-links a:hover, .nav-links a.active { color: white; font-weight: 700; }

        .user-menu { display: flex; gap: 20px; align-items: center; font-size: 18px; }

        /* --- 페이지 타이틀 섹션 --- */
        .page-header {
            padding: 60px 50px 30px;
            max-width: 1400px;
            margin: 0 auto;
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
            border-bottom: 1px solid rgba(255,255,255,0.1);
        }

        .title-group h2 {
            font-size: 36px;
            font-weight: 700;
            margin-bottom: 10px;
        }

        .title-group p {
            color: var(--text-sub);
            font-size: 14px;
        }

        /* --- 정렬 탭 --- */
        .sort-tabs {
            display: flex;
            background: #222;
            padding: 4px;
            border-radius: 30px;
        }

        .sort-tab {
            padding: 8px 20px;
            font-size: 14px;
            color: var(--text-sub);
            border-radius: 25px;
            transition: 0.3s;
            font-weight: 500;
        }

        .sort-tab.active {
            background-color: var(--text-color);
            color: var(--bg-color);
            font-weight: 700;
            box-shadow: 0 4px 10px rgba(0,0,0,0.3);
        }

        .sort-tab:hover:not(.active) { color: white; }

        /* --- 영화 리스트 컨테이너 --- */
        .container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 50px;
            min-height: 60vh;
        }

        .movie-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
            gap: 40px 30px;
        }

        /* --- 영화 카드 디자인 --- */
        .movie-card {
            background-color: transparent;
            perspective: 1000px;
        }

        .poster-frame {
            position: relative;
            border-radius: 12px;
            overflow: hidden;
            aspect-ratio: 2/3;
            box-shadow: 0 10px 30px rgba(0,0,0,0.5);
            transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            margin-bottom: 20px;
            background-color: #333; /* 이미지 로딩 전 배경색 */
        }

        .movie-card:hover .poster-frame {
            transform: translateY(-10px);
            box-shadow: 0 20px 40px rgba(0,0,0,0.7);
        }

        .poster-frame img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        /* 순위 숫자 */
        .rank-num {
            position: absolute;
            bottom: -15px;
            left: 10px;
            font-size: 60px;
            font-weight: 900;
            font-style: italic;
            color: white;
            text-shadow: 2px 2px 10px rgba(0,0,0,0.8);
            line-height: 1;
            z-index: 2;
        }

        /* 오버레이 */
        .poster-overlay {
            position: absolute;
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(0,0,0,0.7);
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            opacity: 0;
            transition: opacity 0.3s;
            backdrop-filter: blur(2px);
            gap: 15px;
        }

        .movie-card:hover .poster-overlay { opacity: 1; }

        .btn-overlay {
            display: inline-block; /* a 태그 적용 위해 변경 */
            text-align: center;
            padding: 12px 30px;
            border-radius: 4px;
            font-weight: bold;
            font-size: 14px;
            text-transform: uppercase;
            transition: 0.2s;
            width: 70%;
        }

        .btn-book {
            background-color: var(--primary-color);
            color: white;
        }
        .btn-book:hover { background-color: var(--primary-hover); transform: scale(1.05); }

        .btn-detail {
            border: 1px solid white;
            color: white;
        }
        .btn-detail:hover { background-color: white; color: black; transform: scale(1.05); }

        /* 관람 등급 뱃지 (DB 데이터와 매핑) */
        .age-badge {
            position: absolute;
            top: 10px;
            right: 10px;
            width: 28px;
            height: 28px;
            border-radius: 6px;
            display: flex;
            justify-content: center;
            align-items: center;
            font-size: 11px;
            font-weight: 800;
            color: white;
            z-index: 3;
            box-shadow: 0 2px 5px rgba(0,0,0,0.3);
            text-shadow: 0 0 2px rgba(0,0,0,0.5);
        }
        /* Enum Value에 맞춰 클래스명 사용 */
        .age-PG12 { background-color: var(--age-PG12); }
        .age-PG15 { background-color: var(--age-PG15); }
        .age-R18  { background-color: var(--age-R18); }
        .age-ALL  { background-color: var(--age-ALL); }

        /* 하단 정보 */
        .movie-info { padding: 0 5px; }

        .movie-title {
            font-size: 18px;
            font-weight: 700;
            margin-bottom: 8px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            padding-left: 20px;
        }

        .movie-meta {
            display: flex;
            align-items: center;
            gap: 12px;
            font-size: 13px;
            color: var(--text-sub);
            padding-left: 20px;
        }

        .meta-divider { width: 1px; height: 10px; background: #444; }
        .highlight-rate { color: var(--primary-color); font-weight: 600; }

        /* Empty State */
        .empty-state {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 400px;
            text-align: center;
            border: 1px dashed #333;
            border-radius: 20px;
            background: rgba(255,255,255,0.02);
            color: var(--text-sub);
        }
        .empty-icon { font-size: 60px; color: #333; margin-bottom: 20px; }
        .btn-refresh { padding: 10px 25px; background: #333; color: white; border-radius: 30px; margin-top: 20px; }
        .btn-refresh:hover { background: #555; }

        /* 모바일 반응형 */
        @media (max-width: 768px) {
            header { padding: 15px 20px; }
            .nav-links { display: none; }
            .page-header { padding: 30px 20px; flex-direction: column; align-items: flex-start; gap: 20px; }
            .container { padding: 20px; }
            .movie-grid { grid-template-columns: repeat(2, 1fr); gap: 20px; }
        }
    </style>
</head>
<body>

<!-- 헤더 -->
<header>
    <a href="/" class="logo">CINEMA PRO</a>
    <nav class="nav-links">
        <a href="/movies/now-showing" class="active">영화</a>
        <a href="/booking">예매</a>
        <a href="#">스토어</a>
        <a href="#">이벤트</a>
        <a href="#">멤버십</a>
    </nav>
    <div class="user-menu">
        <i class="fas fa-search"></i>
        <i class="fas fa-user-circle"></i>
    </div>
</header>

<!-- 페이지 타이틀 & 정렬 필터 -->
<div class="page-header">
    <div class="title-group">
        <h2>현재 상영작</h2>
        <p>현재 극장에서 뜨거운 사랑을 받고 있는 영화들을 만나보세요.</p>
    </div>

    <div class="sort-tabs">
        <a href="/movies/now-showing?sort=booking" class="sort-tab ${currentSort eq 'booking' or empty currentSort ? 'active' : ''}">예매율순</a>
        <a href="/movies/now-showing?sort=audience" class="sort-tab ${currentSort eq 'audience' ? 'active' : ''}">박스오피스</a>
        <a href="/movies/now-showing?sort=rating" class="sort-tab ${currentSort eq 'rating' ? 'active' : ''}">평점순</a>
        <a href="/movies/now-showing?sort=latest" class="sort-tab ${currentSort eq 'latest' ? 'active' : ''}">최신개봉작</a>
    </div>
</div>

<div class="container">

    <!-- 대안 흐름: 영화가 없을 때 -->
    <c:if test="${empty movies}">
        <div class="empty-state">
            <i class="fas fa-film empty-icon"></i>
            <h3>현재 상영 중인 영화가 없습니다</h3>
            <p>새로운 영화 정보를 불러오거나 잠시 후 다시 시도해 주세요.</p>
            <button class="btn-refresh" onclick="location.reload()"><i class="fas fa-sync-alt"></i> 새로고침</button>
        </div>
    </c:if>

    <!-- 기본 흐름: 영화 목록 그리드 -->
    <c:if test="${not empty movies}">
        <div class="movie-grid">
            <c:forEach var="movie" items="${movies}" varStatus="status">
                <!-- 영화 카드 -->
                <div class="movie-card">
                    <div class="poster-frame">
                        <!-- 관람 등급 (DB: PG12 -> CSS: age-PG12) -->
                        <span class="age-badge age-${movie.audienceRating}">
                                <c:choose>
                                    <c:when test="${movie.audienceRating eq 'R18'}">19</c:when>
                                    <c:when test="${movie.audienceRating eq 'PG15'}">15</c:when>
                                    <c:when test="${movie.audienceRating eq 'PG12'}">12</c:when>
                                    <c:otherwise>ALL</c:otherwise>
                                </c:choose>
                            </span>

                        <!-- 순위 (1, 2, 3...) -->
                        <span class="rank-num">${status.count}</span>

                        <!-- 포스터 이미지 -->
                        <img src="${movie.posterImageUrl}" alt="${movie.title} 포스터" onerror="this.src='/images/no-poster.png'">

                        <!-- 호버 오버레이 버튼 -->
                        <div class="poster-overlay">
                            <a href="/booking?movieId=${movie.movieId}" class="btn-overlay btn-book">예매하기</a>
                            <a href="/movies/detail/${movie.movieId}" class="btn-overlay btn-detail">상세정보</a>
                        </div>
                    </div>

                    <!-- 하단 정보 -->
                    <div class="movie-info">
                        <h3 class="movie-title">${movie.title}</h3>
                        <div class="movie-meta">
                                <span class="highlight-rate">
                                    예매율 <fmt:formatNumber value="${movie.bookingRate}" pattern="#,##0.0"/>%
                                </span>
                            <span class="meta-divider"></span>
                            <span>
                                    <i class="fas fa-star" style="color:#e8b928"></i>
                                    <fmt:formatNumber value="${movie.averageRating}" pattern="#,##0.0"/>
                                </span>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:if>

</div>

<!-- 푸터 -->
<footer style="text-align: center; padding: 40px; color: #555; border-top: 1px solid #222; margin-top: 50px;">
    <p>&copy; 2023 CINEMA PRO Corporation. All Rights Reserved.</p>
</footer>

<script>
    // 정렬 탭은 이제 실제 링크로 동작합니다
</script>
</body>
</html>