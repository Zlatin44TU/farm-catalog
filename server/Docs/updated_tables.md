# Документация на базата данни (Актуална версия, синхронизирана с JPA моделите)

### 1. Таблица users (Потребители)
Съхранява акаунтите на системата. В Spring Boot се управлява от Spring Security. Всеки потребител може да бъде зачислен към определена ферма чрез `farm_id`.

| Поле | Тип данни | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Уникален идентификатор |
| **email** | VARCHAR(100) | NOT NULL, UNIQUE | Имейл за вход |
| **password** | VARCHAR(255) | NOT NULL | Хеширана парола (BCrypt, `@JsonIgnore`) |
| **full_name** | VARCHAR(150) | NOT NULL | Име и фамилия |
| **phone_number** | VARCHAR(20) | NOT NULL | Телефон за връзка |
| **role** | VARCHAR(20) / ENUM | NOT NULL, DEFAULT 'BUYER' | Роля в системата (`ADMIN`, `BUYER`, `FARMER`) |
| **farm_id** | BIGINT | NULLABLE, FOREIGN KEY -> farm_profiles(id) | Ферма, към която принадлежи потребителят |
| **created_at** | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Дата на регистрация (`@CreationTimestamp`) |
| **updated_at** | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Последна промяна (`@UpdateTimestamp`) |

---

### 2. Таблица regions (Региони)
Нормализирана таблица за географските региони в България (напр. "Тракийска низина", "Родопи", "Северозападен").

| Поле | Тип данни | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Уникален идентификатор |
| **name** | VARCHAR(150) | NOT NULL | Име на региона (проверява се за уникалност в бизнес логиката) |

---

### 3. Таблица farm_profiles (Профили на ферми)
Съдържа специфичните данни за дадена ферма. Свързана е с регион (`region_id`) и може да съдържа множество потребители (чрез `users.farm_id`).

| Поле | Тип данни | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Уникален идентификатор |
| **region_id** | BIGINT | NOT NULL, FOREIGN KEY -> regions(id) | Връзка към региона |
| **farm_name** | VARCHAR(150) | NOT NULL | Име на фермата |
| **description** | TEXT | NULLABLE | Подробно описание |
| **address** | VARCHAR(255) | NOT NULL | Физически адрес |
| **contact_email** | VARCHAR(100) | NULLABLE | Публичен имейл за контакт |
| **contact_website** | VARCHAR(255) | NULLABLE | Уебсайт на фермата |
| **img_url** | VARCHAR(255) | NULLABLE | Линкова снимка/лого (`img_url` в модела) |
| **created_at** | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Дата на създаване (`@CreationTimestamp`) |
| **updated_at** | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Последна промяна (`@UpdateTimestamp`) |

> *Забележка:* Полето `owner_id` (собственик на фермата) е коментирано в JPA класа `FarmProfile` за текущия етап на разработка.

---

### 4. Таблица categories (Категории)
Номенклатура за категориите продукти (напр. "Плодове", "Зеленчуци", "Млечни", "Пчелни продукти").

| Поле | Тип данни | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Уникален идентификатор |
| **name** | VARCHAR(60) | NOT NULL, UNIQUE | Име на категорията |
| **description** | TEXT | NULLABLE | Описание на категорията |
| **created_at** | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Дата на създаване (`@CreationTimestamp`) |
| **updated_at** | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Последна промяна (`@UpdateTimestamp`) |

---

### 5. Таблица products (Продукти)
Основна таблица за предлаганите стоки. Свързана е с `farm_profiles` (Many-to-One) и `categories` (Many-to-One).

| Поле | Тип данни | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Уникален идентификатор |
| **farm_id** | BIGINT | NOT NULL, FOREIGN KEY -> farm_profiles(id) | Към коя ферма принадлежи |
| **category_id** | BIGINT | NOT NULL, FOREIGN KEY -> categories(id) | Категория на продукта |
| **name** | VARCHAR(150) | NOT NULL | Име на продукта |
| **description** | TEXT | NULLABLE | Описание |
| **price** | DECIMAL(10,2) | NOT NULL | Цена за единица |
| **unit** | VARCHAR(20) / ENUM | NOT NULL | Мерна единица (`KG`, `LITER`, `PIECE`, `PACKET`, `GRAM`, `MILLILITER`) |
| **stock_quantity** | DECIMAL(10,2) | NOT NULL DEFAULT 0.00 | Текущо налично количество |
| **is_available** | BOOLEAN | NOT NULL DEFAULT TRUE | Статус на наличност |
| **image_url** | VARCHAR(255) | NULLABLE | Снимка на продукта |
| **created_at** | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Дата на добавяне (`@CreationTimestamp`) |
| **updated_at** | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Последна промяна (`@UpdateTimestamp`) |

---

### 6. Таблица product_seasonality (Сезонност - Месеци)
Помощна таблица за връзка между продукти и месеци на сезонност (`@ElementCollection` в `Product`).

| Поле | Тип данни | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| **product_id** | BIGINT | NOT NULL, FOREIGN KEY -> products(id) ON DELETE CASCADE | Връзка към продукт |
| **month_numner** | INT / TINYINT | NOT NULL (1-12) | Месец от годината (1 = Януари, 12 = Декември). Забележка: в `Product.java` колоната е кръстена `month_numner`. |

**Комбиниран първичен ключ:** `PRIMARY KEY (product_id, month_numner)`

---

### 7. Таблица inquiries (Запитвания от клиенти / Ресторанти)
Таблица за B2B/B2C комуникация.

| Поле | Тип данни | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Уникален идентификатор |
| **buyer_id** | BIGINT | NULLABLE / NOT NULL, FOREIGN KEY -> users(id) | Потребителят, правищ запитването |
| **product_id** | BIGINT | NULLABLE / NOT NULL, FOREIGN KEY -> products(id) | Запитваният продукт |
| **requested_quantity** | DECIMAL(10,2) | NOT NULL | Желано количество |
| **message** | TEXT | NULLABLE | Допълнително съобщение/бележка |
| **status** | VARCHAR(15) / ENUM | NOT NULL, DEFAULT 'PENDING' | Статус на запитването (`PENDING`, `APPROVED`, `REJECTED`) |
| **created_at** | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Дата на изпращане (`@CreationTimestamp`) |
| **updated_at** | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Последна промяна (`@UpdateTimestamp`) |

---

## Връзки между ентитетите в Spring Boot JPA

В Spring Boot тези таблици се картографират с JPA анотации по следния начин:

1. **User <-> FarmProfile (Членство/Работници):**
   * В `User`: `@ManyToOne @JoinColumn(name = "farm_id") private FarmProfile farm;`
   * В `FarmProfile`: `@OneToMany(mappedBy = "farm") private List<User> members;`

2. **User <-> FarmProfile (Собственост):**
   * В `FarmProfile`: полето `owner` е коментирано временно (`// @ManyToOne @JoinColumn(name = "owner_id") private User owner;`), докато се имплементира съответната логика за собственост.

3. **Region <-> FarmProfile:**
   * В `FarmProfile`: `@ManyToOne @JoinColumn(name = "region_id", nullable = false) private Region region;`

4. **FarmProfile <-> Product:**
   * В `FarmProfile`: `@OneToMany(mappedBy = "farm") private List<Product> products;`
   * В `Product`: `@ManyToOne @JoinColumn(name = "farm_id", nullable = false) private FarmProfile farm;`

5. **Category <-> Product:**
   * В `Product`: `@ManyToOne @JoinColumn(name = "category_id", nullable = false) private Category category;`

6. **Product <-> Seasonality:**
   * В `Product`: `@ElementCollection` с `@CollectionTable(name = "product_seasonality", joinColumns = @JoinColumn(name = "product_id"))` и `@Column(name = "month_numner", nullable = false) private Set<Integer> seasonMonths`.

7. **Inquiry:**
   * В `Inquiry`: `@ManyToOne @JoinColumn(name = "buyer_id") private User user;`
   * В `Inquiry`: `@ManyToOne @JoinColumn(name = "product_id") private Product product;`
   * Статус енъм: `Stauts` със стойности `PENDING`, `APPROVED`, `REJECTED`.

---

## SQL скрипт за генериране на таблиците (DDL)

```sql
-- 1. Таблица за региони
CREATE TABLE regions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL
);

-- 2. Таблица за профили на ферми
CREATE TABLE farm_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    region_id BIGINT NOT NULL,
    farm_name VARCHAR(150) NOT NULL,
    description TEXT,
    address VARCHAR(255) NOT NULL,
    contact_email VARCHAR(100),
    contact_website VARCHAR(255),
    img_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (region_id) REFERENCES regions(id)
);

-- 3. Таблица за потребители
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    role ENUM('ADMIN', 'BUYER', 'FARMER') NOT NULL DEFAULT 'BUYER',
    farm_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_farm FOREIGN KEY (farm_id) REFERENCES farm_profiles(id) ON DELETE SET NULL
);

-- 4. Таблица за категории
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 5. Таблица за продукти
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farm_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    unit ENUM('KG', 'LITER', 'PIECE', 'PACKET', 'GRAM', 'MILLILITER') NOT NULL,
    stock_quantity DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (farm_id) REFERENCES farm_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- 6. Таблица за сезонност
CREATE TABLE product_seasonality (
    product_id BIGINT NOT NULL,
    month_numner INT NOT NULL CHECK (month_numner BETWEEN 1 AND 12),
    PRIMARY KEY (product_id, month_numner),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- 7. Таблица за запитвания
CREATE TABLE inquiries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    buyer_id BIGINT,
    product_id BIGINT,
    requested_quantity DECIMAL(10,2) NOT NULL,
    message TEXT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
```
