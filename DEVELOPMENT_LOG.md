# Nhật ký xây dựng DocuCatalog

## 1. Mục tiêu

Xây dựng ứng dụng web quản lý phát triển và biên mục nguồn tài liệu bằng Spring Boot, Spring Data JPA, Thymeleaf và MySQL. Ứng dụng phục vụ ba nhóm nghiệp vụ: quản trị hệ thống, biên mục và bổ sung nguồn tài liệu.

## 2. Kế hoạch chi tiết

| Giai đoạn | Công việc | Tiêu chí hoàn thành | Trạng thái |
| --- | --- | --- | --- |
| 1 | Khảo sát môi trường, xác định phạm vi | Chốt công nghệ, vai trò và luồng nghiệp vụ | Hoàn thành |
| 2 | Thiết kế dữ liệu và kiến trúc | Có domain, quan hệ, repository, service và quy tắc nghiệp vụ | Hoàn thành |
| 3 | Xây dựng backend | CRUD, tìm kiếm, phân trang, validation, bảo mật và workflow hoạt động | Hoàn thành |
| 4 | Xây dựng Thymeleaf UI | Giao diện responsive, trạng thái rõ ràng, form dễ dùng | Hoàn thành |
| 5 | Dữ liệu mẫu và kiểm thử | Có tài khoản mẫu, dữ liệu demo, test nghiệp vụ | Hoàn thành |
| 6 | Xác minh và bàn giao | Build/test thành công, README hướng dẫn đầy đủ | Hoàn thành |
| 7 | MySQL cục bộ và quản lý phiên bản schema | MySQL thật chạy an toàn, Flyway quản lý schema, smoke test thành công | Hoàn thành |
| 8 | Hoàn thiện nghiệp vụ thực tế | CRUD biên mục/bản ấn phẩm, quản trị tài khoản, kiểm soát xóa và kiểm thử hồi quy | Hoàn thành |

## 3. Phạm vi chức năng

- Đăng nhập và phân quyền `ADMIN`, `CATALOGER`, `ACQUISITION`.
- Dashboard tổng hợp số tài liệu, bản ấn phẩm, đề xuất đang chờ và ngân sách.
- Quản lý danh mục tác giả, thể loại, nhà xuất bản và nhà cung cấp.
- Biên mục tài liệu đầy đủ Thêm – Xem – Sửa – Xóa, sao chép biểu ghi, tìm theo mã/ISBN/tác giả/số đăng ký cá biệt.
- Quản lý từng bản ấn phẩm đầy đủ Thêm – Sửa – Xóa bằng số đăng ký cá biệt, vị trí, tình trạng và trạng thái lưu thông.
- Quy trình bổ sung: nháp → gửi duyệt → phê duyệt/từ chối → hoàn tất.
- Khi hoàn tất đề xuất, hệ thống tự tạo đúng số lượng bản ấn phẩm và mã đăng ký cá biệt không trùng.
- Tìm kiếm, lọc, phân trang, kiểm tra dữ liệu đầu vào và thông báo kết quả.
- Quản trị người dùng, phân vai trò, kích hoạt/vô hiệu hóa và tự đổi mật khẩu.

## 4. Quyết định kỹ thuật

- **2026-08-25:** Chọn Spring Boot 4.1.1, mức biên dịch Java 21. Bản Spring Boot này tương thích Java 26 đang cài trên máy, còn Java 21 giúp artefact dễ triển khai trên môi trường LTS.
- **2026-08-25:** Chọn kiến trúc phân lớp `web → service → repository → domain`; form model tách khỏi entity ở các màn hình nghiệp vụ phức tạp.
- **2026-08-25:** MySQL là cơ sở dữ liệu chạy thật; H2 chỉ dùng cô lập trong kiểm thử tự động.
- **2026-08-25:** Giao diện dùng CSS thuần có design token, không dựa CDN để có thể chạy trong mạng nội bộ.
- **2026-08-25:** Mật khẩu được băm BCrypt; CSRF bật mặc định; quyền ghi được giới hạn theo vai trò.
- **2026-09-28:** Chuyển schema MySQL sang Flyway, để Hibernate ở chế độ `validate`; giữ H2 `create-drop` riêng cho demo/test.
- **2026-09-28:** MySQL chỉ lắng nghe loopback, không mở Windows Firewall, dịch vụ chạy bằng `LocalService`; tài khoản ứng dụng chỉ có quyền trong schema `docucatalog`.
- **2026-09-28:** Tách biểu ghi thư mục và bản ấn phẩm theo mô hình Instance/Item của FOLIO và cách tổ chức bibliographic record/items của Koha; một biểu ghi có thể có nhiều bản vật lý độc lập.
- **2026-09-28:** Không cho xóa biểu ghi còn bản ấn phẩm hoặc còn tham chiếu trong đề xuất; không cho xóa bản đang mượn. Đây là ràng buộc bảo toàn lịch sử thay cho xóa dây chuyền.
- **2026-09-28:** Quản lý nhân sự bằng vô hiệu hóa tài khoản thay vì xóa; chặn tự vô hiệu hóa/tự hạ quyền và chặn loại bỏ quản trị viên hoạt động cuối cùng.

## 5. Mô hình dữ liệu dự kiến

`UserAccount`, `Category`, `Author`, `Publisher`, `Supplier`, `ResourceDocument`, `ResourceCopy`, `AcquisitionRequest`, `AcquisitionItem`.

Quan hệ chính:

- Tài liệu — tác giả: nhiều–nhiều.
- Tài liệu — thể loại/nhà xuất bản: nhiều–một.
- Tài liệu — bản ấn phẩm: một–nhiều.
- Đề xuất bổ sung — nhà cung cấp: nhiều–một.
- Đề xuất bổ sung — dòng đề xuất: một–nhiều; mỗi dòng tham chiếu một tài liệu.

## 6. Nhật ký thực hiện

### 2026-08-25

- Khảo sát workspace: chỉ có tệp `Reame.md` rỗng, chưa có mã nguồn và chưa được khởi tạo Git.
- Kiểm tra môi trường: Java 26.0.2 khả dụng; Maven chưa được cài toàn cục.
- Khởi tạo Maven project, cấu hình MySQL bằng biến môi trường, Docker Compose và profile H2 cho test.
- Hoàn thiện 9 thực thể nghiệp vụ, enum trạng thái, quan hệ JPA, repository và ràng buộc duy nhất ở mức cơ sở dữ liệu.
- Cài đặt tầng service có transaction cho biên mục, dữ liệu danh mục, dashboard và toàn bộ vòng đời đề xuất bổ sung.
- Cài đặt Spring Security với ba vai trò, BCrypt, CSRF và kiểm soát quyền ở URL lẫn phương thức service.
- Xây dựng controller, form model và validation cho tài liệu, bản ấn phẩm, đề xuất bổ sung và dữ liệu danh mục.
- Xây dựng giao diện Thymeleaf responsive bằng design token CSS, hỗ trợ điều hướng di động, trạng thái focus, thông báo, xác nhận xóa và tính tổng đề xuất động.
- Tạo dữ liệu minh họa gồm ba tài khoản theo vai trò, tài liệu, bản ấn phẩm và đề xuất bổ sung.
- Tạo Maven Wrapper 3.9.16; kiểm tra SHA-512 của bản Maven tải từ Apache trước khi dùng để sinh wrapper.
- Viết README, sơ đồ ER, hướng dẫn MySQL/Docker, biến môi trường, tài khoản demo và lệnh vận hành.
- Vòng build đầu phát hiện JDK 26 không tự chạy annotation processor của Lombok; đã khai báo rõ processor và bật `proc=full` trong Maven Compiler Plugin.
- Vòng render đầu phát hiện template tham chiếu utility security chưa có dialect; đã đưa thông tin người dùng/quyền vào model chung và loại bỏ phụ thuộc không cần thiết.
- Mở rộng kiểm thử render qua đăng nhập HTTP thật, CSRF, trang chi tiết/chỉnh sửa và bốn tab dữ liệu danh mục.

## 7. Kết quả kiểm thử

- `AcquisitionWorkflowIntegrationTest`: kiểm tra xuyên suốt `DRAFT → PENDING → APPROVED → COMPLETED`, tổng tiền và việc sinh đúng ba bản ấn phẩm với mã đăng ký cá biệt liên tiếp.
- `WebRenderingIntegrationTest`: khởi động máy chủ trên cổng ngẫu nhiên, đăng nhập qua form có CSRF và tải các màn hình dashboard, danh sách/tạo/chi tiết/chỉnh sửa tài liệu, danh sách/tạo/chi tiết/chỉnh sửa đề xuất và bốn tab dữ liệu danh mục.
- Cơ sở dữ liệu kiểm thử là H2 ở chế độ tương thích MySQL, schema được tạo mới cho từng lần chạy và không chạm dữ liệu thật.
- `./mvnw clean package` hoàn tất lúc 00:43 ngày 2026-08-25: **BUILD SUCCESS**, 46 lớp nguồn được biên dịch, **2 test, 0 lỗi, 0 thất bại, 0 bỏ qua**.
- Artefact chạy độc lập được tạo tại `target/docucatalog-1.0.0.jar`.
- Máy hiện tại không có Docker CLI nên chưa thể chạy `docker compose config` hoặc smoke test container; cấu hình Compose vẫn được cung cấp để chạy trên máy có Docker.

## 8. Kết quả bàn giao và hướng phát triển tiếp

Phiên bản hiện tại đáp ứng phạm vi MVP đã xác định và có thể chạy bằng MySQL trực tiếp hoặc Docker Compose. Các hạng mục nên bổ sung trước khi dùng trong môi trường sản xuất quy mô lớn:

- Thêm import/export MARC21, Dublin Core hoặc Excel nếu thư viện cần trao đổi dữ liệu chuẩn.
- Thêm lưu file số, nhật ký kiểm toán, sao lưu và quan sát hệ thống.
- Chạy test container với MySQL thật trong CI và kiểm thử trình duyệt end-to-end.

### 2026-08-28 — xử lý đường dẫn chạy trên Windows

- Tái hiện lỗi `ClassNotFoundException: vn.edu.docucatalog.DocuCatalogApplication` khi dùng `spring-boot:run`.
- Xác nhận file `.class` tồn tại và chạy được với classpath tương đối, nhưng Java 26 không đọc được classpath tuyệt đối chứa ký tự tiếng Việt có dấu trong đường dẫn hiện tại.
- Thống nhất đổi thư mục dự án sang `Xay dung ung dung web quan ly phat trien va bien muc nguon tai lieu` để Maven/Spring Boot Maven Plugin chỉ nhận đường dẫn ASCII.
- Phát hiện dịch vụ MySQL trên máy đã được cài nhưng đang ở trạng thái `Stopped`; sau khi sửa đường dẫn cần khởi động MySQL trước khi chạy ứng dụng.

### 2026-08-28 — bổ sung chế độ demo không cần MySQL

- Chuyển H2 từ dependency chỉ dành cho test sang runtime để có thể dùng trong bản chạy demo.
- Thêm profile `demo` với cơ sở dữ liệu H2 in-memory, tự tạo/xóa schema và bật bộ dữ liệu mẫu.
- Giữ nguyên profile mặc định dùng MySQL để không thay đổi kiến trúc triển khai chính thức.
- Thêm `run-demo.cmd` làm lệnh khởi động một bước trên Windows.
- Thêm kiểm thử tích hợp xác minh profile demo nạp đúng 3 tài khoản, 5 tài liệu, 4 bản ấn phẩm và 2 đề xuất.
- Kết quả toàn bộ suite sau thay đổi: 3 kiểm thử, 0 lỗi, 0 thất bại, 0 bỏ qua.
- Smoke test `run-demo.cmd`: ứng dụng dùng URL H2 `jdbc:h2:mem:docucatalog-demo`, trang đăng nhập trả HTTP 200, tài khoản admin đăng nhập thành công và dashboard trả HTTP 200.

### 2026-09-28 — cài đặt MySQL thật và hoàn thiện Flyway

- Kiểm kê môi trường trước khi thay đổi: phát hiện dịch vụ `mysql` cũ trỏ tới `D:\xampp\mysql\bin\mysqld.exe` không còn tồn tại; thư mục `D:\xampp` cũng không còn.
- Theo sự cho phép của người dùng, xóa duy nhất đăng ký dịch vụ XAMPP/MySQL hỏng; không có thư mục dữ liệu cũ nào bị xóa.
- Cài MySQL Server 8.4.9 của Oracle bằng manifest chính thức trên Windows Package Manager.
- Khởi tạo datadir mới tại `C:\ProgramData\MySQL\MySQL Server 8.4`, đăng ký dịch vụ `MySQL84` tự khởi động bằng tài khoản giới hạn `NT AUTHORITY\LocalService`.
- Cấu hình MySQL chỉ bind `127.0.0.1:3306`, tắt X Plugin, tắt `local-infile`, không tạo firewall rule và giới hạn quyền NTFS của datadir.
- Sinh mật khẩu ngẫu nhiên cho `root` và `docucatalog`; credential root được mã hóa bằng Windows DPAPI tại `%LOCALAPPDATA%\DocuCatalog\mysql-root.credential.xml`, còn biến kết nối ứng dụng nằm trong `.env.local` đã được ignore và giới hạn ACL.
- Tạo schema `docucatalog` dùng `utf8mb4_0900_ai_ci`; tài khoản `docucatalog@localhost` chỉ được cấp quyền trong schema này.
- Thêm `run-mysql.cmd`, `run-mysql.ps1` và `tools/mysql-admin.ps1`; loại bỏ mật khẩu MySQL mặc định yếu khỏi `application.yml`.
- Thêm `spring-boot-starter-flyway`, `flyway-mysql` và migration `V1__initial_schema.sql`; schema hiện hữu được baseline version 1, Hibernate chuyển từ `update` sang `validate`.
- Smoke test MySQL thật: Connector/J kết nối MySQL 8.4.9, nạp đúng 3 tài khoản, 5 tài liệu, 4 bản ấn phẩm và 2 đề xuất; đăng nhập `admin` qua HTTP thành công, `/dashboard` trả HTTP 200.
- Kiểm thử migration trên schema tạm hoàn toàn rỗng: Flyway tạo 10 bảng nghiệp vụ cùng `flyway_schema_history`, áp dụng version 1 và Hibernate validate thành công. Schema thử và quyền tương ứng được thu hồi/xóa ngay sau kiểm tra.
- Chạy lại toàn bộ test H2 sau thay đổi: **3 test, 0 lỗi, 0 thất bại, 0 bỏ qua**.
- `mvnw.cmd clean package` hoàn tất lúc 05:03 ngày 2026-09-28: **BUILD SUCCESS**; artefact mới tại `target/docucatalog-1.0.0.jar`.

### 2026-09-28 — sửa lỗi mất CSS và hoàn thiện môi trường Eclipse

- Tái hiện lỗi trực tiếp qua HTTP: `/css/app.css` vẫn trả `200 text/css`, nhưng HTML `/dashboard` không có thẻ `<head>`.
- Xác định nguyên nhân ở Thymeleaf: trang con thay thẻ `<html>` bằng fragment được khai báo trên `<body>`, làm mất metadata, viewport và toàn bộ stylesheet.
- Chuyển fragment `layout(...)` lên phần tử `<html>` để mọi trang nhận đủ tài liệu HTML; thêm kiểm thử hồi quy bắt buộc có `<head>`, CSS ứng dụng và CSS icon trên 13 luồng/màn hình.
- Tích hợp Bootstrap Icons 1.13.1 qua WebJar từ dự án chính thức `twbs/icons` (MIT), mở quyền đọc `/webjars/**` và thay icon Unicode ở điều hướng, dashboard, trạng thái rỗng, thông báo và thao tác chính.
- Kiểm tra trực tiếp trên MySQL thật: đăng nhập thành công, `/dashboard` có `app-shell`, stylesheet ứng dụng, stylesheet icon và icon điều hướng; CSS trả `200 text/css`, font WOFF2 trả `200 font/woff2`.
- Thêm `spring.config.import=optional:file:.env.local[.properties]`; chạy thẳng `mvnw.cmd spring-boot:run` đã đọc biến kết nối, Flyway validate schema MySQL 8.4.9 và khởi động cổng 8080 thành công.
- Thêm `.project`, `.classpath`, cấu hình m2e/Java 21/UTF-8 và hai shared launch configuration `DocuCatalog - MySQL`/`DocuCatalog - Demo`.
- Cài Spring Tools for Eclipse 5.4.0 chính thức tại thư mục người dùng và tích hợp Lombok 1.18.46. Gói tải từ `cdn.spring.io` có SHA-256 `6117C960757AB56B70B2806DB110E7505013051FB7ACDEE2A476791B1C358B4D`.
- Chạy toàn bộ suite sau bản sửa: **4 test, 0 lỗi, 0 thất bại, 0 bỏ qua**.
- `mvnw.cmd clean package` hoàn tất lúc 05:24:49: **BUILD SUCCESS**; executable JAR 65.004.261 byte được smoke-test trực tiếp với MySQL, Flyway và toàn bộ asset cục bộ rồi giữ chạy tại `http://localhost:8080`.
- Xác minh Spring Tools ở chế độ headless: Eclipse 4.41.0, JDT, Maven m2e 2.11 và Spring Boot tooling 5.4.0 đều được nạp thành công; file ZIP cài đặt tạm 570 MB đã được xóa sau khi kiểm tra.

### 2026-09-28 — hoàn thiện CRUD và nghiệp vụ vận hành thực tế

- Rà soát ứng dụng theo tài liệu chính thức của FOLIO Inventory, Koha Cataloging và chuẩn MARC 21 của Library of Congress.
- Hiển thị trực tiếp đủ thao tác **Chi tiết – Sửa – Sao chép – Xóa** trên danh sách biên mục; chức năng sao chép giữ metadata mô tả, sinh mã mới, bỏ ISBN và đưa biểu ghi mới về trạng thái bản nháp.
- Bổ sung màn hình sửa toàn bộ thông tin bản ấn phẩm: số đăng ký cá biệt, ngày bổ sung, giá, vị trí, tình trạng vật lý, trạng thái lưu thông và ghi chú.
- Mở rộng tìm kiếm biểu ghi theo số đăng ký cá biệt bằng subquery JPA, bên cạnh nhan đề, mã biên mục, ISBN và tác giả.
- Chặn xóa bản ấn phẩm ở trạng thái `CHECKED_OUT`; vẫn giữ ràng buộc không xóa biểu ghi còn bản ấn phẩm hoặc nằm trong đề xuất bổ sung.
- Bổ sung thao tác xóa đề xuất nháp/bị từ chối ngay trên danh sách; các trạng thái đã gửi duyệt vẫn được bảo vệ bởi tầng service.
- Hoàn thiện quản trị người dùng: danh sách, tạo, sửa, phân vai trò, kích hoạt/vô hiệu hóa; bảo vệ tài khoản đang đăng nhập và quản trị viên cuối cùng.
- Bổ sung đổi mật khẩu cá nhân có xác minh mật khẩu hiện tại, đối chiếu xác nhận, BCrypt và bắt đăng nhập lại sau khi đổi.
- Chỉ hiển thị credential mẫu khi `app.seed-data=true`, tránh công khai thông tin demo ở môi trường đã tắt seed data.
- Thêm `CatalogAdministrationIntegrationTest` và mở rộng `WebRenderingIntegrationTest` cho các màn hình sao chép biểu ghi, sửa bản ấn phẩm, quản trị người dùng và đổi mật khẩu.
- Kết quả toàn bộ suite: **4 lớp kiểm thử, 6 ca kiểm thử, 0 lỗi, 0 thất bại, 0 bỏ qua**.
- Smoke test artefact với MySQL 8.4 thật: đăng nhập qua CSRF thành công; `/dashboard`, `/documents`, `/users` đều trả HTTP 200; HTML biên mục có đủ Sửa, Sao chép và Xóa, CSS cục bộ được liên kết đúng.
- Kiểm tra ma trận quyền qua HTTP: `CATALOGER` mở trang tạo biểu ghi nhưng nhận 403 ở quản trị người dùng; `ACQUISITION` mở trang tạo đề xuất nhưng nhận 403 ở trang tạo biểu ghi.
- `mvnw.cmd clean package` cuối cùng hoàn tất lúc 05:48 ngày 2026-09-28: **BUILD SUCCESS**; JAR 65.024.214 byte được khởi động với MySQL và đang phục vụ tại `http://localhost:8080`.

## 9. Tài liệu chính thức đã tham khảo

- [MySQL 8.4 — cài đặt trên Windows](https://dev.mysql.com/doc/refman/8.4/en/windows-installation.html)
- [MySQL 8.4 — khởi tạo data directory](https://dev.mysql.com/doc/refman/8.4/en/data-directory-initialization.html)
- [MySQL — chạy server dưới dạng Windows Service](https://dev.mysql.com/doc/refman/8.4/en/windows-start-service.html)
- [Spring Boot — database initialization và Flyway](https://docs.spring.io/spring-boot/how-to/data-initialization.html)
- [Spring Boot — danh sách starter, gồm `spring-boot-starter-flyway`](https://docs.spring.io/spring-boot/reference/using/build-systems.html)
- [Flyway — driver/module MySQL](https://documentation.red-gate.com/flyway/reference/database-driver-reference/mysql)
- [Spring Tools for Eclipse](https://spring.io/tools/)
- [Spring Tools — mã nguồn và hướng dẫn](https://github.com/spring-projects/spring-tools)
- [Lombok — thiết lập cho Eclipse/STS](https://projectlombok.org/setup/eclipse)
- [Bootstrap Icons — tài liệu chính thức](https://icons.getbootstrap.com/)
- [Bootstrap Icons — mã nguồn và giấy phép MIT](https://github.com/twbs/icons)
- [FOLIO Inventory — mô hình Instance, Holdings và Item](https://docs.folio.org/docs/metadata/inventory/)
- [Koha Manual — Cataloging](https://github.com/Koha-Community/kohadocs/blob/master/source/cataloging.rst)
- [Library of Congress — MARC 21 Format for Bibliographic Data](https://www.loc.gov/marc/bibliographic/)
- [Evergreen — xóa biểu ghi thư mục rỗng](https://docs.evergreen-ils.org/docs/latest/cataloging/record_buckets.html)
