# NetCafe Management System

Đồ án Java: hệ thống quản lý quán net. Nhóm: **Phú, Trần Hoàng, Khánh An**.

Viết bằng Java thuần (Swing), không dùng thư viện ngoài, dữ liệu lưu ra file. Chạy được với JDK 8 trở lên.

## Cách chạy

```sh
./build.sh        # Linux / macOS
build.bat         # Windows
```

Hoặc thủ công:

```sh
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out netcafe.Main
```

Dữ liệu được tạo trong thư mục `data/` (`customers.csv`, `sessions.csv`, `config.properties`).

## 1. Yêu cầu đề bài và cách đáp ứng

| Yêu cầu | Chức năng trong chương trình |
|---|---|
| a. Tìm khách hàng có sẵn hoặc thêm khách mới | Ô tìm kiếm theo tên/SĐT (lọc ngay khi gõ), nút **Thêm khách hàng** |
| a. Nhận số tiền, quy đổi ra giờ chơi (tỉ lệ tuỳ chọn) | Nút **Nạp tiền** (số tiền phải là bội số của 1.000 VND, sai thì báo lỗi); tỉ lệ VND/giờ chỉnh ở **Cài đặt tỉ lệ**, lưu trong `config.properties` |
| a. Bắt đầu / kết thúc phiên chơi cho từng khách | Nút **Bắt đầu phiên**, **Kết thúc phiên**; mỗi khách có phiên riêng |
| a. Xoá khách hàng | Nút **Xoá khách hàng** (có xác nhận, không cho xoá khi đang chơi) |
| b. GUI để chọn và thêm thông tin | Cửa sổ Swing: bảng khách hàng + các hộp thoại nhập liệu |
| c. Trừ giờ chơi sau khi kết thúc phiên | `SessionManager.end()` trừ thời gian đã chơi và lưu lại số dư |
| c. Hết giờ thì thông báo và tự kết thúc | Timer 1 giây gọi `checkExpired()`, tự kết thúc phiên và hiện hộp thoại "Hết giờ" |
| c. Lấy giờ bắt đầu/kết thúc ngay khi bấm nút, riêng từng khách | `LocalDateTime.now()` được gọi trong xử lý nút; phiên lưu theo mã khách |
| c. Cập nhật thời gian sau khi kết thúc | Số dư ghi vào `customers.csv`, lịch sử phiên ghi vào `sessions.csv` |
| d. Báo cáo, code compile được | README này + báo cáo Word; `javac` chạy không lỗi |
| e. Slide thuyết trình và demo | Làm ở giai đoạn cuối (xem mục 5) |

## 2. Kiến trúc

Chia 4 tầng, tầng trên chỉ gọi tầng dưới:

```
ui (Swing)            MainFrame
   │
service (nghiệp vụ)   CustomerService, SessionManager, Settings
   │
model (dữ liệu)       Customer, Session
   │
persistence (file)    DataStore  →  data/*.csv, config.properties
```

```
src/netcafe/
├── Main.java                     Khởi tạo các tầng và mở cửa sổ chính
├── model/
│   ├── Customer.java             id, tên, SĐT, số giây còn lại
│   └── Session.java              mã khách, giờ bắt đầu, giờ kết thúc, có tự kết thúc không
├── service/
│   ├── Settings.java             tỉ lệ VND/giờ, quy đổi tiền → giây
│   ├── CustomerService.java      tìm, thêm, nạp tiền, xoá khách
│   └── SessionManager.java       bắt đầu/kết thúc phiên, kiểm tra hết giờ, lịch sử
├── persistence/
│   └── DataStore.java            đọc/ghi CSV và file cấu hình
└── ui/
    └── MainFrame.java            cửa sổ chính, bảng khách hàng, các nút
```

### Luồng xử lý chính

- **Nạp tiền:** `giây = số tiền × 3600 / giá mỗi giờ`, cộng vào số dư của khách.
- **Bắt đầu phiên:** kiểm tra khách còn giờ và chưa có phiên đang chạy, ghi `startTime = now()`.
- **Đang chơi:** mỗi giây bảng hiển thị `còn lại = số dư − (now − startTime)`.
- **Kết thúc phiên (thủ công hoặc tự động):** ghi `endTime = now()`, trừ `endTime − startTime` khỏi số dư (không âm), lưu khách và thêm một dòng vào lịch sử.
- **Hết giờ:** khi `còn lại ≤ 0`, phiên tự kết thúc và hiện thông báo.
- **Thoát chương trình:** các phiên đang chạy được kết thúc và trừ giờ trước khi đóng.

### Định dạng file

```
customers.csv   id;tên;sđt;giây_còn_lại
sessions.csv    id_khách;bắt_đầu;kết_thúc;tự_động_kết_thúc
config.properties  pricePerHour=10000
```

## 3. Giao diện

Một cửa sổ chính:

- **Trên cùng:** ô tìm kiếm theo tên/SĐT và tỉ lệ quy đổi hiện tại.
- **Giữa:** bảng khách hàng: Mã, Tên, SĐT, Giờ còn lại (đếm ngược trực tiếp), Trạng thái, Bắt đầu lúc.
- **Bên phải:** các nút Thêm khách hàng, Nạp tiền, Bắt đầu phiên, Kết thúc phiên, Lịch sử phiên, Xoá khách hàng, Cài đặt tỉ lệ.
- **Hộp thoại:** nhập thông tin khách, nhập số tiền, xác nhận xoá, thông báo hết giờ, xem lịch sử.

## 4. Phân công

Mỗi thành viên phụ trách một phần code và một phần tài liệu, để ai cũng có thể trình bày phần của mình khi demo.

| Thành viên | Code | Tài liệu / thuyết trình |
|---|---|---|
| **Phú** | `model/*`, `persistence/DataStore`, `Main`; tích hợp và kiểm tra compile | Phần kiến trúc, lưu trữ dữ liệu trong báo cáo |
| **Trần Hoàng** | `service/*`: quy đổi tiền, bắt đầu/kết thúc phiên, trừ giờ, tự kết thúc khi hết giờ | Phần nghiệp vụ trong báo cáo, kịch bản demo |
| **Khánh An** | `ui/MainFrame`: bảng, tìm kiếm, các nút và hộp thoại, thông báo hết giờ | Slide thuyết trình, ảnh chụp màn hình cho báo cáo |

Quy ước làm việc: mỗi người làm trên một nhánh riêng (`feature/...`), mở Pull Request vào `main`, người khác xem rồi mới merge. Trước khi merge phải chạy `build.sh`/`build.bat` không lỗi.

## 5. Các mốc

| Mốc | Nội dung | Kết quả |
|---|---|---|
| **Tuần 1** | Thống nhất thiết kế, khung project (đã có trong repo) | Repo, README, code compile được |
| **Tuần 2** | Hoàn thiện model + lưu file; nghiệp vụ nạp tiền, phiên chơi | Chạy được các chức năng a, c |
| **Tuần 3** | Hoàn thiện GUI, kiểm tra các trường hợp lỗi (nhập sai số tiền, xoá khi đang chơi, hết giờ) | Đủ chức năng a, b, c |
| **Tuần 4** | Báo cáo, slide, kịch bản demo, tập dượt | Nộp báo cáo, slide, demo (d, e) |

## 6. Kịch bản demo gợi ý

1. Đặt tỉ lệ 10.000 VND/giờ.
2. Thêm khách "Nguyễn Văn A", tìm lại bằng ô tìm kiếm.
3. Nạp 5.000 VND → được 00:30:00.
4. Bắt đầu phiên, chơi một lúc, kết thúc → xem số giờ bị trừ và lịch sử.
5. Đặt tỉ lệ rất cao (vd. 36.000.000 VND/giờ), nạp 10.000 VND (= 1 giây chơi) để demo nhanh việc **tự động kết thúc và thông báo hết giờ**.
6. Mở hai khách cùng lúc để cho thấy thời gian được tính riêng cho từng khách.
7. Xoá khách hàng.
