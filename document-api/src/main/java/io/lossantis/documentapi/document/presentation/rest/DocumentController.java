package io.lossantis.documentapi.document.presentation.rest;


import io.lossantis.documentapi.document.application.command.UploadDocumentCommand;
import io.lossantis.documentapi.document.application.UploadDocumentResult;
import io.lossantis.documentapi.document.application.usecase.UploadDocumentUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final UploadDocumentUseCase uploadDocumentUseCase;

    public DocumentController(UploadDocumentUseCase uploadDocumentUseCase) {
        this.uploadDocumentUseCase = uploadDocumentUseCase;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadDocumentResult> upload(@RequestParam("file") MultipartFile file)
            throws IOException {

        UploadDocumentCommand command = new UploadDocumentCommand(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream()
        );

        UploadDocumentResult result = uploadDocumentUseCase.execute(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }
}
