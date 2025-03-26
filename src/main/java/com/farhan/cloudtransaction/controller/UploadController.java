package com.farhan.cloudtransaction.controller;

import com.farhan.cloudtransaction.dto.ApiResponse;
import com.farhan.cloudtransaction.service.TransactionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/upload")
public class UploadController {

    private final TransactionService transactionService;
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    public UploadController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{token}")
    public String showUploadForm(@PathVariable String token, Model model) {
        try {
            // Just validate the token exists, but don't expose any data
            // The service will throw an exception if the token is invalid
            transactionService.validateUploadToken(token);
            
            model.addAttribute("token", token);
            return "upload-form";
        } catch (Exception e) {
            logger.error("Invalid upload token: {}", token);
            model.addAttribute("error", "Invalid or expired upload link");
            return "error";
        }
    }

    @PostMapping("/{token}")
    public String handleFileUpload(
            @PathVariable String token,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
                return "redirect:/upload/" + token;
            }
            
            transactionService.attachFileToTransaction(token, file);
            
            redirectAttributes.addFlashAttribute("message", "Thank you! Your file has been uploaded successfully.");
            return "redirect:/upload/success";
        } catch (Exception e) {
            logger.error("Error uploading file: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to upload file: " + e.getMessage());
            return "redirect:/upload/" + token;
        }
    }
    
    @GetMapping("/success")
    public String showSuccessPage() {
        return "upload-success";
    }
    
    @ResponseBody
    @PostMapping("/api/{token}")
    public ApiResponse<String> handleApiFileUpload(
            @PathVariable String token,
            @RequestParam("file") MultipartFile file) {
        
        try {
            if (file.isEmpty()) {
                return ApiResponse.error("Please select a file to upload");
            }
            
            transactionService.attachFileToTransaction(token, file);
            return ApiResponse.success("File uploaded successfully");
        } catch (Exception e) {
            logger.error("Error uploading file via API: {}", e.getMessage());
            return ApiResponse.error("Failed to upload file: " + e.getMessage());
        }
    }
} 