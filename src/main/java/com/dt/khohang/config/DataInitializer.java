package com.dt.khohang.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dt.khohang.entity.Product;
import com.dt.khohang.entity.User;
import com.dt.khohang.repository.ProductRepository;
import com.dt.khohang.repository.UserRepository;
import com.dt.khohang.service.ProductService;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            ProductRepository productRepository,
            ProductService productService,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // 1. Tạo tài khoản ADMIN mặc định nếu chưa có
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setRole("ADMIN");
                admin.setFullName("Nguyễn Đức Dương (Quản trị viên)");
                admin.setEmail("admin@smartwarehouse.vn");
                admin.setPhone("0988123456");
                admin.setAddress("Hà Nội, Việt Nam");
                userRepository.save(admin);
            }

            // 2. Tạo tài khoản CUSTOMER mặc định nếu chưa có
            if (userRepository.findByUsername("customer").isEmpty()) {
                User customer = new User();
                customer.setUsername("customer");
                customer.setPassword(passwordEncoder.encode("123456"));
                customer.setRole("CUSTOMER");
                customer.setFullName("Phạm Hoàng Hưng Thịnh (Khách hàng)");
                customer.setEmail("customer@smartwarehouse.vn");
                customer.setPhone("0912345678");
                customer.setAddress("TP. Hồ Chí Minh, Việt Nam");
                userRepository.save(customer);
            }

            // 3. Khởi tạo sản phẩm mẫu trong kho nếu chưa có
            if (productRepository.count() == 0) {
                Product p1 = new Product(
                    "Máy quét mã vạch không dây Zebra DS2278",
                    "SP-ZEBRA-01",
                    "Thiết bị kho",
                    3850000.0,
                    "Máy quét mã vạch 2D Bluetooth chuyên dụng cho quản lý kho bãi logistic, phạm vi quét 10m",
                    "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=600&auto=format&fit=crop&q=60",
                    "Cái"
                );
                productService.createProduct(p1, 45, 10, "Kệ A-01-02");

                Product p2 = new Product(
                    "Xe đẩy hàng 4 bánh sàn thép Dandy UDA-LSC",
                    "SP-DANDY-02",
                    "Công cụ kho",
                    2650000.0,
                    "Tải trọng tối đa 300kg, bánh xe cao su chống ồn, mặt sàn bọc đệm cao su chống trượt",
                    "https://images.unsplash.com/photo-1553413077-190dd305871c?w=600&auto=format&fit=crop&q=60",
                    "Chiếc"
                );
                productService.createProduct(p2, 18, 5, "Khu vực B-03");

                Product p3 = new Product(
                    "Pallet nhựa lót sàn 1200x1000x150mm",
                    "SP-PALLET-03",
                    "Vật tư lưu kho",
                    420000.0,
                    "Chất liệu nhựa HDPE nguyên sinh, chịu tải động 1.5 tấn, tải tĩnh 4 tấn",
                    "https://images.unsplash.com/photo-1616401784845-180882ba9ba8?w=600&auto=format&fit=crop&q=60",
                    "Tấm"
                );
                productService.createProduct(p3, 120, 20, "Dãy C-Pallet");

                Product p4 = new Product(
                    "Thùng carton 5 lớp chuyên dụng xuất khẩu 60x40x40",
                    "SP-CARTON-04",
                    "Bao bì đóng gói",
                    32000.0,
                    "Giấy sóng BC 5 lớp cứng cáp, bảo vệ hàng dễ vỡ khi vận chuyển đường dài",
                    "https://images.unsplash.com/photo-1530587191325-3db32d826c18?w=600&auto=format&fit=crop&q=60",
                    "Thùng"
                );
                productService.createProduct(p4, 850, 100, "Kho Phụ D-01");

                Product p5 = new Product(
                    "Cuộn màng PE bọc quấn pallet 3kg (50cm)",
                    "SP-PE-05",
                    "Bao bì đóng gói",
                    145000.0,
                    "Độ dãn 300%, chống nước, chống bụi bẩn bám dính khi lưu kho",
                    "https://images.unsplash.com/photo-1587293852726-70cdb56c2866?w=600&auto=format&fit=crop&q=60",
                    "Cuộn"
                );
                productService.createProduct(p5, 8, 15, "Kho Phụ D-02"); // Cảnh báo tồn kho thấp (8 < 15)

                Product p6 = new Product(
                    "Băng keo dán thùng OPP trong 4.8cm x 100 yard",
                    "SP-TAPE-06",
                    "Bao bì đóng gói",
                    18000.0,
                    "Độ dính 50 mic siêu chắc, không đứt ngang khi dán thùng hàng xuất xưởng",
                    "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=600&auto=format&fit=crop&q=60",
                    "Cuộn"
                );
                productService.createProduct(p6, 5, 20, "Kho Phụ D-03"); // Cảnh báo tồn kho thấp (5 < 20)
            }
        };
    }
}