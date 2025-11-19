Ứng dụng đọc tin tức được phát triển bằng Kotlin.
NewsApp cho phép người dùng xem tin tức mới nhất từ GNews API, lưu tin offline bằng Room, và thêm vào mục yêu thích để đọc lại sau.

Tính năng chính:
 + Hiển thị danh sách tin tức từ GNews API.
   
 + Xem chi tiết bài báo.
   
 + Thêm/xóa tin tức vào mục Yêu thích.
   
 + Lưu dữ liệu offline bằng Room Database.
   
 + Phân trang bằng Fragment (Tin tức, Yêu thích, Tìm kiếm).
   
 + Xử lý bất đồng bộ bằng Coroutines.
   
 + Giao diện đơn giản, dễ sử dụng.

Công nghệ sử dụng:
  + Ngôn ngữ: Kotlin.
    
  + UI: RecyclerView, Fragment, ViewBinding.
    
  + Thư viện: Retrofit + Gson.
    
  + Database: Room (DAO, Entity, LiveData).
    
  + Kiến trúc: MVVM (ViewModel + Repository).
