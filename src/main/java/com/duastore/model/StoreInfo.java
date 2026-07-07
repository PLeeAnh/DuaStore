package com.duastore.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_info")
public class StoreInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tenCuaHang", nullable = false, length = 200)
    private String tenCuaHang;

    @Column(name = "soNha", length = 100)
    private String soNha;

    @Column(name = "duong", length = 200)
    private String duong;

    @Column(name = "phuongXa", length = 100)
    private String phuongXa;

    @Column(name = "quanHuyen", length = 100)
    private String quanHuyen;

    @Column(name = "tinhThanh", length = 100)
    private String tinhThanh;

    @Column(name = "soDienThoai", length = 20)
    private String soDienThoai;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "isActive", nullable = false)
    private Boolean isActive = true;

    @Column(name = "isDefault", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getDiaChiDayDu() {
        StringBuilder sb = new StringBuilder();
        if (soNha != null && !soNha.isBlank()) sb.append(soNha).append(", ");
        if (duong != null && !duong.isBlank()) sb.append(duong).append(", ");
        if (phuongXa != null && !phuongXa.isBlank()) sb.append(phuongXa).append(", ");
        if (quanHuyen != null && !quanHuyen.isBlank()) sb.append(quanHuyen).append(", ");
        if (tinhThanh != null && !tinhThanh.isBlank()) sb.append(tinhThanh);
        return sb.toString().replaceAll(", $", "");
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTenCuaHang() { return tenCuaHang; }
    public void setTenCuaHang(String tenCuaHang) { this.tenCuaHang = tenCuaHang; }
    public String getSoNha() { return soNha; }
    public void setSoNha(String soNha) { this.soNha = soNha; }
    public String getDuong() { return duong; }
    public void setDuong(String duong) { this.duong = duong; }
    public String getPhuongXa() { return phuongXa; }
    public void setPhuongXa(String phuongXa) { this.phuongXa = phuongXa; }
    public String getQuanHuyen() { return quanHuyen; }
    public void setQuanHuyen(String quanHuyen) { this.quanHuyen = quanHuyen; }
    public String getTinhThanh() { return tinhThanh; }
    public void setTinhThanh(String tinhThanh) { this.tinhThanh = tinhThanh; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
