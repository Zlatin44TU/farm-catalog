package com.catalog.farm.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catalog.farm.dto.request.InquiryDTO;
import com.catalog.farm.dto.request.InquiryStatusUpdateDTO;
import com.catalog.farm.dto.response.ErrorDetailsDTO;
import com.catalog.farm.dto.response.InquiryResponseDTO;
import com.catalog.farm.service.InquiryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
@Validated
@Tag(name = "Запитвания / Поръчки (/api/inquiries)", description = "Комуникация между купувачи и фермери за продукти")
public class InquiryController {

        private final InquiryService inquiryService;

        @PostMapping
        @PreAuthorize("hasRole('BUYER')")
        @Operation(summary = "Изпращане на запитване/заявка за конкретен продукт", description = "Позволява на потребител с роля BUYER да изпрати запитване за продукт със заявено количество и съобщение. "
                        + "Изпращане на запитване за собствен продукт на потребителя е забранено (хвърля SelfInquiryNotAllowedException).", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Запитването е изпратено успешно", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InquiryResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Невалидни данни или опит за запитване към собствен продукт", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
                        @ApiResponse(responseCode = "403", description = "Нямате роля BUYER"),
                        @ApiResponse(responseCode = "404", description = "Продуктът не съществува", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
        })
        public ResponseEntity<InquiryResponseDTO> createInquiry(
                        @RequestBody @Valid InquiryDTO dto,
                        @AuthenticationPrincipal UserDetails userDetails) {
                InquiryResponseDTO created = this.inquiryService.createInquiry(dto, userDetails.getUsername());
                URI location = URI.create(String.format("/api/inquiries/%s", created.id()));
                return ResponseEntity.created(location).body(created);
        }

        @GetMapping("/sent")
        @PreAuthorize("hasRole('BUYER')")
        @Operation(summary = "Преглед на изпратените запитвания от купувача", description = "Връща пагиниран списък с всички запитвания, изпратени от логнатия купувач. Размерът на страницата е защитен до максимум 50.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Списъкът със запитвания е извлечен успешно"),
                        @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
                        @ApiResponse(responseCode = "403", description = "Нямате роля BUYER")
        })
        public ResponseEntity<Page<InquiryResponseDTO>> getSentInquiries(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @Parameter(hidden = true) Pageable pageable) {
                Page<InquiryResponseDTO> sent = this.inquiryService.getSentInquiries(userDetails.getUsername(),
                                pageable);
                return ResponseEntity.ok(sent);
        }

        @GetMapping("/received")
        @PreAuthorize("hasRole('FARMER')")
        @Operation(summary = "Преглед на получените запитвания от фермера", description = "Връща пагиниран списък с всички запитвания за продукти от фермата на логнатия фермер. Размерът на страницата е защитен до максимум 50.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Списъкът с получени запитвания е извлечен успешно"),
                        @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
                        @ApiResponse(responseCode = "403", description = "Нямате роля FARMER"),
                        @ApiResponse(responseCode = "404", description = "Фермерът няма регистрирана ферма", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
        })
        public ResponseEntity<Page<InquiryResponseDTO>> getReceivedInquiries(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @Parameter(hidden = true) Pageable pageable) {
                Page<InquiryResponseDTO> received = this.inquiryService.getReceivedInquiries(userDetails.getUsername(),
                                pageable);
                return ResponseEntity.ok(received);
        }

        @PatchMapping("/{id}/status")
        @PreAuthorize("hasRole('FARMER')")
        @Operation(summary = "Промяна на статуса на запитване (ACCEPTED / REJECTED)", description = "Позволява на фермера собственик на продукта да приеме (ACCEPTED) или отхвърли (REJECTED) получено запитване. "
                        + "Статусът може да преминава само от PENDING към крайно състояние (ACCEPTED или REJECTED). Връщане към PENDING е забранено.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Статусът на запитването е актуализиран успешно", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InquiryResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Невалиден преход на статус (напр. опит за промяна на вече решено запитване)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
                        @ApiResponse(responseCode = "403", description = "Запитването принадлежи на продукт от друга ферма"),
                        @ApiResponse(responseCode = "404", description = "Запитването не е намерено", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
        })
        public ResponseEntity<InquiryResponseDTO> updateInquiryStatus(
                        @Parameter(description = "ID на запитването", required = true, example = "1") @PathVariable Long id,
                        @RequestBody @Valid InquiryStatusUpdateDTO dto,
                        @AuthenticationPrincipal UserDetails userDetails) {
                InquiryResponseDTO updated = this.inquiryService.updateInquiryStatus(id, dto.status(),
                                userDetails.getUsername());
                return ResponseEntity.ok(updated);
        }
}
