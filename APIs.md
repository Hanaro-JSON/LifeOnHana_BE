# Life On Hana APIs

<details>
<summary>Account API</summary>

## Account API
Account related APIs for managing bank accounts.

### <span style="color: green;">GET</span> Get Account List
```
/api/account
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "계좌 목록 조회 성공",
    "data": {
        "accounts": [...]
    }
}
```

### <span style="color: green;">GET</span> Get Salary Account
```
/api/account/salary
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "월급 통장 조회 성공",
    "data": {
        "account": {...}
    }
}
```

### <span style="color: #FF8C00;">POST</span> Transfer Money
```
/api/account/transfer
```
**Request Body**
```json
{
    "fromAccount": "string",
    "toAccount": "string",
    "amount": "number"
}
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "이체가 완료되었습니다.",
    "data": {...}
}
```
</details>

<details>
<summary>Article API</summary>

## Article API
Article related APIs for managing content articles.

### <span style="color: green;">GET</span> Get Article Details
```
/api/articles/{articleId}
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "기사 상세 조회 성공",
    "data": {...}
}
```

### <span style="color: green;">GET</span> Get Article List
```
/api/articles?category={category}&page={page}&size={size}
```
**Query Parameters**
- category (optional)
- page (default: 1)
- size (default: 20)

**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "기사 목록 조회 성공",
    "data": {
        "articles": [...],
        "hasNext": boolean
    }
}
```

### <span style="color: green;">GET</span> Search Articles
```
/api/articles/search?query={query}&page={page}&size={size}
```
**Query Parameters**
- query (optional)
- page (default: 0)
- size (default: 20)

**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "기사 검색 성공",
    "data": {
        "articles": [...],
        "hasNext": boolean
    }
}
```
</details>

<details>
<summary>Article Like API</summary>

## Article Like API
APIs for managing article likes and interactions.

### <span style="color: #FF8C00;">POST</span> Toggle Article Like
```
/api/articles/{articleId}/like
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "좋아요 성공/취소 성공",
    "data": {
        "isLiked": boolean,
        "likeCount": number
    }
}
```

### <span style="color: green;">GET</span> Get Article Like Info
```
/api/articles/{articleId}/like
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "게시글 좋아요 정보 조회 성공",
    "data": {...}
}
```

### <span style="color: green;">GET</span> Get Liked Articles
```
/api/articles/liked?page={page}&size={size}&category={category}
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "좋아요한 기사 목록 조회 성공",
    "data": {
        "articles": [...],
        "hasNext": boolean
    }
}
```
</details>

<details>
<summary>Auth API</summary>

## Auth API
Authentication related APIs.

### <span style="color: #FF8C00;">POST</span> Sign In
```
/api/auth/signin
```
**Request Body**
```json
{
    "username": "string",
    "password": "string"
}
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "로그인 성공",
    "data": {
        "accessToken": "string",
        "refreshToken": "string"
    }
}
```

### <span style="color: #FF8C00;">POST</span> Refresh Token
```
/api/auth/refresh
```
**Request Body**
```json
{
    "refreshToken": "string"
}
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "토큰 갱신 성공",
    "data": {
        "accessToken": "string",
        "refreshToken": "string"
    }
}
```

### <span style="color: #FF8C00;">POST</span> Sign Out
```
/api/auth/signout
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "로그아웃 성공",
    "data": null
}
```
</details>

<details>
<summary>History API</summary>

## History API
Transaction history related APIs.

### <span style="color: green;">GET</span> Get Transaction History
```
/api/history?yearMonth={yearMonth}&page={page}&size={size}
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "거래 내역 조회 성공",
    "data": {...}
}
```

### <span style="color: green;">GET</span> Get Monthly Expenses
```
/api/history/monthly
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "월별 지출 내역 조회 성공",
    "data": {...}
}
```

### <span style="color: green;">GET</span> Get Statistics
```
/api/history/statistics?yearMonth={yearMonth}
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "거래 통계 조회 성공",
    "data": {...}
}
```
</details>

<details>
<summary>Loan API</summary>

## Loan API
Loan recommendation and related APIs.

### <span style="color: #FF8C00;">POST</span> Get Loan Recommendations
```
/api/anthropic/loans
```
**Request Body**
```json
{
    "reason": "string",
    "amount": "number"
}
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "대출 상품 추천 성공",
    "data": [...]
}
```
</details>

<details>
<summary>Lump Sum API</summary>

## Lump Sum API
APIs for managing lump sum withdrawals.

### <span style="color: #FF8C00;">POST</span> Request Lump Sum Withdrawal
```
/api/lumpsum
```
**Request Body**
```json
{
    "amount": "number",
    "purpose": "string"
}
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "목돈 인출 신청 성공",
    "data": {
        "lumpSumId": "string",
        "balance": "number",
        "requestedAt": "string"
    }
}
```
</details>

<details>
<summary>Product API</summary>

## Product API
Financial product related APIs.

### <span style="color: green;">GET</span> Get Products List
```
/api/products?category={category}&offset={offset}&limit={limit}
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "상품 목록 조회 성공",
    "data": {...}
}
```

### <span style="color: green;">GET</span> Get Savings Product Details
```
/api/products/savings/{productId}
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "예적금 상품 상세 조회 성공",
    "data": {...}
}
```

### <span style="color: green;">GET</span> Get Loan Product Details
```
/api/products/loans/{productId}
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "대출 상품 상세 조회 성공",
    "data": {...}
}
```

### <span style="color: green;">GET</span> Get Life Product Details
```
/api/products/life/{productId}
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "라이프 상품 상세 조회 성공",
    "data": {...}
}
```
</details>

<details>
<summary>Product Insight API</summary>

## Product Insight API
Product analysis and insight APIs.

### <span style="color: #FF8C00;">POST</span> Get Product Analysis
```
/api/anthropic/effect
```
**Request Body**
```json
{
    "productId": "number",
    "productType": "string"
}
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "상품 분석 성공",
    "data": {...}
}
```
</details>

<details>
<summary>Product Like API</summary>

## Product Like API
APIs for managing product likes.

### <span style="color: green;">GET</span> Get Liked Products
```
/api/users/liked/products
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "좋아요한 상품 목록 조회 성공",
    "data": {...}
}
```

### <span style="color: #FF8C00;">POST</span> Toggle Product Like
```
/api/users/{productId}/like
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "좋아요 성공/취소 성공",
    "data": {
        "isLiked": boolean
    }
}
```
</details>

<details>
<summary>User API</summary>

## User API
User information related APIs.

### <span style="color: green;">GET</span> Get User Info
```
/api/users/info
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "사용자 정보 조회 성공",
    "data": {...}
}
```

### <span style="color: green;">GET</span> Get MyData
```
/api/users/mydata
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "마이데이터 조회 성공",
    "data": {...}
}
```

### <span style="color: green;">GET</span> Get User Nickname
```
/api/users/nickname
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "사용자 칭호 조회 성공",
    "data": {...}
}
```
</details>

<details>
<summary>Wallet API</summary>

## Wallet API
Digital wallet management APIs.

### <span style="color: #FF8C00;">POST</span> Create Wallet
```
/api/wallet
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "하나지갑 정보 등록 성공",
    "data": {...}
}
```

### <span style="color: green;">GET</span> Get Wallet Info
```
/api/wallet
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "하나지갑 정보 조회 성공",
    "data": {...}
}
```

### <span style="color: #FF8C00;">PUT</span> Update Wallet
```
/api/wallet
```
**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "하나지갑 정보 수정 성공",
    "data": {...}
}
```
</details>

<details>
<summary>Whilick API</summary>

## Whilick API
Column shorts related APIs.

### <span style="color: green;">GET</span> Get Shorts
```
/api/articles/shorts
/api/articles/shorts/{articleId}
```
**Query Parameters**
- page (default: 0)
- size (default: 10)

**Response**
```json
{
    "code": 200,
    "status": "OK",
    "message": "컨텐츠 조회 성공",
    "data": {...}
}
```
</details>
