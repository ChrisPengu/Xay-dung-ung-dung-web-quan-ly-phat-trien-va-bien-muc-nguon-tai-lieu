# Hệ thống thiết kế DocuCatalog

Tài liệu này mô tả lớp giao diện hiện đại của DocuCatalog để việc phát triển tiếp theo giữ được tính nhất quán. Mã nền nằm trong `app.css`; lớp token và tinh chỉnh hiện đại nằm trong `app-modern.css`.

## Định hướng

- Phong cách: dashboard quản trị hiện đại, mật độ thông tin khá cao nhưng vẫn có khoảng thở.
- Nhận diện: xanh thư viện làm màu chủ đạo, hổ phách làm điểm nhấn, nền trung tính ấm.
- Font: system font để hiển thị tiếng Việt tốt và không cần tải tài nguyên ngoài.
- Theme: sáng/tối theo lựa chọn lưu trong `localStorage`; lần đầu theo thiết lập hệ điều hành.

## Token ngữ nghĩa

Không dùng trực tiếp màu nền/chữ trong component mới. Ưu tiên các token:

| Nhóm | Token chính | Mục đích |
| --- | --- | --- |
| Chữ | `--ink-950`, `--ink-800`, `--ink-650`, `--ink-500` | Tiêu đề, nội dung, phụ chú |
| Bề mặt | `--surface`, `--card`, `--card-muted`, `--glass` | Nền trang, thẻ, vùng phụ, topbar |
| Thương hiệu | `--brand-800`, `--brand-700`, `--brand-600`, `--brand-100`, `--brand-50` | Điều hướng, CTA, focus |
| Trạng thái | `--success`, `--danger`, `--warning`, `--info` và biến `*-soft` | Phản hồi nghiệp vụ |
| Cấu trúc | `--line`, `--line-soft`, `--shadow-sm/md/lg` | Viền và phân cấp chiều sâu |

## Thành phần và tương tác

- Nút, liên kết dạng nút, phân trang, menu di động và nút hiện mật khẩu có vùng tương tác tối thiểu 44px.
- Mọi trường nhập có `label`; lỗi server hiển thị cạnh trường và trường lỗi đầu tiên tự nhận focus.
- Nút submit chuyển sang trạng thái loading, có `aria-busy` và không cho gửi lặp.
- Form hồ sơ cảnh báo khi rời trang nếu có thay đổi chưa lưu; giới thiệu ngắn có bộ đếm ký tự.
- Hành động nguy hiểm dùng `dialog` có focus rõ, hỗ trợ phím Escape và fallback về xác nhận trình duyệt.
- Thước đo mật khẩu chỉ phản hồi chất lượng nhập; quy tắc bắt buộc vẫn được kiểm tra phía server.

## Chuyển động và VFX

- Chuyển cảnh chính dùng khoảng 180-360ms và easing tự nhiên; không dùng hiệu ứng cuộn cưỡng bức.
- Canvas hạt là lớp trang trí duy nhất, `pointer-events: none`, giới hạn 18-30 hạt và DPR tối đa 1.5.
- Animation dừng khi tab ẩn. Với `prefers-reduced-motion: reduce`, hạt đứng yên và các animation vào trang/dialog bị tắt.

## Responsive và accessibility

- Breakpoint chính: 1180px cho layout hồ sơ, 900px cho sidebar di động, 680px và 430px cho màn nhỏ.
- Có skip link, focus ring dễ thấy, `aria-current` cho điều hướng, vùng `aria-live` cho thông báo và độ mạnh mật khẩu.
- Nội dung chức năng không phụ thuộc màu sắc hoặc chuyển động; icon trang trí dùng `aria-hidden`.
- Khi thêm component mới, kiểm tra tối thiểu ở 390px, 768px và 1440px, cả theme sáng/tối và chế độ giảm chuyển động.
