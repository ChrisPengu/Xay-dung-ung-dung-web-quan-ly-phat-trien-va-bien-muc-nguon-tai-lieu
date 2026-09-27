CREATE TABLE authors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    birth_year INT DEFAULT NULL,
    name VARCHAR(150) NOT NULL,
    nationality VARCHAR(80) DEFAULT NULL,
    note VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT authors_chk_1 CHECK (birth_year <= 2100 AND birth_year >= 1000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    code VARCHAR(30) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    name VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UKiwylx6fb2dqdw8kfc31vaiiyp (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE publishers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    address VARCHAR(255) DEFAULT NULL,
    email VARCHAR(150) DEFAULT NULL,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UKan1ucpx8sw2qm194mlok8e5us (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE suppliers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    address VARCHAR(255) DEFAULT NULL,
    contact_person VARCHAR(100) DEFAULT NULL,
    email VARCHAR(150) DEFAULT NULL,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UKeegixpn11chp14nb25tl3ucv0 (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    active BIT(1) NOT NULL,
    email VARCHAR(150) DEFAULT NULL,
    full_name VARCHAR(120) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ACQUISITION','ADMIN','CATALOGER') NOT NULL,
    username VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UKlxwlgwuy2yrbye2vgs9w9x7mr (username),
    UNIQUE KEY UKf9sl209luxhu4rylls0h1m625 (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE resource_documents (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    call_number VARCHAR(50) DEFAULT NULL,
    catalog_code VARCHAR(40) NOT NULL,
    classification_number VARCHAR(50) DEFAULT NULL,
    edition VARCHAR(80) DEFAULT NULL,
    isbn VARCHAR(20) DEFAULT NULL,
    keywords VARCHAR(500) DEFAULT NULL,
    language VARCHAR(50) NOT NULL,
    physical_description VARCHAR(255) DEFAULT NULL,
    publication_year INT DEFAULT NULL,
    status ENUM('DRAFT','PUBLISHED') NOT NULL,
    subtitle VARCHAR(255) DEFAULT NULL,
    summary TEXT,
    title VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL,
    publisher_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UKoh91l61d77ugqyiyqd77e0fbr (catalog_code),
    UNIQUE KEY UKafgybmw9jswefgev5l5o2w4n (isbn),
    KEY FK8a5m31pspv7o6fhg9fkrhmiew (category_id),
    KEY FKhidjix8tqdk8tb02260b13ak8 (publisher_id),
    CONSTRAINT FK8a5m31pspv7o6fhg9fkrhmiew FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT FKhidjix8tqdk8tb02260b13ak8 FOREIGN KEY (publisher_id) REFERENCES publishers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE acquisition_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    approved_at DATETIME(6) DEFAULT NULL,
    approved_by VARCHAR(120) DEFAULT NULL,
    completed_at DATETIME(6) DEFAULT NULL,
    expected_date DATE DEFAULT NULL,
    note VARCHAR(1000) DEFAULT NULL,
    request_code VARCHAR(40) NOT NULL,
    request_date DATE NOT NULL,
    requester_name VARCHAR(120) NOT NULL,
    status ENUM('APPROVED','COMPLETED','DRAFT','PENDING','REJECTED') NOT NULL,
    title VARCHAR(200) NOT NULL,
    supplier_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UKbap25qhk74iojt5xxkun7n65 (request_code),
    KEY FK8g5gmxl1bcgu38oc2xyyry2lo (supplier_id),
    CONSTRAINT FK8g5gmxl1bcgu38oc2xyyry2lo FOREIGN KEY (supplier_id) REFERENCES suppliers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE resource_copies (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    accession_number VARCHAR(60) NOT NULL,
    acquired_date DATE NOT NULL,
    physical_condition ENUM('DAMAGED','GOOD','NEW','WORN') NOT NULL,
    location VARCHAR(100) NOT NULL,
    note VARCHAR(500) DEFAULT NULL,
    price DECIMAL(15,2) DEFAULT NULL,
    status ENUM('AVAILABLE','CHECKED_OUT','LOST','WITHDRAWN') NOT NULL,
    document_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UKaqf9gktsdr9kretun8nk66vvy (accession_number),
    KEY FKfwkg6eytcqdruqt4olslo2cfh (document_id),
    CONSTRAINT FKfwkg6eytcqdruqt4olslo2cfh FOREIGN KEY (document_id) REFERENCES resource_documents (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE document_authors (
    document_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (document_id, author_id),
    KEY FKhl8uncgf9ux6ejeufr74nj7td (author_id),
    CONSTRAINT FK1hvn7amcsp2wgu6r2097jt2jg FOREIGN KEY (document_id) REFERENCES resource_documents (id),
    CONSTRAINT FKhl8uncgf9ux6ejeufr74nj7td FOREIGN KEY (author_id) REFERENCES authors (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE acquisition_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    note VARCHAR(500) DEFAULT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    document_id BIGINT NOT NULL,
    request_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY FKr7xlxgf65tnlrbx0lulgkjfry (document_id),
    KEY FK996fp1rm87ubq55on3qlxsfvn (request_id),
    CONSTRAINT FK996fp1rm87ubq55on3qlxsfvn FOREIGN KEY (request_id) REFERENCES acquisition_requests (id),
    CONSTRAINT FKr7xlxgf65tnlrbx0lulgkjfry FOREIGN KEY (document_id) REFERENCES resource_documents (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
