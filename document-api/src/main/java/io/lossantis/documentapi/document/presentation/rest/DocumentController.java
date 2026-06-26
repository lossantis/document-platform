package io.lossantis.documentapi.document.presentation.rest;

import io.lossantis.documentapi.document.application.command.UploadDocumentCommand;
import io.lossantis.documentapi.document.application.result.UploadDocumentResult;
import io.lossantis.documentapi.document.application.usecase.UploadDocumentUseCase;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final UploadDocumentUseCase uploadDocumentUseCase;

    public DocumentController(UploadDocumentUseCase uploadDocumentUseCase) {
        this.uploadDocumentUseCase = uploadDocumentUseCase;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public UploadDocumentResult upload(@RequestParam("file") MultipartFile file) {
        UploadDocumentCommand command = new UploadDocumentCommand(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize()
        );

        return uploadDocumentUseCase.execute(command);
    }
}
