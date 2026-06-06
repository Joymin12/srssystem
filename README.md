# SRS 강의실 예약 시스템

결과보고서 HWP/PDF의 상세 설계 대상 유스케이스를 기준으로 구현한 Java 21 서버/클라이언트 프로그램입니다.

상세 구현 기준:

- UC-01 로그인
- UC-02 강의실 및 강의 현황 조회
- UC-03 빈 강의실 조회
- UC-04 강의실 예약 신청
- UC-05 내 예약 목록 및 상세 조회
- UC-06 예약 취소
- UC-07 예약 승인 및 거부
- UC-08 예약 결과 알림 확인
- UC-09 강의실 및 강의 시간 관리
- UC-10 백업 및 복구

## 실행

서버:

```bash
cd srs-server
mvn test
java -cp target/classes com.se03.Main
```

클라이언트:

```bash
cd srs-client
mvn test
java -cp target/classes com.se03.Main
```

## 기본 계정

| 역할 | 아이디 | 비밀번호 |
| --- | --- | --- |
| 학생 | `student1` | `1234` |
| 학생 | `student2` | `1234` |
| 교수 | `prof1` | `1234` |
| 조교 | `assistant1` | `1234` |

## 결과보고서 API

- `GET /rooms/{roomId}/status?buildingId={buildingId}&viewType={viewType}`
- `GET /rooms/available?buildingId={buildingId}&date={date}&dayOfWeek={dayOfWeek}&startPeriod={startPeriod}&endPeriod={endPeriod}`
- `POST /reservations`
- `GET /reservations?userId={userId}`
- `GET /reservations/{reservationId}`
- `POST /reservations/{reservationId}/cancel`
- `POST /reservations/{reservationId}/force-cancel`
- `GET /reservations/pending`
- `POST /reservations/{reservationId}/approve`
- `POST /reservations/{reservationId}/reject`
- `GET /notifications?userId={userId}`
- `POST /notifications/{notificationId}/read`
- `GET /admin/classrooms`
- `POST /admin/classrooms`
- `DELETE /admin/classrooms/{roomId}`
- `GET /admin/schedules`
- `POST /admin/schedules`
- `DELETE /admin/schedules/{scheduleId}`

- `POST /login`
- `POST /backup`
- `POST /restore`

## 서버 구조

일반적인 계층형 패키지로 구성했습니다.

- `Main`: 서버 조립 및 HTTP 라우팅 시작점
- `model`: `Reservation`, `Classroom`, `User`, `Notification` 등 도메인 데이터와 결과 객체
- `service`: 예약, 강의실 현황, 관리, 알림, 로그인 유스케이스 로직
- `repository`: Repository 인터페이스와 파일 기반 구현
- `controller`: HTTP 요청 파라미터를 서비스 호출로 변환
- `handler`: URL/HTTP 메서드별 요청 분기
- `support`: HTTP 공통 유틸리티

## 클라이언트 구조

- `api`: `ApiClient`, `RoomApi`, `ReservationApi`, `ApprovalApi`
- `panel`: `RoomStatusView`, `AvailableRoomView`, `ReservationFormView`, `ReservationListView`, `ApprovalView`
- `panel`: `NotificationView`, `ManagementView`, `ForceCancelView`, `BackupView`
- `theme`: 어두운 관리 콘솔 스타일 UI 테마
- `app`: 세션과 메인 프레임

## 검증

- `srs-server`: JUnit5 테스트 10개 통과
- `srs-client`: Maven 빌드 통과
- 결과보고서 주요 API 경로 수동 검증 완료
