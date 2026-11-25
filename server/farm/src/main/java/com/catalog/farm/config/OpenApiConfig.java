package com.catalog.farm.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Farm Catalog REST API")
                        .version("1.0.0")
                        .description("RESTful API документация за онлайн платформата 'Фермерски Каталог'. "
                                + "Платформата осигурява директна връзка между локални земеделски производители (фермери) "
                                + "и крайни потребители (купувачи). Включва автентикация с JWT, профили на ферми, "
                                + "каталог с продукти и сезонност, масови редакции на цени и наличности, както и "
                                + "управление на клиентски запитвания / поръчки.")
                        .contact(new Contact()
                                .name("Farm Catalog Dev Team")
                                .email("contact@farm-catalog.bg"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("/").description("Текущ сървър / Default Server URL")
                ))
                .tags(List.of(
                        new Tag().name("Автентикация (/api/auth)").description("Регистрация, вход в системата и извличане на профила на текущо логнатия потребител"),
                        new Tag().name("Фермерски профили (/api/farms)").description("Публичен каталог с ферми, детайлен преглед, създаване и актуализация на фермерски профили"),
                        new Tag().name("Продукти (/api/products)").description("Каталог на продукти, филтрация по категория, регион и месец на сезонност, детайли и масова актуализация"),
                        new Tag().name("Запитвания / Поръчки (/api/inquiries)").description("Изпращане на клиентски запитвания от купувачи и обработка на техния статус от фермери"),
                        new Tag().name("Категории (/api/categories)").description("Номенклатура от категории за класификация на продуктите"),
                        new Tag().name("Региони (/api/regions)").description("Географски региони за локализиране на ферми и филтриране на продуктовия каталог")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Въведете Bearer JWT токен, получен след успешен вход (/api/auth/login) или регистрация (/api/auth/register). Пример: 'eyJhbGciOi...'")));
    }
}
