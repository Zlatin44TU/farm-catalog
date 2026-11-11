# Документация на базата данни (Версия с поддръжка на множество потребители във ферма)

### 1. Таблица users (Потребители)
Съхранява акаунтите на системата. В Spring Boot се управлява от Spring Security. Всеки потребител може да бъде зачислен към определена ферма чрез `farm_id`.

| Поле | Тип данни | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Уникален идентификатор |
| **email** | VARCHAR(100) | NOT NULL, UNIQUE | Имейл за вход |
| **password** | VARCHAR(255) | NOT NULL | Хеширана парола (BCrypt) |
| **full_name** | VARCHAR(100) | NOT NULL | Име и фамилия |
| **phone_number** | VARCHAR(20) | NOT NULL | Телефон за връзка |
| **role** | ENUM('BUYER', 'FARMER', 'ADMIN') | NOT NULL | Роля в системата |
| **farm_id** | BIGINT | NULLABLE, FOREIGN KEY -> farm_profiles(id) | Ферма, към която принадлежи потребителят |
| **created_at** | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Дата на регистрация |

---

### 2. Таблица regions (Региони)
Нормализирана таблица за географските региони в България (напр. "Тракийска низина", "Родопи", "Северозападен").

| Поле | Тип данни | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Уникален идентификатор |
| **name** | VARCHAR(100) | NOT NULL, UNIQUE | Име на региона |

---

### 3. Таблица farm_profiles (Профили на ферми)
Съдържа специфичните данни за дадена ферма. Всяка ферма се притежава от един собственик (`owner_id`), свързана е с регион (`region_id`) и може да съдържа множество потребители (чрез `users.farm_id`).

| Поле | Тип данни | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Уникален идентификатор |
| **owner_id** | BIGINT | NOT NULL, FOREIGN KEY -> users(id) | Собственик / създател на фермата |
| **region_id** | BIGINT | NOT NULL, FOREIGN KEY -> regions(id) | Връзка към региона |
| **farm_name** | VARCHAR(150) | NOT NULL | Име на фермата |
| **description** | TEXT | NULLABLE | Подробно описание |
| **address** | VARCHAR(255) | NOT NULL | Физически адрес |
| **contact_email** | VARCHAR(100) | NOT NULL | Публичен имейл за контакт |
| **image_url** | VARCHAR(255) | NULLABLE | Линкова снимка/лого |

---

### 4. Таблица categories (Категории)
Номенклатура за категориите продукти (напр. "Плодове", "Зеленчуци", "Млечни", "Пчелни продукти").

| Поле | Тип данни | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Уникален идентификатор |
| **name** | VARCHAR(50) | NOT NULL, UNIQUE | Име на категорията |
| **description** | VARCHAR(255) | NULLABLE | Кратко описание |

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
| **unit** | ENUM('KG', 'G', 'LITER', 'PIECE') | NOT NULL | Мерна единица |
| **stock_quantity** | DECIMAL(10,2) | NOT NULL DEFAULT 0.00 | Текущо налично количество |
| **is_available** | BOOLEAN | NOT NULL DEFAULT TRUE | Статус на наличност |
| **image_url** | VARCHAR(255) | NULLABLE | Снимка на продукта |
| **created_at** | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Дата на добавяне |
| **updated_at** | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Последна промяна |

---

### 6. Таблица product_seasonality (Сезонност - Месеци)
Помощна таблица за връзка Many-to-Many между продукти и месеци на сезонност.

| Поле | Тип данни | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| **product_id** | BIGINT | NOT NULL, FOREIGN KEY -> products(id) ON DELETE CASCADE | Връзка към продукт |
| **month_number** | TINYINT | NOT NULL (1-12) | Месец от годината (1 = Януари, 12 = Декември) |

**Комбиниран първичен ключ:** `PRIMARY KEY (product_id, month_number)`

---

### 7. Таблица inquiries (Запитвания от клиенти / Ресторанти)
Таблица за B2B/B2C комуникация.

| Поле | Тип данни | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Уникален идентификатор |
| **buyer_id** | BIGINT | NOT NULL, FOREIGN KEY -> users(id) | Потребителят, правищ запитването |
| **product_id** | BIGINT | NOT NULL, FOREIGN KEY -> products(id) | Запитваният продукт |
| **requested_quantity** | DECIMAL(10,2) | NOT NULL | Желано количество |
| **message** | TEXT | NULLABLE | Допълнително съобщение/бележка |
| **status** | ENUM('PENDING', 'ACCEPTED', 'REJECTED') | DEFAULT 'PENDING' | Статус на запитването |
| **created_at** | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Дата на изпращане |

---

## Връзки между ентитетите в Spring Boot JPA

В Spring Boot тези таблици се картографират с JPA анотации по следния начин:

1. **User <-> FarmProfile (Собственост):**
   * В `FarmProfile`: `@ManyToOne @JoinColumn(name = "owner_id") private User owner;`
   * В `User`: `@OneToMany(mappedBy = "owner") private List<FarmProfile> ownedFarms;` *(или `@OneToOne`, ако един потребител може да притежава само една ферма)*.

2. **User <-> FarmProfile (Членство/Работници):**
   * В `User`: `@ManyToOne @JoinColumn(name = "farm_id") private FarmProfile farm;`
   * В `FarmProfile`: `@OneToMany(mappedBy = "farm") private List<User> workers;`

3. **Region <-> FarmProfile:**
   * В `FarmProfile`: `@ManyToOne @JoinColumn(name = "region_id") private Region region;`

4. **FarmProfile <-> Product:**
   * В `FarmProfile`: `@OneToMany(mappedBy = "farm") private List<Product> products;`
   * В `Product`: `@ManyToOne @JoinColumn(name = "farm_id") private FarmProfile farm;`

5. **Category <-> Product:**
   * В `Product`: `@ManyToOne @JoinColumn(name = "category_id") private Category category;`

6. **Product <-> Seasonality:**
   * В `Product`: `@ElementCollection` с `@CollectionTable(name = "product_seasonality")`.

7. **Inquiry:**
   * В `Inquiry`: `@ManyToOne` към `User` (buyer) и `@ManyToOne` към `Product`.

---

## SQL скрипт за генериране на таблиците

```sql
-- 1. Таблица за региони
CREATE TABLE regions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- 2. Таблица за потребители (без външния ключ към farm_profiles за момента поради циклична зависимост)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    role ENUM('BUYER', 'FARMER', 'ADMIN') NOT NULL DEFAULT 'BUYER',
    farm_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Таблица за профили на ферми
CREATE TABLE farm_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,
    farm_name VARCHAR(150) NOT NULL,
    description TEXT,
    address VARCHAR(255) NOT NULL,
    contact_email VARCHAR(100) NOT NULL,
    image_url VARCHAR(255),
    FOREIGN KEY (owner_id) REFERENCES users(id),
    FOREIGN KEY (region_id) REFERENCES regions(id)
);

-- Добавяне на външния ключ в users към farm_profiles
ALTER TABLE users
ADD CONSTRAINT fk_users_farm 
FOREIGN KEY (farm_id) REFERENCES farm_profiles(id) ON DELETE SET NULL;

-- 4. Таблица за категории
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 5. Таблица за продукти
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farm_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    unit ENUM('KG', 'G', 'LITER', 'PIECE') NOT NULL,
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
    month_number TINYINT NOT NULL CHECK (month_number BETWEEN 1 AND 12),
    PRIMARY KEY (product_id, month_number),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- 7. Таблица за запитвания
CREATE TABLE inquiries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    buyer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    requested_quantity DECIMAL(10,2) NOT NULL,
    message TEXT,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
```