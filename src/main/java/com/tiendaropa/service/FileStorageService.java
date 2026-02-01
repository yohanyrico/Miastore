package com.tiendaropa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir:C:/tienda-data/uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            // Crear el directorio si no existe
            Files.createDirectories(this.fileStorageLocation);
            System.out.println("📁 Directorio de almacenamiento creado en: " + this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo crear el directorio de almacenamiento: " + uploadDir, ex);
        }
    }

    /**
     * Guarda un archivo en el sistema de archivos
     * @param file Archivo a guardar
     * @return Nombre del archivo guardado
     */
    public String storeFile(MultipartFile file) {
        // Normalizar nombre del archivo
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            // Verificar si el archivo contiene caracteres inválidos
            if (originalFileName.contains("..")) {
                throw new RuntimeException("El nombre del archivo contiene una secuencia de ruta inválida: " + originalFileName);
            }

            // Verificar que el archivo no esté vacío
            if (file.isEmpty()) {
                throw new RuntimeException("No se puede almacenar un archivo vacío: " + originalFileName);
            }

            // Generar un nombre único para el archivo
            String fileExtension = "";
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex > 0) {
                fileExtension = originalFileName.substring(dotIndex);
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;

            // Copiar archivo a la ubicación de destino
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ Archivo guardado: " + fileName);
            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo almacenar el archivo " + originalFileName + ". Inténtelo de nuevo.", ex);
        }
    }

    /**
     * Elimina un archivo del sistema de archivos
     * @param fileName Nombre del archivo a eliminar
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                System.out.println("🗑️ Archivo eliminado: " + fileName);
            } else {
                System.out.println("⚠️ Archivo no encontrado: " + fileName);
            }
        } catch (IOException ex) {
            System.err.println("❌ Error al eliminar archivo: " + fileName);
            throw new RuntimeException("No se pudo eliminar el archivo " + fileName, ex);
        }
    }

    /**
     * Carga la ruta de un archivo
     * @param fileName Nombre del archivo
     * @return Path del archivo
     */
    public Path loadFile(String fileName) {
        return this.fileStorageLocation.resolve(fileName).normalize();
    }

    /**
     * Verifica si un archivo existe
     * @param fileName Nombre del archivo
     * @return true si existe, false si no
     */
    public boolean fileExists(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.exists(filePath);
        } catch (Exception ex) {
            return false;
        }
    }
}