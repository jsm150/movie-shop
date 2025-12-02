<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CINEMA PRO - 영화 예매</title>
    <!-- 폰트 및 아이콘 로드 -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">

    <style>
        /* --- 기본 설정 --- */
        :root {
            --primary-color: #e50914; /* 브랜드 레드 */
            --bg-color: #141414;      /* 배경색 (다크) */
            --text-color: #ffffff;    /* 텍스트 (화이트) */
            --gray-color: #aaa;
            --card-bg: #1f1f1f;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Noto Sans KR', sans-serif;
            background-color: var(--bg-color);
            color: var(--text-color);
            line-height: 1.6;
        }

        a { text-decoration: none; color: inherit; }
        ul { list-style: none; }

        /* --- 네비게이션 바 --- */
        header {
            position: fixed;
            top: 0;
            width: 100%;
            padding: 20px 50px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: linear-gradient(to bottom, rgba(0,0,0,0.9) 0%, rgba(0,0,0,0) 100%);
            z-index: 1000;
            transition: 0.3s;
        }

        header.scrolled {
            background-color: #000;
        }

        .logo {
            font-size: 28px;
            font-weight: 900;
            color: var(--primary-color);
            letter-spacing: 2px;
        }

        .nav-links {
            display: flex;
            gap: 30px;
        }

        .nav-links a {
            font-weight: 500;
            font-size: 16px;
            transition: color 0.3s;
        }

        .nav-links a:hover {
            color: var(--primary-color);
        }

        .user-actions {
            display: flex;
            gap: 20px;
            align-items: center;
        }

        .btn-login {
            padding: 8px 20px;
            border: 1px solid white;
            border-radius: 4px;
            font-size: 14px;
            transition: 0.3s;
        }

        .btn-login:hover {
            background-color: white;
            color: black;
        }

        /* --- 히어로 섹션 (메인 배너) --- */
        .hero {
            height: 80vh;
            background-image: linear-gradient(rgba(0,0,0,0.3), var(--bg-color)), url('https://images.unsplash.com/photo-1536440136628-849c177e76a1?ixlib=rb-1.2.1&auto=format&fit=crop&w=1920&q=80');
            background-size: cover;
            background-position: center;
            display: flex;
            align-items: center;
            padding: 0 50px;
        }

        .hero-content {
            max-width: 600px;
        }

        .hero-tag {
            background-color: var(--primary-color);
            padding: 5px 10px;
            font-size: 12px;
            font-weight: bold;
            border-radius: 3px;
            margin-bottom: 10px;
            display: inline-block;
        }

        .hero h1 {
            font-size: 60px;
            margin-bottom: 20px;
            line-height: 1.1;
        }

        .hero p {
            font-size: 18px;
            color: #ddd;
            margin-bottom: 30px;
        }

        .btn-primary {
            background-color: var(--primary-color);
            color: white;
            padding: 15px 30px;
            font-size: 18px;
            border-radius: 5px;
            font-weight: bold;
            border: none;
            cursor: pointer;
            transition: transform 0.2s;
            display: inline-flex;
            align-items: center;
            gap: 10px;
        }

        .btn-primary:hover {
            transform: scale(1.05);
            background-color: #f40612;
        }

        /* --- 무비 차트 섹션 --- */
        .section-container {
            padding: 50px;
        }

        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 30px;
        }

        .section-title {
            font-size: 24px;
            font-weight: 700;
            border-left: 4px solid var(--primary-color);
            padding-left: 15px;
        }

        .view-all {
            color: var(--gray-color);
            font-size: 14px;
        }

        .movie-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
            gap: 25px;
        }

        .movie-card {
            background-color: var(--card-bg);
            border-radius: 8px;
            overflow: hidden;
            transition: transform 0.3s;
            position: relative;
        }

        .movie-card:hover {
            transform: translateY(-10px);
            box-shadow: 0 10px 20px rgba(0,0,0,0.5);
        }

        .poster-area {
            position: relative;
            height: 320px;
            overflow: hidden;
        }

        .poster-area img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .rank-badge {
            position: absolute;
            top: 0;
            left: 0;
            background-color: var(--primary-color);
            width: 40px;
            height: 40px;
            display: flex;
            justify-content: center;
            align-items: center;
            font-weight: bold;
            font-size: 20px;
            box-shadow: 2px 2px 5px rgba(0,0,0,0.3);
        }

        .movie-info {
            padding: 15px;
        }

        .movie-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 5px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .movie-meta {
            display: flex;
            justify-content: space-between;
            font-size: 13px;
            color: var(--gray-color);
            margin-bottom: 15px;
        }

        .rating {
            color: #ffc107; /* 별 색상 */
        }

        .btn-book {
            width: 100%;
            padding: 10px;
            background-color: #333;
            color: white;
            border: 1px solid #555;
            border-radius: 4px;
            cursor: pointer;
            transition: 0.3s;
        }

        .btn-book:hover {
            background-color: var(--primary-color);
            border-color: var(--primary-color);
        }

        /* --- 이벤트/할인 섹션 --- */
        .event-section {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 20px;
            margin-top: 50px;
        }

        .event-banner {
            height: 200px;
            background-color: #333;
            border-radius: 10px;
            display: flex;
            justify-content: center;
            align-items: center;
            font-size: 24px;
            color: #777;
            position: relative;
            overflow: hidden;
        }

        .event-banner img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            opacity: 0.6;
            transition: 0.3s;
        }

        .event-banner:hover img {
            opacity: 1;
            transform: scale(1.05);
        }

        /* --- 푸터 --- */
        footer {
            background-color: #000;
            padding: 50px;
            margin-top: 80px;
            text-align: center;
            color: var(--gray-color);
            font-size: 14px;
        }

        .social-icons {
            margin-bottom: 20px;
        }

        .social-icons i {
            font-size: 24px;
            margin: 0 10px;
            cursor: pointer;
            transition: color 0.3s;
        }

        .social-icons i:hover {
            color: var(--primary-color);
        }

        .footer-links {
            margin-bottom: 20px;
        }

        .footer-links a {
            margin: 0 10px;
        }

        /* --- 반응형 (모바일) --- */
        @media (max-width: 768px) {
            header { padding: 15px 20px; }
            .nav-links { display: none; } /* 모바일에서 메뉴 숨김 (실제 구현시 햄버거 메뉴 필요) */
            .hero { height: 60vh; padding: 0 20px; }
            .hero h1 { font-size: 36px; }
            .section-container { padding: 30px 20px; }
            .event-section { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>

<!-- 헤더 / 네비게이션 -->
<header id="header">
    <a href="#" class="logo">CINEMA PRO</a>
    <nav class="nav-links">
        <a href="/movies/now-showing">영화</a>
        <a href="#">예매</a>
        <a href="#">극장</a>
        <a href="#">이벤트</a>
        <a href="#">스토어</a>
    </nav>
    <div class="user-actions">
        <a href="#"><i class="fas fa-search"></i></a>
        <a href="#" class="btn-login">로그인</a>
    </div>
</header>

<!-- 히어로 섹션 (메인 배너) -->
<section class="hero">
    <div class="hero-content">
        <span class="hero-tag">절찬 상영중</span>
        <h1>인터스텔라: <br>더 비기닝</h1>
        <p>우주의 끝에서 시작된 인류의 마지막 희망.<br>당신의 상상을 뛰어넘는 압도적인 스케일이 펼쳐진다.</p>
        <button class="btn-primary">
            <i class="fas fa-ticket-alt"></i> 예매하기
        </button>
        <button class="btn-primary" style="background: transparent; border: 1px solid white; margin-left: 10px;">
            <i class="fas fa-play"></i> 예고편
        </button>
    </div>
</section>

<!-- 무비 차트 (영화 목록) -->
<div class="section-container">
    <div class="section-header">
        <h2 class="section-title">무비 차트</h2>
        <a href="/movies/now-showing" class="view-all">전체보기 <i class="fas fa-chevron-right"></i></a>
    </div>

    <div class="movie-grid">
        <!-- 영화 카드 1 -->
        <div class="movie-card">
            <div class="poster-area">
                <span class="rank-badge">1</span>
                <!-- 이미지는 예시용 랜덤 이미지입니다 -->
                <img src="https://i.namu.wiki/i/mNTlQ3_7hdy5wuj00UO1Cuf220sm_3arb2LUTiRaF9gzAe3B_FK1-8CdIprkr121vLNdfEV19-ouvaau6IyYSg.webp" alt="영화 포스터">
            </div>
            <div class="movie-info">
                <h3 class="movie-title">주토피아 2</h3>
                <div class="movie-meta">
                    <span class="rating"><i class="fas fa-star"></i> 9.5</span>
                    <span>예매율 32.5%</span>
                </div>
                <button class="btn-book">예매하기</button>
            </div>
        </div>

        <!-- 영화 카드 2 -->
        <div class="movie-card">
            <div class="poster-area">
                <span class="rank-badge">2</span>
                <img src="https://cdn.gardentimes.co.kr/news/photo/202511/3746_4387_4328.jpg" alt="영화 포스터">
            </div>
            <div class="movie-info">
                <h3 class="movie-title">나우 유 씨 미 3</h3>
                <div class="movie-meta">
                    <span class="rating"><i class="fas fa-star"></i> 8.9</span>
                    <span>예매율 21.0%</span>
                </div>
                <button class="btn-book">예매하기</button>
            </div>
        </div>

        <!-- 영화 카드 3 -->
        <div class="movie-card">
            <div class="poster-area">
                <span class="rank-badge">3</span>
                <img src="https://cdn.cgv.co.kr/cgvpomsfilm/Movie/Thumbnail/Poster/030000/30000103/30000103_320.jpg" alt="영화 포스터">
            </div>
            <div class="movie-info">
                <h3 class="movie-title">극장판 체인소 맨: 레제편</h3>
                <div class="movie-meta">
                    <span class="rating"><i class="fas fa-star"></i> 8.5</span>
                    <span>예매율 15.3%</span>
                </div>
                <button class="btn-book">예매하기</button>
            </div>
        </div>

        <!-- 영화 카드 4 -->
        <div class="movie-card">
            <div class="poster-area">
                <span class="rank-badge">4</span>
                <img src="https://muko.kr/files/attach/images/2025/07/03/d83d1dd469909f5ce6fb198473e61e0d.jpg" alt="영화 포스터">
            </div>
            <div class="movie-info">
                <h3 class="movie-title">극장판 귀멸의 칼날: 무한성편</h3>
                <div class="movie-meta">
                    <span class="rating"><i class="fas fa-star"></i> 8.2</span>
                    <span>예매율 9.8%</span>
                </div>
                <button class="btn-book">예매하기</button>
            </div>
        </div>
    </div>

    <!-- 이벤트 섹션 -->
    <h2 class="section-title" style="margin-top: 80px;">이벤트 & 프로모션</h2>
    <div class="event-section">
        <div class="event-banner">
            <img src="https://images.unsplash.com/photo-1485095329183-f07b83a69163?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=60" alt="할인 이벤트">
            <span style="position: absolute; color: white; font-weight: bold; font-size: 24px; text-shadow: 2px 2px 4px rgba(0,0,0,0.8);">팝콘 콤보 50% 할인</span>
        </div>
        <div class="event-banner">
            <img src="https://images.unsplash.com/photo-1517604931442-710c8ef555c9?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=60" alt="제휴 할인">
            <span style="position: absolute; color: white; font-weight: bold; font-size: 24px; text-shadow: 2px 2px 4px rgba(0,0,0,0.8);">VIP 멤버십 혜택</span>
        </div>
    </div>
</div>

<!-- 푸터 -->
<footer>
    <div class="social-icons">
        <i class="fab fa-facebook"></i>
        <i class="fab fa-instagram"></i>
        <i class="fab fa-twitter"></i>
        <i class="fab fa-youtube"></i>
    </div>
    <div class="footer-links">
        <a href="#">이용약관</a>
        <a href="#">개인정보처리방침</a>
        <a href="#">고객센터</a>
        <a href="#">채용정보</a>
    </div>
    <p>&copy; 2025 CINEMA PRO Corporation. All Rights Reserved.</p>
    <p>부산광역시 사상구 백양대로700번길 140</p>
</footer>

<!-- 스크롤 시 헤더 배경색 변경을 위한 간단한 스크립트 -->
<script>
    window.addEventListener('scroll', function() {
        const header = document.getElementById('header');
        if (window.scrollY > 50) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }
    });
</script>
</body>
</html>