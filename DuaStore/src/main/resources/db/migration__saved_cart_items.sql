-- Bảng lưu sản phẩm để mua sau
CREATE TABLE SavedCartItems (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    ProductId INT NOT NULL,
    VariantId INT NOT NULL,
    SoLuong INT NOT NULL DEFAULT 1,
    GiaLuu DECIMAL(12,0) NOT NULL,
    NgayLuu DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_SavedCartItems_Users FOREIGN KEY (UserId) REFERENCES Users(Id),
    CONSTRAINT FK_SavedCartItems_Products FOREIGN KEY (ProductId) REFERENCES Products(Id),
    CONSTRAINT FK_SavedCartItems_ProductVariants FOREIGN KEY (VariantId) REFERENCES ProductVariants(Id)
);
